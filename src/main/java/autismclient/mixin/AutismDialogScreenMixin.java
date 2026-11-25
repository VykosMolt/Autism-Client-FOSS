package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.modules.PoolEntry;
import autismclient.util.AutismCustomFilterOverlay;
import autismclient.util.AutismCustomFilterPresetOverlay;
import autismclient.util.AutismKeybindOverlay;
import autismclient.util.AutismLANSyncOverlay;
import autismclient.util.AutismLauncherOverlay;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.AutismMacroListOverlay;
import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.AutismQueueEditorOverlay;
import autismclient.util.AutismServerInfoOverlay;
import autismclient.util.aE;
import autismclient.util.bk;
import autismclient.util.cX;
import autismclient.util.cx;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.dialog.WaitingForResponseScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({DialogScreen.class, WaitingForResponseScreen.class})
public abstract class AutismDialogScreenMixin extends Screen {
   @Unique
   private AutismLauncherOverlay launcherOverlay;
   @Unique
   private AutismLANSyncOverlay lanSyncOverlay;
   @Unique
   private AutismMacroListOverlay macroListOverlay;
   @Unique
   private AutismQueueEditorOverlay queueEditorOverlay;
   @Unique
   private AutismPacketLoggerOverlay packetLoggerOverlay;
   @Unique
   private AutismCustomFilterOverlay customFilterOverlay;
   @Unique
   private AutismCustomFilterPresetOverlay customFilterPresetOverlay;
   @Unique
   private AutismMacroEditorOverlay macroEditorOverlay;
   @Unique
   private AutismKeybindOverlay keybindOverlay;
   @Unique
   private AutismServerInfoOverlay serverInfoOverlay;
   @Unique
   private boolean autism$overlaysBuilt;

   protected AutismDialogScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void autism$init(CallbackInfo ci) {
      Screen screen = this;
      ScreenEvents.afterExtract(screen).register((scrn, drawContext, mouseX, mouseY, tickDelta) -> {
         if (this.autism$isAutismActive()) {
            try {
               bk.init_bk().init(drawContext, mouseX, mouseY, tickDelta);
            } catch (Throwable var7) {
            }
         }
      });
      ScreenEvents.remove(screen).register(scrn -> {
         Minecraft mc = Minecraft.getInstance();
         boolean stillOnDialog = mc != null && (mc.gui.screen() instanceof DialogScreen || mc.gui.screen() instanceof WaitingForResponseScreen);
         if (!stillOnDialog) {
            this.autism$overlaysBuilt = false;
            this.autism$saveOverlays();
         }
      });
      if (this.autism$isAutismActive() && !cx.read_Z()) {
         if (!this.autism$overlaysBuilt || !bk.init_bk().init_Z()) {
            try {
               this.autism$buildOverlays();
               this.autism$overlaysBuilt = true;
            } catch (Throwable var4) {
            }
         } else if (this.launcherOverlay != null) {
            this.launcherOverlay.init_V_Z(true);
         }
      }
   }

   @Unique
   private void autism$buildOverlays() {
      aE.init_aE().flush(() -> {});
      this.lanSyncOverlay = AutismLANSyncOverlay.init_AutismLANSyncOverl_Font(this.font);
      this.macroListOverlay = new AutismMacroListOverlay(this.font);
      this.queueEditorOverlay = new AutismQueueEditorOverlay(this.font);
      this.customFilterOverlay = new AutismCustomFilterOverlay(this.font);
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
         this.macroEditorOverlay.load_V_Z_3(!inWorld);
      }

      bk manager = bk.init_bk();
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

      ActionEditorOverlay actionEditor = ActionEditorOverlay.init_ActionEditorOverla_2();
      actionEditor.load_V_Z(inWorld);
      manager.init_V(actionEditor);
      PoolEntry autismModule = PoolEntry.init_PoolEntry();
      if (!AutismLiteVariant.enabled()) {
         cX multiOverlay = autismModule == null ? null : autismModule.sync_cX();
         if (multiOverlay != null && multiOverlay.sync_Z()) {
            manager.init_V(multiOverlay);
         }
      }

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
            this.serverInfoOverlay = PoolEntry.init_PoolEntry().init_AutismServerInfoOv();
         }

         if (this.serverInfoOverlay != null) {
            manager.init_V(this.serverInfoOverlay);
         }

         return this.serverInfoOverlay;
      });
      if (this.packetLoggerOverlay == null && AutismPacketLoggerOverlay.poll_Z_13()) {
         this.packetLoggerOverlay = PoolEntry.init_PoolEntry().init_AutismPacketLogger();
         if (this.packetLoggerOverlay != null) {
            this.packetLoggerOverlay.flush_V_13();
            if (this.packetLoggerOverlay.sync_Z()) {
               manager.init_V(this.packetLoggerOverlay);
            }
         }
      }

      if (this.serverInfoOverlay == null && AutismServerInfoOverlay.poll_Z_16()) {
         this.serverInfoOverlay = PoolEntry.init_PoolEntry().init_AutismServerInfoOv();
         if (this.serverInfoOverlay != null) {
            this.serverInfoOverlay.update_V_21();
            if (this.serverInfoOverlay.sync_Z()) {
               manager.init_V(this.serverInfoOverlay);
            }
         }
      }

      if (!AutismLiteVariant.enabled()) {
         cX profilesOverlay = PoolEntry.init_PoolEntry().load_cX();
         if (profilesOverlay != null && profilesOverlay.sync_Z()) {
            manager.init_V(profilesOverlay);
         }
      }

      this.launcherOverlay.b__V();
      this.launcherOverlay.init_V_Z(true);
      manager.init_V(this.launcherOverlay);
   }

   @Unique
   private void autism$saveOverlays() {
      if (this.autism$isAutismActive() && !cx.read_Z()) {
         try {
            bk.init_bk().close();
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
         } finally {
            bk.init_bk().load_V();
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      return this.autism$isAutismActive() && bk.init_bk().init_Z_D_2(click.x(), click.y(), Compat263.mouseEventGlfwButton(click))
         ? true
         : super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent click) {
      return this.autism$isAutismActive() && bk.init_bk().load(click.x(), click.y(), Compat263.mouseEventGlfwButton(click)) ? true : super.mouseReleased(click);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.autism$isAutismActive() && bk.init_bk().init(click.x(), click.y(), Compat263.mouseEventGlfwButton(click), deltaX, deltaY)
         ? true
         : super.mouseDragged(click, deltaX, deltaY);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.autism$isAutismActive() && bk.init_bk().init_Z_D(mouseX, mouseY, verticalAmount)
         ? true
         : super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      return this.autism$isAutismActive() && bk.init_bk().init_Z_I(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
         ? true
         : super.keyPressed(input);
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.autism$isAutismActive() && bk.init_bk().init_Z_C_I((char)input.codepoint(), 0) ? true : super.charTyped(input);
   }

   @Unique
   private boolean autism$isAutismActive() {
      PoolEntry module = PoolEntry.init_PoolEntry();
      return module != null && module.load_Z();
   }
}
