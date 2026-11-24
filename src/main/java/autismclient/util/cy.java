package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.mixin.accessor.AutismKeyboardHandlerAccessor;
import autismclient.modules.Module;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.Level;

public final class cy {
   private static final String init_Str = "Unsaved UI transition draft";
   private static final JsonArray init_JsonArray = new JsonArray();
   private static boolean init_Z;
   private static int init_I;
   private static autismclient.gui.d.HashUtil init_HashUtil;
   private static Object init_Obj;
   private static autismclient.util.macro.FieldCache init_FieldCache;
   private static int load;

   private cy() {
   }

   public static void init_V() {
      if (Boolean.getBoolean("autism.ui.continuity.check")
         && FabricLoader.getInstance().getGameDir().toAbsolutePath().toString().contains("autism-ai-qualification-")) {
         ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!init_Z && mc.level != null && mc.player != null && mc.hasSingleplayerServer() && ++init_I >= 60) {
               init_Z = true;
               Thread worker = new Thread(() -> init_V_Minecraft(mc), "Autism-UiContinuityCheck");
               worker.setDaemon(true);
               worker.start();
            }
         });
      }
   }

   private static void init_V_Minecraft(Minecraft mc) {
      try {
         init_Obj_Minecraft_Supplier(mc, () -> {
            init_HashUtil = new autismclient.gui.d.HashUtil(null);
            cx.init_V(mc, init_HashUtil);
            AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
            editor.init_V_aM_Z(null, false);
            editor.init_V_Z(true);
            init_FieldCache = new autismclient.util.macro.FieldCache();
            ActionEditorOverlay action = ActionEditorOverlay.init_ActionEditorOverla_2();
            action.init_V_MacroAction(init_FieldCache, null, null);
            action.init_V_Z(true);
            init_Obj = init_HashUtil.init_ConfigStore();
            return true;
         });
         Thread.sleep(500L);
         init_Obj_Minecraft_Supplier(mc, () -> {
            AutismMacroEditorOverlay.init_AutismMacroEditorO_2().init_Str_Str_4("Unsaved UI transition draft");
            ActionEditorOverlay.init_ActionEditorOverla_2().init_CompoundTag_Str("13");
            return true;
         });
         init_V_Str_cy_DataUtil("teleport preserves editor and unsaved draft", () -> {
            init_V_Minecraft_StrArr(mc, "gamemode survival @a", "fill 12030 149 11995 12065 149 12005 stone", "tp @a 12040.5 150 12000.5");
            init_V_BooleanSupplier_Str(() -> init_Z_Minecraft_Supplier(mc, () -> mc.player != null && mc.player.getX() > 12039.0), "teleport");
            load(mc);
         });
         init_V_Str_cy_DataUtil(
            "death and respawn preserve editor and draft",
            () -> {
               init_V_Minecraft_StrArr(mc, "kill @a");
               init_V_BooleanSupplier_Str(() -> init_Z_Minecraft_Supplier(mc, () -> mc.player != null && mc.player.isDeadOrDying()), "death");
               load(mc);
               LocalPlayer old = init_Obj_Minecraft_Supplier(mc, () -> mc.player);
               init_Obj_Minecraft_Supplier(mc, () -> {
                  mc.player.respawn();
                  return true;
               });
               init_V_BooleanSupplier_Str(
                  () -> init_Z_Minecraft_Supplier(mc, () -> mc.player != null && mc.player != old && !mc.player.isDeadOrDying()), "respawn"
               );
               load(mc);
            }
         );
         init_V_Str_cy_DataUtil(
            "dimension change preserves editor and draft",
            () -> {
               init_V_Minecraft_StrArr(
                  mc, "execute in minecraft:the_nether run fill -2 99 -2 2 99 2 stone", "execute in minecraft:the_nether run tp @a 0.5 100 0.5"
               );
               init_V_BooleanSupplier_Str(() -> init_Z_Minecraft_Supplier(mc, () -> mc.level != null && mc.level.dimension().equals(Level.NETHER)), "nether");
               load(mc);
               init_V_Minecraft_StrArr(mc, "execute in minecraft:overworld run tp @a 12040.5 150 12000.5");
               init_V_BooleanSupplier_Str(
                  () -> init_Z_Minecraft_Supplier(mc, () -> mc.level != null && mc.level.dimension().equals(Level.OVERWORLD)), "overworld"
               );
               load(mc);
            }
         );
         init_V_Str_cy_DataUtil("disconnect preserves open windows while offline", () -> {
            init_Obj_Minecraft_Supplier(mc, () -> {
               mc.disconnect(new TitleScreen(), false);
               return true;
            });
            init_V_BooleanSupplier_Str(() -> init_Z_Minecraft_Supplier(mc, () -> mc.level == null && mc.getSingleplayerServer() == null), "disconnect");
            Thread.sleep(500L);
            load(mc);
         });
         init_V_Str_cy_DataUtil(
            "failed loopback reconnect keeps editor and network result separate",
            () -> {
               int port;
               try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
                  port = socket.getLocalPort();
               }

               String var7x = "127.0.0.1:" + port;
               init_Obj_Minecraft_Supplier(
                  mc,
                  () -> {
                     Module reconnect = autismclient.modules.al.init_Module_Str("auto-reconnect");
                     if (reconnect != null) {
                        reconnect.setEnabled(false);
                     }

                     ConnectScreen.startConnecting(
                        new TitleScreen(), mc, ServerAddress.parseString(var7x), new ServerData("Local UI refusal", var7x, Type.OTHER), false, null
                     );
                     return true;
                  }
               );
               init_V_BooleanSupplier_Str(() -> init_Z_Minecraft_Supplier(mc, () -> cx.init_Screen(mc) instanceof DisconnectedScreen), "loopback refusal");
               load(mc);
               init_V_Z_Str(init_Obj_Minecraft_Supplier(mc, () -> mc.gui.screen() != cx.init_Screen(mc)), "connection screen replaced foreground");
            }
         );
         init_V_Str_cy_DataUtil("local world reconnect preserves the same draft owners", () -> {
            read(mc);
            load(mc);
         });
         init_V_Str_cy_DataUtil("explicit close stays closed through a later reconnect", () -> {
            init_Obj_Minecraft_Supplier(mc, () -> {
               cx.init_V();

               try {
                  init_HashUtil.onClose();
               } finally {
                  cx.load_V();
               }

               return true;
            });
            init_V_Z_Str(init_Obj_Minecraft_Supplier(mc, () -> !cx.load_Z() && mc.gui.screen() != init_HashUtil), "manual close was blocked");
            init_Obj_Minecraft_Supplier(mc, () -> {
               mc.disconnect(new TitleScreen(), false);
               return true;
            });
            init_V_BooleanSupplier_Str(() -> init_Z_Minecraft_Supplier(mc, () -> mc.level == null && mc.getSingleplayerServer() == null), "second disconnect");
            read(mc);
            Thread.sleep(500L);
            init_V_Z_Str(init_Obj_Minecraft_Supplier(mc, () -> !cx.load_Z() && mc.gui.screen() != init_HashUtil), "closed editor reopened on join");
         });
         init_V_Str_cy_DataUtil(
            "inventory macro windows follow an automatic GUI close",
            () -> {
               init_Obj_Minecraft_Supplier(mc, () -> {
                  init_AutismMacroListOve_Minecraft(mc).init_V_Z(true);
                  AutismMacroEditorOverlay.init_AutismMacroEditorO_2().init_V_Z(true);
                  ActionEditorOverlay.init_ActionEditorOverla_2().init_V_Z(true);
                  return true;
               });
               Thread.sleep(250L);
               init_Obj_Minecraft_Supplier(mc, () -> {
                  AutismMacroEditorOverlay.init_AutismMacroEditorO_2().init_Str_Str_4("Unsaved UI transition draft");
                  ActionEditorOverlay.init_ActionEditorOverla_2().init_CompoundTag_Str("13");
                  mc.gui.setScreen(null);
                  return true;
               });
               Thread.sleep(350L);
               init_V_Z_Str(
                  init_Obj_Minecraft_Supplier(mc, () -> !cx.load_Z() && mc.gui.screen() == null && init_J_Str("autism-macrolist") == 0L),
                  "inventory windows survived their GUI close"
               );
            }
         );
         init_V_Str_cy_DataUtil("repeated inventory replacement never duplicates the open library", () -> {
            for (int i = 0; i < 5; i++) {
               init_Obj_Minecraft_Supplier(mc, () -> {
                  init_AutismMacroListOve_Minecraft(mc).init_V_Z(true);
                  mc.gui.setScreen(new InventoryScreen(mc.player));
                  init_V_Z_Str(!cx.load_Z() && init_J_Str("autism-macrolist") == 1L, "inventory replacement stacked library owners");
                  return true;
               });
               Thread.sleep(100L);
            }

            init_V_Minecraft_Str(mc, "inventory-single-library");
         });
         init_V_Str_cy_DataUtil("inventory resize keeps the same library owner", () -> init_Obj_Minecraft_Supplier(mc, () -> {
            AutismMacroListOverlay library = init_AutismMacroListOve_Minecraft(mc);
            library.init_V_Z(true);
            Screen inventory = mc.gui.screen();
            inventory.resize(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            init_V_Z_Str(library == init_AutismMacroListOve() && init_J_Str("autism-macrolist") == 1L, "resize rebuilt the library");
            return true;
         }));
         init_V_Str_cy_DataUtil("native inventory key closes its library without retaining a host", () -> init_Obj_Minecraft_Supplier(mc, () -> {
            init_AutismMacroListOve_Minecraft(mc).init_V_Z(true);
            init_V_Minecraft_I(mc, 69);
            init_V_Z_Str(mc.gui.screen() == null && !cx.load_Z() && init_J_Str("autism-macrolist") == 0L, "E left inventory windows open");
            return true;
         }));
         init_V_Str_cy_DataUtil("native Escape closes inventory without retaining a host", () -> init_Obj_Minecraft_Supplier(mc, () -> {
            init_AutismMacroListOve_Minecraft(mc).init_V_Z(true);
            init_V_Minecraft_I(mc, 256);
            init_V_Z_Str(mc.gui.screen() == null && !cx.load_Z() && init_J_Str("autism-macrolist") == 0L, "Escape left inventory windows open");
            return true;
         }));
         init_V_Str_cy_DataUtil(
            "a tied macro host registers only one library",
            () -> init_Obj_Minecraft_Supplier(
               mc,
               () -> {
                  AutismMacroListOverlay library = init_AutismMacroListOve_Minecraft(mc);
                  library.init_V_Z(true);
                  autismclient.gui.d.TaskTimer host = new autismclient.gui.d.TaskTimer(library, null, false, true);
                  cx.init_V(mc, host);
                  init_V_Z_Str(init_J_Str("autism-macrolist") == 1L && init_AutismMacroListOve() == library, "host duplicated its tied library");
                  Object windowSet = init_Obj_Obj_Class(host, ak.class);
                  init_V_Z_Str(
                     init_Obj_Obj_Class(windowSet, AutismMacroListOverlay.class) == library
                        && init_Obj_Obj_Class(init_Obj_Obj_Class(windowSet, AutismLauncherOverlay.class), AutismMacroListOverlay.class) == library,
                     "host launcher targets a different library owner"
                  );
                  mc.gui.setScreen(null);
                  init_V_Z_Str(!cx.load_Z() && init_J_Str("autism-macrolist") == 0L, "host retained its windows");
                  return true;
               }
            )
         );
         init_V_Str_cy_DataUtil(
            "module menu protects one library while a background inventory initializes",
            () -> init_Obj_Minecraft_Supplier(
               mc,
               () -> {
                  autismclient.gui.d.HashUtil moduleMenu = new autismclient.gui.d.HashUtil(null);
                  cx.init_V(mc, moduleMenu);
                  AutismMacroListOverlay library = init_AutismMacroListOve();
                  library.init_V_Z(true);
                  InventoryScreen inventory = new InventoryScreen(mc.player);
                  mc.gui.setScreen(inventory);
                  inventory.resize(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                  init_V_Z_Str(
                     mc.gui.screen() == moduleMenu && cx.load_Z() && init_J_Str("autism-macrolist") == 1L && init_AutismMacroListOve() == library,
                     "background inventory replaced or duplicated module windows"
                  );
                  cx.init_V();

                  try {
                     moduleMenu.onClose();
                  } finally {
                     cx.load_V();
                  }

                  init_V_Z_Str(
                     !cx.load_Z() && mc.gui.screen() == inventory && init_J_Str("autism-macrolist") == 1L,
                     "closing module menu did not activate inventory windows"
                  );
                  mc.gui.setScreen(null);
                  init_V_Z_Str(init_J_Str("autism-macrolist") == 0L, "revealed inventory kept its library after close");
                  return true;
               }
            )
         );
         init_V_Str_cy_DataUtil(
            "schematic library shares one window and remembers its state after disconnect",
            () -> {
               bk manager = bk.init_bk();
               cX schematic = init_Obj_Minecraft_Supplier(
                  mc,
                  () -> {
                     cx.init_V(mc, new autismclient.gui.d.HashUtil(null));
                     autismclient.ai.cV.read_V();
                     Optional<cX> existing = manager.init_List()
                        .stream()
                        .filter(overlay -> "schematic-library".equals(overlay.init_Str()) && overlay.sync_Z())
                        .findFirst();
                     if (existing.isEmpty()) {
                        autismclient.ai.cV.load_V();
                     }

                     cX library = init_cX();
                     init_V_Z_Str(library.sync_Z(), "module menu did not open the library");
                     library.write_V_Z(true);
                     init_AutismMacroListOve_Minecraft(mc);
                     init_V_Z_Str(
                        init_cX() == library && library.flush_Z() && library.sync_Z() && !manager.load_Z(library),
                        "inventory opened a different library or lost its state"
                     );
                     return library;
                  }
               );
               init_Obj_Minecraft_Supplier(mc, () -> {
                  mc.disconnect(new TitleScreen(), false);
                  return true;
               });
               init_V_BooleanSupplier_Str(
                  () -> init_Z_Minecraft_Supplier(mc, () -> mc.level == null && mc.getSingleplayerServer() == null), "schematic disconnect"
               );
               init_V_Z_Str(init_Obj_Minecraft_Supplier(mc, () -> {
                  cH saved = ck.init_ck().init_cH_Str("schematic-library");
                  return saved != null && saved.init_Z && saved.load_Z;
               }), "disconnect saved the library as closed");
               read(mc);
               init_Obj_Minecraft_Supplier(
                  mc,
                  () -> {
                     init_AutismMacroListOve_Minecraft(mc);
                     init_V_Z_Str(
                        init_cX() == schematic && schematic.sync_Z() && schematic.flush_Z() && !manager.load_Z(schematic),
                        "the library did not restore on reconnect"
                     );
                     schematic.write_V_Z(false);
                     return true;
                  }
               );
               init_V_Minecraft_Str(mc, "schematic-inventory");
               init_Obj_Minecraft_Supplier(
                  mc,
                  () -> {
                     autismclient.ai.cV.load_V();
                     cx.init_V(mc, new autismclient.gui.d.HashUtil(null));
                     init_V_Z_Str(
                        !schematic.sync_Z()
                           && manager.init_List().stream().noneMatch(overlay -> "schematic-library".equals(overlay.init_Str()) && overlay.sync_Z()),
                        "an explicitly closed library reopened in the other host"
                     );
                     mc.gui.setScreen(null);
                     return true;
                  }
               );
            }
         );
         init_V_Str_cy_DataUtil(
            "explicit HUD pin survives inventory close without retaining the inventory",
            () -> init_Obj_Minecraft_Supplier(
               mc,
               () -> {
                  init_AutismMacroListOve_Minecraft(mc);
                  AutismPacketLoggerOverlay logger = autismclient.modules.PoolEntry.init_PoolEntry().init_AutismPacketLogger();
                  logger.update_V_Z(true);
                  logger.init_V_Z(true);
                  bk.init_bk().init_V(logger);
                  mc.gui.setScreen(null);
                  init_V_Z_Str(
                     mc.gui.screen() == null && !cx.load_Z() && bk.init_bk().init_List().contains(logger) && logger.sync_Z(),
                     "explicit HUD pin was lost or retained its host"
                  );
                  logger.update_V_Z(false);
                  logger.init_V_Z(false);
                  bk.init_bk().load_V();
                  return true;
               }
            )
         );
         init_V_Str_cy_DataUtil(
            "ordinary settings follow disconnect and stay closed on reconnect",
            () -> {
               autismclient.gui.d.CacheEntry settings = init_Obj_Minecraft_Supplier(mc, () -> {
                  autismclient.gui.d.CacheEntry next = new autismclient.gui.d.CacheEntry(null);
                  cx.init_V(mc, next);
                  next.init_Str_Str("#13579B");
                  return next;
               });
               init_Obj_Minecraft_Supplier(mc, () -> {
                  mc.disconnect(new TitleScreen(), false);
                  return true;
               });
               init_V_BooleanSupplier_Str(
                  () -> init_Z_Minecraft_Supplier(mc, () -> mc.level == null && mc.getSingleplayerServer() == null), "settings disconnect"
               );
               init_V_Z_Str(init_Obj_Minecraft_Supplier(mc, () -> mc.gui.screen() != settings && !cx.load_Z()), "ordinary settings retained over disconnect");
               init_V_Minecraft_Str(mc, "settings-offline");
               read(mc);
               init_V_Z_Str(init_Obj_Minecraft_Supplier(mc, () -> !cx.load_Z() && mc.gui.screen() != settings), "discarded settings reopened");
            }
         );
      } catch (Throwable error) {
         JsonObject row = new JsonObject();
         row.addProperty("name", "setup");
         row.addProperty("pass", false);
         row.addProperty("error", error.toString());
         init_JsonArray.add(row);
         AutismClientAddon.LOG.warn("[UiContinuityCheck] setup failed", error);
      } finally {
         try {
            int failures = 0;

            for (JsonElement check : init_JsonArray) {
               if (!check.getAsJsonObject().get("pass").getAsBoolean()) {
                  failures++;
               }
            }

            JsonObject report = new JsonObject();
            report.addProperty("done", true);
            report.addProperty("pass", init_JsonArray.size() - failures);
            report.addProperty("fail", failures);
            report.add("checks", init_JsonArray);
            Path file = FabricLoader.getInstance().getGameDir().resolve("ui-continuity-check.json");
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(report));
            AutismClientAddon.LOG.info("[UiContinuityCheck] done pass={} fail={}", init_JsonArray.size() - failures, failures);
         } catch (Exception error) {
            AutismClientAddon.LOG.warn("[UiContinuityCheck] report failed", error);
         }
      }
   }

   private static long init_J_Str(String id) {
      return bk.init_bk().init_List().stream().filter(overlay -> id.equals(overlay.init_Str())).count();
   }

   private static cX init_cX() {
      init_V_Z_Str(init_J_Str("schematic-library") == 1L, "expected one schematic library");
      return bk.init_bk().init_List().stream().filter(overlay -> "schematic-library".equals(overlay.init_Str())).findFirst().orElseThrow();
   }

   private static AutismMacroListOverlay init_AutismMacroListOve() {
      return bk.init_bk()
         .init_List()
         .stream()
         .filter(AutismMacroListOverlay.class::isInstance)
         .map(AutismMacroListOverlay.class::cast)
         .findFirst()
         .orElseThrow();
   }

   private static AutismMacroListOverlay init_AutismMacroListOve_Minecraft(Minecraft mc) {
      cx.init_V(mc, new InventoryScreen(mc.player));
      AutismMacroEditorOverlay.init_AutismMacroEditorO_2().init_V_Z(false);
      ActionEditorOverlay.init_ActionEditorOverla_2().init_V_Z(false);
      return init_AutismMacroListOve();
   }

   private static void init_V_Minecraft_I(Minecraft mc, int key) {
      AutismKeyboardHandlerAccessor keys = (AutismKeyboardHandlerAccessor)mc.keyboardHandler;
      KeyEvent event = Compat263.newGlfwKeyEvent(key, 0, 0);
      keys.autism$invokeKeyPress(mc.getWindow().handle(), 1, event);
      keys.autism$invokeKeyPress(mc.getWindow().handle(), 0, event);
   }

   private static Object init_Obj_Obj_Class(Object owner, Class<?> type) {
      try {
         for (Field field : owner.getClass().getDeclaredFields()) {
            if (field.getType() == type) {
               field.setAccessible(true);
               return field.get(owner);
            }
         }

         throw new IllegalStateException("Missing hosted window owner: " + type);
      } catch (ReflectiveOperationException error) {
         throw new IllegalStateException(error);
      }
   }

   private static void init_V_Str_cy_DataUtil(String name, cy.Inner_DataUtil check) {
      JsonObject row = new JsonObject();
      row.addProperty("name", name);

      try {
         check.run();
         row.addProperty("pass", true);
      } catch (Throwable error) {
         row.addProperty("pass", false);
         row.addProperty("error", error.toString());
         AutismClientAddon.LOG.warn("[UiContinuityCheck] {} failed", name, error);
      }

      init_JsonArray.add(row);
   }

   private static void load(Minecraft mc) throws InterruptedException, ExecutionException, TimeoutException {
      Thread.sleep(250L);
      init_V_Z_Str(
         init_Obj_Minecraft_Supplier(mc, () -> mc.gui.screen() == init_HashUtil && init_HashUtil.init_ConfigStore() == init_Obj), "screen/controller rebuilt"
      );
      init_V_Z_Str(
         init_Obj_Minecraft_Supplier(
            mc,
            () -> AutismMacroEditorOverlay.init_AutismMacroEditorO_2().sync_Z()
               && !bk.init_bk().load_Z(AutismMacroEditorOverlay.init_AutismMacroEditorO_2())
               && "Unsaved UI transition draft".equals(AutismMacroEditorOverlay.init_AutismMacroEditorO_2().init_Str_Str_4(null))
         ),
         "macro draft hidden/reset"
      );
      init_V_Z_Str(
         init_Obj_Minecraft_Supplier(
            mc,
            () -> ActionEditorOverlay.init_ActionEditorOverla_2().sync_Z()
               && !bk.init_bk().load_Z(ActionEditorOverlay.init_ActionEditorOverla_2())
               && ActionEditorOverlay.init_ActionEditorOverla_2().init_CompoundTag_Str(null).getIntOr("thenSteps", 0) == 13
               && init_FieldCache.thenSteps == 1
         ),
         "action draft hidden/reset/committed"
      );
      init_V_Minecraft_Str(mc, "macro-preserved-" + ++load);
   }

   private static void init_V_Minecraft_Str(Minecraft mc, String name) throws InterruptedException, ExecutionException, TimeoutException {
      init_Obj_Minecraft_Supplier(mc, () -> {
         File directory = FabricLoader.getInstance().getGameDir().resolve("ui-continuity-shots").toFile();
         directory.mkdirs();
         Screenshot.grab(directory, name + ".png", mc.gameRenderer.mainRenderTarget(), 1, message -> {});
         return true;
      });
   }

   private static void read(Minecraft mc) throws InterruptedException, ExecutionException, TimeoutException {
      init_Obj_Minecraft_Supplier(mc, () -> {
         mc.createWorldOpenFlows().openWorld(System.getProperty("autism.ui.continuity.world", "AI Local UI World"), () -> {});
         return true;
      });
      init_V_BooleanSupplier_Str(
         () -> init_Z_Minecraft_Supplier(mc, () -> mc.level != null && mc.player != null && mc.hasSingleplayerServer()), "local world reopen"
      );
      Thread.sleep(500L);
   }

   private static void init_V_Minecraft_StrArr(Minecraft mc, String... commands) throws InterruptedException, ExecutionException, TimeoutException {
      IntegratedServer server = init_Obj_Minecraft_Supplier(mc, mc::getSingleplayerServer);
      CompletableFuture<Boolean> future = new CompletableFuture<>();
      server.execute(() -> {
         try {
            for (String command : commands) {
               server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
            }

            future.complete(true);
         } catch (Throwable error) {
            future.completeExceptionally(error);
         }
      });
      future.get(15L, TimeUnit.SECONDS);
   }

   private static void init_V_BooleanSupplier_Str(BooleanSupplier ready, String what) throws InterruptedException {
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60L);

      while (!ready.getAsBoolean()) {
         if (System.nanoTime() > deadline) {
            throw new IllegalStateException("Timed out: " + what);
         }

         Thread.sleep(25L);
      }
   }

   private static boolean init_Z_Minecraft_Supplier(Minecraft mc, Supplier<Boolean> action) {
      try {
         return init_Obj_Minecraft_Supplier(mc, action);
      } catch (Exception error) {
         throw new IllegalStateException(error);
      }
   }

   private static <T> T init_Obj_Minecraft_Supplier(Minecraft mc, Supplier<T> action) throws InterruptedException, ExecutionException, TimeoutException {
      CompletableFuture<T> future = new CompletableFuture<>();
      mc.execute(() -> {
         try {
            future.complete(action.get());
         } catch (Throwable error) {
            future.completeExceptionally(error);
         }
      });
      return future.get(15L, TimeUnit.SECONDS);
   }

   private static void init_V_Z_Str(boolean condition, String message) {
      if (!condition) {
         throw new IllegalStateException(message);
      }
   }

   @FunctionalInterface
   private interface Inner_DataUtil {
      void run() throws Exception;
   }
}
