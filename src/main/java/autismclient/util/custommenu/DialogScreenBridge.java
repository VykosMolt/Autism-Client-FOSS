package autismclient.util.custommenu;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.mixin.accessor.AutismDialogScreenAccessor;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;

public final class DialogScreenBridge {
   private static final long init = 500L;

   private DialogScreenBridge() {
   }

   public static CustomMenuSnapshot init_CustomMenuSnapshot(Minecraft mc) {
      Dialog dialog = init_Dialog(mc);
      return dialog == null ? null : VanillaDialogMenuAdapter.init(dialog, mc.getConnection() != null ? "PLAY" : "CONFIGURATION");
   }

   public static Screen init_Screen(Minecraft mc) {
      return mc != null && mc.gui != null ? mc.gui.screen() : null;
   }

   public static void init(Minecraft mc, ClickEvent clientAction, Screen answered) {
      if (mc != null && answered instanceof DialogScreen) {
         CompletableFuture<Void> applied;
         try {
            applied = mc.submit(() -> {
               if (mc.gui.screen() == answered && mc.gui.screen() instanceof DialogScreen<?> screen) {
                  Dialog dialog = ((AutismDialogScreenAccessor)screen).autism$dialog();
                  DialogAction after = dialog == null ? DialogAction.CLOSE : dialog.common().afterAction();
                  if (clientAction != null || after != DialogAction.NONE) {
                     screen.runAction(Optional.ofNullable(clientAction));
                  }
               }
            });
         } catch (RuntimeException clientGone) {
            return;
         }

         try {
            applied.get(500L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
         } catch (Exception var6) {
         }
      }
   }

   private static Dialog init_Dialog(Minecraft mc) {
      if (mc != null && mc.gui != null) {
         return mc.gui.screen() instanceof DialogScreen<?> screen ? ((AutismDialogScreenAccessor)screen).autism$dialog() : null;
      } else {
         return null;
      }
   }
}
