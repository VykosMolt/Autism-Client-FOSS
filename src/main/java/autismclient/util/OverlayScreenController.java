package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;

public final class OverlayScreenController {
   private static final autismclient.gui.ui.ForegroundScreenSlot<Screen> init_CachePool = new autismclient.gui.ui.ForegroundScreenSlot<>();
   private static int init_I;
   private static int load_I;
   private static boolean init_Z;
   private static Screen init_Screen;
   private static Screen load_Screen;
   private static Screen read;
   private static ClientLevel init_ClientLevel;
   private static boolean load_Z;

   private OverlayScreenController() {
   }

   public static void init_V() {
      init_I++;
   }

   public static void load_V() {
      init_I = Math.max(0, init_I - 1);
   }

   public static void read_V() {
      load_I++;
   }

   public static void write() {
      load_I = Math.max(0, load_I - 1);
   }

   public static void init_V(Minecraft client, Screen next) {
      init_V();

      try {
         client.gui.setScreen(next);
      } finally {
         load_V();
      }
   }

   public static Screen init_Screen(Screen actual) {
      return load_I > 0 || load_Screen != null && actual == load_Screen ? actual : init_CachePool.init_Obj(actual);
   }

   public static Screen init_Screen(Minecraft client) {
      read_V();

      try {
         return client.gui.screen();
      } finally {
         write();
      }
   }

   public static boolean init_Z() {
      return load_Screen != null;
   }

   public static boolean init_Z(Minecraft client, Screen screen) {
      if (!init_Z() && client != null) {
         read = init_Screen(client);
         init_ClientLevel = client.level;
         load_Screen = screen;
         load_Z = true;

         try {
            client.gui.setScreen(screen);
         } finally {
            load_Z = false;
         }

         Screen current = init_Screen(client);
         if (current == screen) {
            return true;
         }

         if (current == read) {
            read = null;
         }

         init_V(screen);
         return false;
      } else {
         return false;
      }
   }

   public static void load(Minecraft client, Screen screen) {
      if (load_Screen == screen) {
         Screen parent = client != null && client.level == init_ClientLevel ? read : null;
         if (client != null && init_Screen(client) == screen) {
            if (parent == null) {
               init_V(screen);
            } else {
               load_Screen = null;
               read = null;
               init_ClientLevel = null;
            }

            load_Z = true;

            try {
               client.gui.setScreen(parent);
            } finally {
               load_Z = false;
            }
         } else {
            init_V(screen);
         }
      }
   }

   public static void init_V(Screen screen) {
      if (load_Screen == screen) {
         Screen previous = read;
         load_Screen = null;
         read = null;
         init_ClientLevel = null;
         if (previous != null && previous != init_CachePool.init_Obj()) {
            previous.removed();
         }
      }
   }

   public static boolean load_Z() {
      return init_CachePool.init_Z();
   }

   public static boolean read_Z() {
      return load_Z() && init_I == 0 && !init_Z;
   }

   private static boolean load_Z(Screen screen) {
      return AutismLiteVariant.enabled() ? false : screen instanceof autismclient.gui.ui.RetainedForegroundScreen;
   }

   public static void init_V(Minecraft client) {
      if (!init_CachePool.init_Z() && client != null && !autismclient.modules.aD.init_Z()) {
         Screen current = client.gui.screen();
         if (load_Z(current)) {
            init_CachePool.init_V(current);
         }
      }
   }

   public static boolean init(Minecraft client, Screen actual, Screen requested) {
      if (!init_Z && !load_Z) {
         if (load_Screen != null && actual == load_Screen && requested != load_Screen) {
            if (requested == read) {
               read = null;
            }

            init_V(actual);
         }

         if (init_CachePool.init_Z() && (init_I > 0 || load_Z(requested))) {
            if (requested == init_CachePool.init_Obj()) {
               return true;
            }

            init_Z = true;

            try {
               Screen previous = init_CachePool.load(load_Z(requested) ? requested : null);
               autismclient.gui.ui.widgets.TextInputWidget.init_V();
               if (previous == actual) {
                  return false;
               }

               if (previous != null && previous != actual) {
                  previous.removed();
               }

               if (init_CachePool.init_Z()) {
                  requested.added();
                  requested.init(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
                  return true;
               }

               if (!load_Z(requested)) {
                  boolean staleContainer = requested instanceof AbstractContainerScreen<?> container
                     && (client.player == null || container.getMenu() != client.player.inventoryMenu && container.getMenu() != client.player.containerMenu);
                  if (requested == null || requested == init_Screen || staleContainer) {
                     if (actual == null) {
                        client.mouseHandler.grabMouse();
                     } else {
                        actual.resize(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
                     }

                     return true;
                  }
               }
            } finally {
               init_Z = false;
            }
         }

         if (init_I == 0 && !load_Z(requested)) {
            init_V(client);
         }

         init_Screen = requested;
         return false;
      } else {
         return false;
      }
   }

   public static boolean init_Z(Screen screen) {
      return load_Screen != null && load_Z && screen == read || init_CachePool.init_Z() && screen == init_CachePool.init_Obj() && !init_Z;
   }

   public static void load_V(Screen actual) {
      Screen retained = init_CachePool.init_Obj();
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

   public static void update() {
      Screen old = init_CachePool.load();
      Minecraft client = Minecraft.getInstance();
      if (old != null && (client == null || old != init_Screen(client))) {
         old.removed();
      }

      init_Screen = null;
   }
}
