package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;

public final class OverlayScreenController {
   private static final autismclient.gui.ui.ForegroundScreenSlot<Screen> FOREGROUND_SLOT = new autismclient.gui.ui.ForegroundScreenSlot<>();
   private static int explicitTransitionDepth;
   private static int rawScreenReadDepth;
   private static boolean switchingRetainedScreen;
   private static Screen lastRequestedBackgroundScreen;
   private static Screen modalScreen;
   private static Screen modalParentScreen;
   private static ClientLevel modalLevel;
   private static boolean modalTransitionInProgress;

   private OverlayScreenController() {
   }

   public static void beginExplicitTransition() {
      explicitTransitionDepth++;
   }

   public static void endExplicitTransition() {
      explicitTransitionDepth = Math.max(0, explicitTransitionDepth - 1);
   }

   public static void beginRawScreenRead() {
      rawScreenReadDepth++;
   }

   public static void endRawScreenRead() {
      rawScreenReadDepth = Math.max(0, rawScreenReadDepth - 1);
   }

   public static void setScreenExplicit(Minecraft client, Screen next) {
      beginExplicitTransition();

      try {
         client.gui.setScreen(next);
      } finally {
         endExplicitTransition();
      }
   }

   public static Screen resolveEffectiveScreen(Screen actual) {
      return rawScreenReadDepth > 0 || modalScreen != null && actual == modalScreen ? actual : FOREGROUND_SLOT.orElse(actual);
   }

   public static Screen getActualScreen(Minecraft client) {
      beginRawScreenRead();

      try {
         return client.gui.screen();
      } finally {
         endRawScreenRead();
      }
   }

   public static boolean hasModalScreen() {
      return modalScreen != null;
   }

   public static boolean openModalScreen(Minecraft client, Screen screen) {
      if (!hasModalScreen() && client != null) {
         modalParentScreen = getActualScreen(client);
         modalLevel = client.level;
         modalScreen = screen;
         modalTransitionInProgress = true;

         try {
            client.gui.setScreen(screen);
         } finally {
            modalTransitionInProgress = false;
         }

         Screen current = getActualScreen(client);
         if (current == screen) {
            return true;
         }

         if (current == modalParentScreen) {
            modalParentScreen = null;
         }

         discardModalScreen(screen);
         return false;
      } else {
         return false;
      }
   }

   public static void closeModalScreen(Minecraft client, Screen screen) {
      if (modalScreen == screen) {
         Screen parent = client != null && client.level == modalLevel ? modalParentScreen : null;
         if (client != null && getActualScreen(client) == screen) {
            if (parent == null) {
               discardModalScreen(screen);
            } else {
               modalScreen = null;
               modalParentScreen = null;
               modalLevel = null;
            }

            modalTransitionInProgress = true;

            try {
               client.gui.setScreen(parent);
            } finally {
               modalTransitionInProgress = false;
            }
         } else {
            discardModalScreen(screen);
         }
      }
   }

   public static void discardModalScreen(Screen screen) {
      if (modalScreen == screen) {
         Screen previous = modalParentScreen;
         modalScreen = null;
         modalParentScreen = null;
         modalLevel = null;
         if (previous != null && previous != FOREGROUND_SLOT.get()) {
            previous.removed();
         }
      }
   }

   public static boolean hasRetainedForeground() {
      return FOREGROUND_SLOT.isSet();
   }

   public static boolean shouldInterceptBackgroundScreen() {
      return hasRetainedForeground() && explicitTransitionDepth == 0 && !switchingRetainedScreen;
   }

   private static boolean isRetainedForegroundScreen(Screen screen) {
      return AutismLiteVariant.enabled() ? false : screen instanceof autismclient.gui.ui.RetainedForegroundScreen;
   }

   public static void captureRetainedForegroundIfNeeded(Minecraft client) {
      if (!FOREGROUND_SLOT.isSet() && client != null && !autismclient.modules.aD.init_Z()) {
         Screen current = client.gui.screen();
         if (isRetainedForegroundScreen(current)) {
            FOREGROUND_SLOT.setIfEmpty(current);
         }
      }
   }

   public static boolean interceptSetScreen(Minecraft client, Screen actual, Screen requested) {
      if (!switchingRetainedScreen && !modalTransitionInProgress) {
         if (modalScreen != null && actual == modalScreen && requested != modalScreen) {
            if (requested == modalParentScreen) {
               modalParentScreen = null;
            }

            discardModalScreen(actual);
         }

         if (FOREGROUND_SLOT.isSet() && (explicitTransitionDepth > 0 || isRetainedForegroundScreen(requested))) {
            if (requested == FOREGROUND_SLOT.get()) {
               return true;
            }

            switchingRetainedScreen = true;

            try {
               Screen previous = FOREGROUND_SLOT.load(isRetainedForegroundScreen(requested) ? requested : null);
               autismclient.gui.ui.widgets.TextInputWidget.clearGlobalFocus();
               if (previous == actual) {
                  return false;
               }

               if (previous != null && previous != actual) {
                  previous.removed();
               }

               if (FOREGROUND_SLOT.isSet()) {
                  requested.added();
                  requested.init(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
                  return true;
               }

               if (!isRetainedForegroundScreen(requested)) {
                  boolean staleContainer = requested instanceof AbstractContainerScreen<?> container
                     && (client.player == null || container.getMenu() != client.player.inventoryMenu && container.getMenu() != client.player.containerMenu);
                  if (requested == null || requested == lastRequestedBackgroundScreen || staleContainer) {
                     if (actual == null) {
                        client.mouseHandler.grabMouse();
                     } else {
                        actual.resize(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
                     }

                     return true;
                  }
               }
            } finally {
               switchingRetainedScreen = false;
            }
         }

         if (explicitTransitionDepth == 0 && !isRetainedForegroundScreen(requested)) {
            captureRetainedForegroundIfNeeded(client);
         }

         lastRequestedBackgroundScreen = requested;
         return false;
      } else {
         return false;
      }
   }

   public static boolean shouldPreserveOnRemove(Screen screen) {
      return modalScreen != null && modalTransitionInProgress && screen == modalParentScreen || FOREGROUND_SLOT.isSet() && screen == FOREGROUND_SLOT.get() && !switchingRetainedScreen;
   }

   public static void tickRetainedForeground(Screen actual) {
      Screen retained = FOREGROUND_SLOT.get();
      if (retained != null && retained != actual) {
         retained.tick();
      }

      if (retained != null) {
         Minecraft client = Minecraft.getInstance();
         if (client.mouseHandler.isMouseGrabbed()) {
            client.mouseHandler.releaseMouse();
         }
      }
   }

   public static void clearRetainedForeground() {
      Screen old = FOREGROUND_SLOT.load();
      Minecraft client = Minecraft.getInstance();
      if (old != null && (client == null || old != getActualScreen(client))) {
         old.removed();
      }

      lastRequestedBackgroundScreen = null;
   }
}
