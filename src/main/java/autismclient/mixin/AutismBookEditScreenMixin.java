package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.modules.PoolEntry;
import autismclient.util.AutismClientMessaging;
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
import autismclient.util.ck;
import autismclient.util.cp;
import autismclient.util.cx;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookEditScreen.class)
public abstract class AutismBookEditScreenMixin extends Screen implements cp {
   @Unique
   private static final Minecraft MC = Minecraft.getInstance();
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
   private boolean autism$overlaysInitialized;

   protected AutismBookEditScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void yang$init(CallbackInfo ci) {
      if (this.yang$isAutismActive() && !cx.read_Z()) {
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

         this.macroEditorOverlay = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
         if (this.macroEditorOverlay != null) {
            this.macroEditorOverlay.poll_V_5();
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
               PoolEntry mod = PoolEntry.init_PoolEntry();
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
            PoolEntry mod = PoolEntry.init_PoolEntry();
            this.packetLoggerOverlay = mod == null ? null : mod.init_AutismPacketLogger();
            if (this.packetLoggerOverlay != null) {
               this.packetLoggerOverlay.flush_V_13();
               if (this.packetLoggerOverlay.sync_Z()) {
                  manager.init_V(this.packetLoggerOverlay);
               }
            }
         }

         if (this.serverInfoOverlay == null && AutismServerInfoOverlay.poll_Z_16()) {
            PoolEntry mod = PoolEntry.init_PoolEntry();
            this.serverInfoOverlay = mod == null ? null : mod.init_AutismServerInfoOv();
            if (this.serverInfoOverlay != null) {
               this.serverInfoOverlay.update_V_21();
               if (this.serverInfoOverlay.sync_Z()) {
                  manager.init_V(this.serverInfoOverlay);
               }
            }
         }

         this.launcherOverlay.b__V();
         manager.init_V(this.launcherOverlay);
         this.autism$overlaysInitialized = true;
         Screen screen = this;
         ScreenEvents.afterExtract(screen).register((scrn, drawContext, mouseX, mouseY, tickDelta) -> {
            if (this.yang$isAutismActive()) {
               bk.init_bk().init(drawContext, mouseX, mouseY, tickDelta);
            }
         });
      }
   }

   @Override
   public void removed() {
      if ((this.autism$overlaysInitialized || this.yang$isAutismActive()) && !cx.read_Z()) {
         this.autism$overlaysInitialized = false;
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

         bk.init_bk().load_V();
      }

      super.removed();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      return this.yang$isAutismActive() && bk.init_bk().init_Z_D_2(click.x(), click.y(), Compat263.mouseEventGlfwButton(click))
         ? true
         : super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent click) {
      return this.yang$isAutismActive() && bk.init_bk().load(click.x(), click.y(), Compat263.mouseEventGlfwButton(click)) ? true : super.mouseReleased(click);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.yang$isAutismActive() && bk.init_bk().init(click.x(), click.y(), Compat263.mouseEventGlfwButton(click), deltaX, deltaY)
         ? true
         : super.mouseDragged(click, deltaX, deltaY);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.yang$isAutismActive() && bk.init_bk().init_Z_D(mouseX, mouseY, verticalAmount)
         ? true
         : super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
   private void yang$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
      if (this.yang$isAutismActive() && bk.init_bk().init_Z_I(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         cir.setReturnValue(true);
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.yang$isAutismActive() && bk.init_bk().init_Z_C_I((char)input.codepoint(), 0) ? true : super.charTyped(input);
   }

   @Unique
   private boolean yang$isAutismActive() {
      PoolEntry module = PoolEntry.init_PoolEntry();
      return module != null && module.init_Z();
   }

   @Override
   public void autism$closeWithPacket() {
      this.autism$closeWithPacket(true);
   }

   @Override
   public void autism$closeWithPacket(boolean notify) {
      if (MC.getConnection() != null) {
         ck.init_ck().data(true);
         this.autism$invokeSaveChanges();
      }

      MC.gui.setScreen(null);
   }

   @Override
   public void autism$closeWithoutPacket() {
      this.autism$closeWithoutPacket(true);
   }

   @Override
   public void autism$closeWithoutPacket(boolean notify) {
      MC.gui.setScreen(null);
      if (notify) {
         AutismClientMessaging.sendPrefixed("Book edit closed without packet.");
      }
   }

   @Override
   public void autism$desync() {
      this.autism$desync(true);
   }

   @Override
   public void autism$desync(boolean notify) {
      if (MC.getConnection() == null) {
         if (notify) {
            AutismClientMessaging.sendPrefixed("Failed to desync: no network.");
         }
      } else {
         ck.init_ck().data(true);
         this.autism$invokeSaveChanges();
         if (notify) {
            AutismClientMessaging.sendPrefixed("Book update packet sent; GUI intentionally stays open.");
         }
      }
   }

   @Invoker("saveChanges")
   protected abstract void autism$invokeSaveChanges();
}
