package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismMouseHandlerAccessor;
import autismclient.mixin.accessor.AutismWindowAccessor;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ClientLevel;

public final class aX {
   private static final Deque<aX.Inner_DataUtil> init_Deque = new ArrayDeque<>();
   private static final JsonArray init_JsonArray = new JsonArray();
   private static final JsonArray load_JsonArray = new JsonArray();
   private static int init_I;
   private static int load_I;
   private static int read;
   private static int write;
   private static int update;
   private static int sync;
   private static int flush;
   private static boolean init_Z;
   private static boolean load_Z;
   private static Screen init_Screen;
   private static String init_Str;
   private static int close;
   private static int open;
   private static int poll;
   private static int push;
   private static int pop;

   private aX() {
   }

   public static void init_V() {
      if (Boolean.getBoolean("autism.minimize.check")) {
         ClientTickEvents.END_CLIENT_TICK.register(aX::init_V_Minecraft);
      }
   }

   private static void init_V_Minecraft(Minecraft mc) {
      if (flush != 3 && mc.getWindow() != null) {
         if (init_Z) {
            write(mc);
         }

         if (flush != 0 && flush != 2) {
            aX.Inner_DataUtil step = init_Deque.peekFirst();
            if (step != null) {
               if (++init_I >= step.init_I()) {
                  init_Deque.pollFirst();
                  init_I = 0;
                  init_V_Str_ObjArr("%s", step.init_Str());

                  try {
                     step.init_Consumer().accept(mc);
                  } catch (Throwable t) {
                     load_JsonArray.add(step.init_Str() + ": " + t);
                     AutismClientAddon.LOG.error("[MinimizeCheck] FAIL " + step.init_Str(), t);
                  }
               }
            } else if (!load_Z) {
               flush = 2;
            } else {
               flush(mc);
            }
         } else {
            Screen screen = mc.gui.screen();
            boolean onTitle = mc.level == null && screen != null && screen.getClass().getSimpleName().contains("TitleScreen");
            boolean inWorld = flush == 0 && mc.level != null && mc.player != null && screen == null;
            if (!onTitle && !inWorld) {
               load_I = 0;
            } else if (++load_I >= 100) {
               load_I = 0;
               flush = 1;
               if (inWorld) {
                  load();
               } else {
                  init_Screen = screen;
                  read();
               }
            }
         }
      }
   }

   private static void load() {
      init_V_I(0, "world: minimize (the pause menu opens on the lost focus)", aX::read);
      init_V_I(200, "world: restore", aX::update);
      init_V_Str("world");
      init_V_I(20, "world: back to the game", mc -> mc.gui.setScreen(null));
      init_V_I(20, "world: PASS", mc -> init_JsonArray.add("world"));
      cX[] launcher = new cX[1];
      init_V_I(20, "inventory: open", mc -> mc.gui.setScreen(new InventoryScreen(mc.player)));
      init_V_I(40, "inventory: find the launcher", mc -> {
         for (cX overlay : bk.init_bk().init_List()) {
            if (overlay instanceof AutismLauncherOverlay && overlay.sync_Z()) {
               launcher[0] = overlay;
            }
         }

         if (launcher[0] == null) {
            throw new IllegalStateException("no launcher window in the inventory");
         }
      });
      init_V_Str_cXArr("inventory launcher", launcher);
      init_V_I(10, "inventory: minimize open", aX::read);
      init_V_I(60, "inventory: restore", aX::update);
      init_V_Str("inventory");
      init_V_I(20, "inventory: close", mc -> mc.gui.setScreen(null));
      init_V_I(20, "inventory: PASS", mc -> init_JsonArray.add("inventory"));
      init_V_Str_2("world", () -> null, autismclient.gui.d.HashUtil.Inner_DataUtil.init);
      init_V_I(20, "pause menu: open", mc -> mc.pauseGame(false));
      init_V_I(40, "pause menu: minimize", aX::read);
      init_V_I(100, "pause menu: restore", aX::update);
      init_V_Str("pause menu");
      init_V_I(20, "pause menu: close", mc -> mc.gui.setScreen(null));
      init_V_I(20, "pause menu: PASS", mc -> init_JsonArray.add("pause menu"));
      init_V_I(20, "world: leave for the main menu", mc -> mc.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE));
   }

   private static void read() {
      load_Z = true;
      write();

      for (String raw : System.getProperty("autism.minimize.check.scales", "").split(",")) {
         if (!raw.isBlank()) {
            int scale = Integer.parseInt(raw.trim());
            init_V_I(20, "gui scale " + scale + " (framebuffer 1920x1080)", mc -> init_V_Minecraft_I(mc, scale));
            write();
            init_V_I(20, "gui scale " + scale + ": back", aX::load);
         }
      }
   }

   private static void init_V_Minecraft_I(Minecraft mc, int scale) {
      if (close == 0) {
         close = mc.getWindow().getWidth();
         open = mc.getWindow().getHeight();
         poll = mc.options.guiScale().get();
      }

      mc.getWindow().setWidth(1920);
      mc.getWindow().setHeight(1080);
      mc.options.guiScale().set(scale);
      mc.framebufferSizeChanged();
      init_V_Str_ObjArr("gui scale now %d (%dx%d gui)", mc.getWindow().getGuiScale(), mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
   }

   private static void load(Minecraft mc) {
      if (close != 0) {
         mc.getWindow().setWidth(close);
         mc.getWindow().setHeight(open);
         mc.options.guiScale().set(poll);
         open = 0;
         close = 0;
         mc.framebufferSizeChanged();
      }
   }

   private static void write() {
      init_V_I(0, "title: minimize", aX::read);
      init_V_I(200, "title: restore", aX::update);
      init_V_Str("title");
      init_V_I(60, "title: PASS", mc -> init_JsonArray.add("title"));
      init_V_Str_2("title", () -> init_Screen, autismclient.gui.d.HashUtil.Inner_DataUtil.load);
      init_V_Str("matchmaking", m -> m.init_cX(), o -> ((AutismMatchmakingOverlay)o).load(true));
      init_V_Str("profiles", m -> m.load_cX(), o -> ((AutismProfilesOverlay)o).load(true));
   }

   private static void init_V_Str_2(String where, Supplier<Screen> parent, autismclient.gui.d.HashUtil.Inner_DataUtil mode) {
      for (String style : new String[]{"DEFAULT", "CSGO"}) {
         String menu = where + " modules " + style;
         init_V_I(
            20,
            menu + ": open",
            mc -> {
               if (init_Str == null) {
                  init_Str = AutismConfig.getGlobal().moduleMenuStyle;
               }

               AutismConfig.getGlobal().moduleMenuStyle = style;
               mc.gui
                  .setScreen(
                     mode == autismclient.gui.d.HashUtil.Inner_DataUtil.load
                        ? new autismclient.gui.d.HashUtil(parent.get(), mode)
                        : new autismclient.gui.d.HashUtil(parent.get())
                  );
            }
         );
         init_V_I(60, menu + ": collapse every window", mc -> {
            if (mc.gui.screen() instanceof autismclient.gui.d.HashUtil screen && screen.init_ConfigStore() != null) {
               Map<String, autismclient.gui.f.NetHelper> headers = screen.init_ConfigStore().init_Map();
               if (headers.isEmpty()) {
                  throw new IllegalStateException("module menu shows no window headers");
               }

               List<aX.Inner_DataUtil> steps = new ArrayList<>();

               for (String id : headers.keySet()) {
                  String name = menu + " window '" + id + "'";
                  steps.add(new aX.Inner_DataUtil(5, name + ": click to collapse", m -> init_V_Minecraft_Str(m, id)));
                  steps.add(new aX.Inner_DataUtil(30, name + ": collapsed?", m -> init_V_Minecraft_3(m, id, true)));
                  steps.add(new aX.Inner_DataUtil(1, name + ": minimize collapsed", aX::read));
                  steps.add(new aX.Inner_DataUtil(60, name + ": restore", aX::update));
                  steps.add(new aX.Inner_DataUtil(10, name + ": shrink collapsed to 160x28", m -> init_V_Minecraft_2(m, 160, 28)));
                  steps.add(new aX.Inner_DataUtil(30, name + ": size back", aX::sync));
                  steps.add(new aX.Inner_DataUtil(20, name + ": click to expand", m -> init_V_Minecraft_Str(m, id)));
                  steps.add(new aX.Inner_DataUtil(30, name + ": expanded?", m -> init_V_Minecraft_3(m, id, false)));
                  steps.add(new aX.Inner_DataUtil(1, name + ": PASS", m -> init_JsonArray.add(name)));
               }

               for (int i = steps.size() - 1; i >= 0; i--) {
                  init_Deque.addFirst(steps.get(i));
               }
            } else {
               throw new IllegalStateException("module menu did not open");
            }
         });
         init_V_I(10, menu + ": minimize open", aX::read);
         init_V_I(60, menu + ": restore", aX::update);
         init_V_Str(menu);
         init_V_I(20, menu + ": close", mc -> mc.gui.setScreen(parent.get()));
      }

      init_V_I(20, "menu style back", mc -> {
         if (init_Str != null) {
            AutismConfig.getGlobal().moduleMenuStyle = init_Str;
         }
      });
   }

   private static void init_V_Str(String name, Function<autismclient.modules.PoolEntry, cX> pick, Consumer<cX> mainMenuMode) {
      cX[] open = new cX[1];
      init_V_I(20, name + ": open", mc -> {
         autismclient.modules.PoolEntry mod = autismclient.modules.PoolEntry.init_PoolEntry();
         cX overlay = mod == null ? null : pick.apply(mod);
         if (overlay == null) {
            throw new IllegalStateException(name + " overlay missing");
         }

         bk.init_bk().init_V(overlay);
         mainMenuMode.accept(overlay);
         overlay.init_V_Z(true);
         open[0] = overlay;
         mc.gui.setScreen(new autismclient.gui.d.TaskTimer(overlay, init_Screen, true));
      });
      init_V_I(40, name + ": minimize", aX::read);
      init_V_I(60, name + ": restore", aX::update);
      init_V_Str(name);
      init_V_I(20, name + ": close", mc -> {
         if (open[0] != null) {
            open[0].init_V_Z(false);
         }

         mc.gui.setScreen(init_Screen);
      });
      init_V_I(20, name + ": PASS", mc -> init_JsonArray.add(name));
   }

   private static void init_V_Str_cXArr(String name, cX[] window) {
      init_V_I(5, name + ": click to collapse", mc -> init_V_Minecraft_cX(mc, window[0]));
      init_V_I(30, name + ": collapsed?", mc -> {
         if (window[0] == null || !window[0].flush_Z()) {
            throw new IllegalStateException("the header click did not collapse it");
         }
      });
      init_V_I(1, name + ": minimize collapsed", aX::read);
      init_V_I(60, name + ": restore", aX::update);
      init_V_I(10, name + ": shrink collapsed to 160x28", mc -> init_V_Minecraft_2(mc, 160, 28));
      init_V_I(30, name + ": size back", aX::sync);
      init_V_I(20, name + ": click to expand", mc -> init_V_Minecraft_cX(mc, window[0]));
      init_V_I(30, name + ": expanded?", mc -> {
         if (window[0] == null || window[0].flush_Z()) {
            throw new IllegalStateException("the header click did not expand it");
         }
      });
      init_V_I(1, name + ": PASS", mc -> init_JsonArray.add(name));
   }

   private static void init_V_Str(String name) {
      init_V_I(10, name + ": shrink to 160x28", mc -> init_V_Minecraft_2(mc, 160, 28));
      init_V_I(40, name + ": shrink to 1x1", mc -> init_V_Minecraft_2(mc, 1, 1));
      init_V_I(40, name + ": shrink to 320x40", mc -> init_V_Minecraft_2(mc, 320, 40));
      init_V_I(40, name + ": shrink to 40x320", mc -> init_V_Minecraft_2(mc, 40, 320));
      if (Boolean.getBoolean("autism.minimize.check.zero")) {
         init_V_I(40, name + ": shrink to 160x0", mc -> init_V_Minecraft_2(mc, 160, 0));
         init_V_I(40, name + ": shrink to 0x28", mc -> init_V_Minecraft_2(mc, 0, 28));
         init_V_I(40, name + ": size back", aX::sync);
         init_V_I(40, name + ": callback 160x0", mc -> load(mc, 160, 0));
         init_V_I(40, name + ": callback 0x28", mc -> load(mc, 0, 28));
         init_V_I(40, name + ": callback 160x1", mc -> load(mc, 160, 1));
         init_V_I(40, name + ": callback back to the real size", mc -> load(mc, push, pop));
      }

      init_V_I(40, name + ": size back", aX::sync);
   }

   private static void init_V_I(int delay, String label, Consumer<Minecraft> action) {
      init_Deque.addLast(new aX.Inner_DataUtil(delay, label, action));
   }

   private static void init_V_Minecraft_Str(Minecraft mc, String id) {
      if (mc.gui.screen() instanceof autismclient.gui.d.HashUtil screen && screen.init_ConfigStore() != null) {
         autismclient.gui.f.NetHelper header = screen.init_ConfigStore().init_Map().get(id);
         if (header == null) {
            throw new IllegalStateException("window '" + id + "' has no header any more");
         }

         init_V_Minecraft(mc, header.read() + Math.min(32.0, header.update() / 2.0), header.write() + header.sync() / 2.0);
      } else {
         throw new IllegalStateException("module menu closed");
      }
   }

   private static void init_V_Minecraft_cX(Minecraft mc, cX overlay) {
      if (overlay == null) {
         throw new IllegalStateException("overlay closed");
      }

      cH bounds = overlay.init_cH();
      init_V_Minecraft(mc, bounds.init_I + bounds.read / 2.0, bounds.load_I + 5.0);
   }

   private static void init_V_Minecraft(Minecraft mc, double virtualX, double virtualY) {
      Window window = mc.getWindow();
      double scale = cA.init_F();
      AutismMouseHandlerAccessor mouse = (AutismMouseHandlerAccessor)mc.mouseHandler;
      mouse.autism$setXpos(virtualX * scale * window.getScreenWidth() / window.getGuiScaledWidth());
      mouse.autism$setYpos(virtualY * scale * window.getScreenHeight() / window.getGuiScaledHeight());
      MouseButtonInfo left = Compat263.newGlfwMouseButtonInfo(0, 0);
      mouse.autism$invokeOnButton(window.handle(), left, 1);
      mouse.autism$invokeOnButton(window.handle(), left, 0);
   }

   private static void init_V_Minecraft_3(Minecraft mc, String id, boolean collapsed) {
      if (!(mc.gui.screen() instanceof autismclient.gui.d.HashUtil screen && screen.init_ConfigStore() != null)) {
         throw new IllegalStateException("module menu closed");
      } else {
         if (screen.init_ConfigStore().init_Z_Str(id) != collapsed) {
            throw new IllegalStateException("the header click did not " + (collapsed ? "collapse" : "expand") + " it");
         }
      }
   }

   private static void read(Minecraft mc) {
      read = mc.getWindow().getScreenWidth();
      write = mc.getWindow().getScreenHeight();
      AutismMouseHandlerAccessor mouse = (AutismMouseHandlerAccessor)mc.mouseHandler;
      mouse.autism$setXpos(read / 2.0);
      mouse.autism$setYpos(write / 2.0);
      init_Z = true;
      write(mc);
   }

   private static void write(Minecraft mc) {
      AutismWindowAccessor window = (AutismWindowAccessor)(Object)mc.getWindow();
      window.autism$setScreenWidth(0);
      window.autism$setScreenHeight(0);
      Compat263.noopBool(window, true);
      window.autism$setIconified(true);
      window.autism$setFocused(false);
   }

   private static void update(Minecraft mc) {
      init_Z = false;
      AutismWindowAccessor window = (AutismWindowAccessor)(Object)mc.getWindow();
      window.autism$setScreenWidth(read);
      window.autism$setScreenHeight(write);
      Compat263.noopBool(window, false);
      window.autism$setIconified(false);
      window.autism$setFocused(true);
      mc.resizeGui();
   }

   private static void init_V_Minecraft_2(Minecraft mc, int w, int h) {
      if (update == 0) {
         update = mc.getWindow().getWidth();
         sync = mc.getWindow().getHeight();
      }

      mc.getWindow().setWidth(w);
      mc.getWindow().setHeight(h);
      mc.framebufferSizeChanged();
   }

   private static void load(Minecraft mc, int w, int h) {
      if (push == 0) {
         push = mc.getWindow().getWidth();
         pop = mc.getWindow().getHeight();
      }

      int restoreW = push;
      int restoreH = pop;
      Compat263.windowFramebufferResize(mc.getWindow(), mc.getWindow().handle(), w, h);
      if (w == restoreW && h == restoreH) {
         pop = 0;
         push = 0;
      }
   }

   private static void sync(Minecraft mc) {
      if (update != 0) {
         mc.getWindow().setWidth(update);
         mc.getWindow().setHeight(sync);
         sync = 0;
         update = 0;
         mc.framebufferSizeChanged();
      }
   }

   private static void flush(Minecraft mc) {
      flush = 3;
      JsonObject report = new JsonObject();
      report.addProperty("result", load_JsonArray.isEmpty() ? "PASS" : "FAIL");
      report.add("passed", init_JsonArray);
      report.add("failed", load_JsonArray);

      try {
         Files.writeString(
            mc.gameDirectory.toPath().resolve("minimize-check.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report), StandardCharsets.UTF_8
         );
      } catch (IOException e) {
         init_V_Str_ObjArr("report not written: %s", e);
      }

      init_V_Str_ObjArr(
         "%s (%d passed, %d failed, %d draws clipped to nothing skipped)",
         load_JsonArray.isEmpty() ? "PASS" : "FAIL",
         init_JsonArray.size(),
         load_JsonArray.size(),
         autismclient.gui.e.TaskQueue.init_I
      );
   }

   private static void init_V_Str_ObjArr(String format, Object... args) {
      AutismClientAddon.LOG.info("[MinimizeCheck] " + String.format(Locale.ROOT, format, args));
   }

   private record Inner_DataUtil(int init_I, String init_Str, Consumer<Minecraft> init_Consumer) {
   }
}
