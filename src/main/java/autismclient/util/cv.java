package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.a;
import autismclient.util.macro.d;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public final class cv {
   private static boolean init_Z;
   private static int init_I;
   private static final JsonArray init_JsonArray = new JsonArray();

   private cv() {
   }

   public static void init_V() {
      if (Boolean.getBoolean("autism.tools.macro.check")) {
         Path dir = FabricLoader.getInstance().getGameDir().toAbsolutePath();
         if (dir.toString().contains("autism-ai-qualification-")) {
            ClientTickEvents.END_CLIENT_TICK.register(mc -> {
               if (!init_Z && mc.player != null && mc.level != null && mc.hasSingleplayerServer() && ++init_I >= 60) {
                  init_Z = true;
                  Thread worker = new Thread(() -> init_V_Minecraft(mc), "Autism-ToolsMacroCheck");
                  worker.setDaemon(true);
                  worker.start();
               }
            });
         }
      }
   }

   private static void init_V_Str_cv_DataUtil(String name, cv.Inner_DataUtil test) {
      JsonObject row = new JsonObject();
      row.addProperty("name", name);

      try {
         test.run();
         row.addProperty("pass", true);
      } catch (Throwable error) {
         row.addProperty("pass", false);
         row.addProperty("error", error.toString());
         AutismClientAddon.LOG.warn("[ToolsMacroCheck] {} failed", name, error);

         try {
            init_Obj_Minecraft_Supplier(Minecraft.getInstance(), () -> {
               MacroExecutor.sync_V();
               autismclient.ai.o.init_V("qualification cleanup");
               return true;
            });
         } catch (Exception var5) {
         }
      }

      init_JsonArray.add(row);
   }

   private static void init_V_Minecraft(Minecraft mc) {
      try {
         load(mc);
         init_V_Str_cv_DataUtil("client-thread controller ownership", () -> init_Obj_Minecraft_Supplier(mc, () -> {
            try (AutoCloseable input = MacroExecutor.init_AutoCloseable_Collection(List.of("INPUT", "ROTATION"))) {
               init_V_Z_Str(input != null, "first controller rejected");
               init_V_Z_Str(MacroExecutor.init_AutoCloseable_Collection(List.of("INPUT")) == null, "same-thread re-entry accepted");

               try (AutoCloseable chat = MacroExecutor.init_AutoCloseable_Collection(List.of("NETWORK"))) {
                  init_V_Z_Str(chat != null, "independent chat blocked");
               }
            } catch (Exception error) {
               throw new IllegalStateException(error);
            }

            return true;
         }));
         init_V_Str_cv_DataUtil("chat macro progresses during direct travel", () -> {
            autismclient.util.macro.k.Inner_StreamUtil travel = init_k_StreamUtil_Minecraft_I(mc, 12030);
            init_V_BooleanSupplier_Str(() -> travel.init_CompletableFuture().isDone(), "travel start");
            long run = init_J_Minecraft(mc, "tools-chat", init_W_Str("Tools macro concurrency check"), new autismclient.util.macro.EventBus(300));
            init_V_J(run);
            init_V_Z_Str(!travel.load().isDone(), "travel should still be running while chat macro completes");
            travel.load().get(30L, TimeUnit.SECONDS);
            init_V_Z_Str(travel.init_k_CachePool().init_Z(), travel.init_k_CachePool().init_Str());
         });
         init_V_Str_cv_DataUtil(
            "continue, running condition, wait, success condition",
            () -> {
               autismclient.util.macro.al go = init_al_I_Str(12060, "CONTINUE");
               autismclient.util.macro.FieldCache running = init_FieldCache_Str("RUNNING");
               autismclient.util.macro.FieldCache success = init_FieldCache_Str("SUCCEEDED");
               autismclient.util.macro.TableIndex fail = new autismclient.util.macro.TableIndex();
               fail.target = autismclient.util.macro.TableIndex.Inner_StreamUtil.flush;
               long id = init_J_Minecraft(
                  mc,
                  "tools-continue",
                  go,
                  running,
                  init_W_Str("Tools running"),
                  fail,
                  init_al_Str("WAIT"),
                  success,
                  init_W_Str("Tools succeeded"),
                  fail,
                  new autismclient.util.macro.EventBus(0)
               );
               init_V_BooleanSupplier_Str(() -> autismclient.util.macro.k.load(id, "THIS_MACRO", "") != null, "owned invocation");
               autismclient.util.macro.k.Inner_StreamUtil job = autismclient.util.macro.k.load(id, "THIS_MACRO", "");
               init_V_BooleanSupplier_Str(() -> job.init_CompletableFuture().isDone(), "continue start");
               init_V_Z_Str(!job.load().isDone(), "continue waited for completion");
               init_V_J(id);
               init_V_Z_Str(MacroExecutor.init_MacroExecutor_Macr_J(id).lastCompletedStep() == 9, "Tools condition selected the wrong branch");
               init_V_Z_Str(job.init_k_CachePool().init_Z(), job.init_k_CachePool().init_Str());
            }
         );
         init_V_Str_cv_DataUtil("conflicting macro input waits without freezing client", () -> {
            autismclient.util.macro.k.Inner_StreamUtil travel = init_k_StreamUtil_Minecraft_I(mc, 12085);
            init_V_BooleanSupplier_Str(() -> travel.init_CompletableFuture().isDone(), "travel start");
            long id = init_J_Minecraft(mc, "tools-input", new autismclient.util.macro.ad(false), init_W_Str("Input released to macro"));
            Thread.sleep(300L);
            init_V_Z_Str(MacroExecutor.init_Z_J(id), "conflicting macro did not wait");
            travel.load().get(30L, TimeUnit.SECONDS);
            init_V_J(id);
         });
         init_V_Str_cv_DataUtil("stopping a macro cancels its tool and releases controls", () -> {
            long id = init_J_Minecraft(mc, "tools-cancel", init_al_I_Str(12010, "CONTINUE"), new autismclient.util.macro.EventBus(30000));
            init_V_BooleanSupplier_Str(() -> autismclient.util.macro.k.load(id, "THIS_MACRO", "") != null, "owned invocation");
            autismclient.util.macro.k.Inner_StreamUtil job = autismclient.util.macro.k.load(id, "THIS_MACRO", "");
            init_V_BooleanSupplier_Str(() -> job.init_CompletableFuture().isDone(), "tool start");
            init_Obj_Minecraft_Supplier(mc, () -> {
               MacroExecutor.init_V_J(id);
               return true;
            });
            job.load().get(10L, TimeUnit.SECONDS);
            init_V_Z_Str(job.init_k_CachePool().init_k_ConfigStore() == autismclient.util.macro.k.Inner_ConfigStore.flush, "tool survived macro stop");
            init_V_Z_Str(!MacroExecutor.init_Z_DataPipe_DataUtil(autismclient.util.macro.DataPipe.Inner_DataUtil.read), "input lease leaked");
         });
         init_V_Str_cv_DataUtil(
            "pause, resume and intentional stop continue the macro",
            () -> {
               long id = init_J_Minecraft(
                  mc,
                  "tools-control",
                  init_al_I_Str(12010, "CONTINUE"),
                  init_al_Str("PAUSE"),
                  new autismclient.util.macro.EventBus(500),
                  init_al_Str("RESUME"),
                  new autismclient.util.macro.EventBus(400),
                  init_al_Str("STOP"),
                  init_W_Str("Tool stopped intentionally")
               );
               init_V_BooleanSupplier_Str(() -> autismclient.util.macro.k.init(id, "THIS_MACRO", "", "PAUSED"), "paused state");
               double x = init_Obj_Minecraft_Supplier(mc, () -> mc.player.getX());
               Thread.sleep(180L);
               init_V_Z_Str(Math.abs(init_Obj_Minecraft_Supplier(mc, () -> mc.player.getX()) - x) < 0.4, "paused tool kept moving");
               init_V_J(id);
            }
         );
         init_V_Str_cv_DataUtil(
            "read-only tool cleanup preserves macro movement",
            () -> {
               autismclient.util.macro.s move = new autismclient.util.macro.s(autismclient.util.macro.s.Inner_DataUtil.init, 40);
               move.nonBlocking = true;
               long id = init_J_Minecraft(mc, "tools-readonly", move, new autismclient.util.macro.EventBus(2200));
               init_V_BooleanSupplier_Str(() -> init_Z_Minecraft_Supplier(mc, () -> mc.options.keyUp.isDown()), "macro movement");
               autismclient.util.macro.k.Inner_StreamUtil read = init_Obj_Minecraft_Supplier(
                  mc, () -> autismclient.ai.o.init_k_StreamUtil("client:waypoints", new JsonObject())
               );
               read.load().get(10L, TimeUnit.SECONDS);
               init_V_Z_Str(read.init_k_CachePool().init_Z(), read.init_k_CachePool().init_Str());
               init_V_Z_Str(init_Obj_Minecraft_Supplier(mc, () -> mc.options.keyUp.isDown()), "read-only tool cleared macro forward input");
               init_V_J(id);
            }
         );
         init_V_Str_cv_DataUtil(
            "tools launched from a command-launched macro do not deadlock",
            () -> {
               long independent = init_J_Minecraft(mc, "tools-independent-launch", init_W_Str("Independent macro"), new autismclient.util.macro.EventBus(1600));
               aM macro = new aM("Tools qualification nested");
               macro.init_List.add(init_al_I_Str(12040, "WAIT"));
               init_Obj_Minecraft_Supplier(mc, () -> {
                  aN.init_aN().init_V_aM(macro);
                  return true;
               });
               JsonObject args = new JsonObject();
               args.addProperty("name", macro.init_Str);
               args.addProperty("timeoutSeconds", 30);
               autismclient.util.macro.k.Inner_StreamUtil outer = init_Obj_Minecraft_Supplier(
                  mc, () -> autismclient.ai.o.init_k_StreamUtil("client:run_macro", args)
               );
               autismclient.util.macro.k.Inner_CachePool outcome = outer.load().get(35L, TimeUnit.SECONDS);
               init_V_Z_Str(outcome.init_Z(), outcome.init_Str());
               init_V_J(independent);
               init_Obj_Minecraft_Supplier(mc, () -> {
                  aN.init_aN().load(macro);
                  return true;
               });
            }
         );
         init_V_Str_cv_DataUtil("typed tool arguments and coordinates resolve macro variables", () -> {
            autismclient.util.macro.o variables = new autismclient.util.macro.o();
            variables.names.addAll(List.of("toolsTimeout", "toolsX"));
            variables.values.addAll(List.of("30", "12045"));
            autismclient.util.macro.al go = init_al_I_Str(12099, "WAIT");
            d.init_V(go, "toolArg.timeoutSeconds", "{toolsTimeout}");
            d.init_V(go, "x", "{toolsX}");
            long id = init_J_Minecraft(mc, "tools-variables", variables, go, new autismclient.util.macro.EventBus(250), init_W_Str("Tool variables resolved"));
            init_V_BooleanSupplier_Str(() -> autismclient.util.macro.k.load(id, "THIS_MACRO", "") != null, "variable tool invocation");
            autismclient.util.macro.k.Inner_StreamUtil job = autismclient.util.macro.k.load(id, "THIS_MACRO", "");
            init_V_J(id);
            init_V_Z_Str(job.init_k_CachePool().init_Z(), job.init_k_CachePool().init_Str());
            init_V_Z_Str(Math.abs(init_Obj_Minecraft_Supplier(mc, () -> mc.player.getX()) - 12045.5) < 1.1, "coordinate variable was ignored");
         });
         init_V_Str_cv_DataUtil("invalid tool arguments fail and stop the waiting macro", () -> {
            autismclient.util.macro.al invalid = new autismclient.util.macro.al();
            invalid.toolId = "game:mine";
            invalid.argumentsJson = "{}";
            long id = init_J_Minecraft(mc, "tools-invalid", invalid, init_W_Str("must not execute"));
            init_V_BooleanSupplier_Str(() -> MacroExecutor.init_MacroExecutor_Macr_J(id) != null, "failed macro outcome");
            init_V_Z_Str("FAILED".equals(MacroExecutor.init_MacroExecutor_Macr_J(id).outcome()), "invalid tool did not fail macro");
            init_V_Z_Str(MacroExecutor.init_MacroExecutor_Macr_J(id).lastCompletedStep() < 2, "macro ran after failed tool");
         });
         init_V_Str_cv_DataUtil(
            "waypoint, current position and relative destinations",
            () -> {
               String scope = init_Obj_Minecraft_Supplier(mc, () -> AuditWaypointScope.current(mc));
               String name = "Tools qualification destination";
               init_Obj_Minecraft_Supplier(mc, () -> {
                  AutismWaypoints.init_AutismWaypoints()
                     .init(scope, new AutismWaypoints.Waypoint(name, 12050, 150, 12000, -8337153, System.currentTimeMillis()));
                  return true;
               });

               try {
                  autismclient.util.macro.al waypoint = init_al_I_Str(0, "WAIT");
                  waypoint.destination = "WAYPOINT";
                  waypoint.waypoint = name;
                  autismclient.util.macro.al current = init_al_I_Str(0, "WAIT");
                  current.destination = "CURRENT";
                  autismclient.util.macro.al relative = init_al_I_Str(5, "WAIT");
                  relative.destination = "OFFSET";
                  relative.f_y_I = 0;
                  relative.f_z_I = 0;
                  init_V_J(init_J_Minecraft(mc, "tools-destinations", waypoint, current, new autismclient.util.macro.EventBus(500)));
                  BlockPos origin = init_Obj_Minecraft_Supplier(mc, () -> mc.player.blockPosition());
                  init_V_Z_Str(
                     Math.abs(init_Obj_Minecraft_Supplier(mc, () -> mc.player.getX()) - 12050.5) < 1.25, "waypoint did not reach its accepted arrival area"
                  );
                  long relativeRun = init_J_Minecraft(mc, "tools-relative", relative, new autismclient.util.macro.EventBus(500));
                  init_V_BooleanSupplier_Str(() -> autismclient.util.macro.k.load(relativeRun, "THIS_MACRO", "") != null, "relative invocation");
                  autismclient.util.macro.k.Inner_StreamUtil job = autismclient.util.macro.k.load(relativeRun, "THIS_MACRO", "");
                  init_V_J(relativeRun);
                  JsonObject receipt = JsonParser.parseString(job.init_k_CachePool().load()).getAsJsonObject();
                  init_V_Z_Str(
                     receipt.get("x").getAsInt() == origin.getX() + 5 && receipt.get("z").getAsInt() == origin.getZ(),
                     "relative target was not offset from the settled starting block"
                  );
                  init_V_Z_Str(
                     Math.abs(init_Obj_Minecraft_Supplier(mc, () -> mc.player.getX()) - (origin.getX() + 5.5)) < 1.1,
                     "relative destination did not reach its accepted arrival area"
                  );
               } finally {
                  init_Obj_Minecraft_Supplier(mc, () -> {
                     AutismWaypoints.init_AutismWaypoints().init_Z(scope, name);
                     return true;
                  });
               }
            }
         );
      } catch (Throwable error) {
         JsonObject row = new JsonObject();
         row.addProperty("name", "setup");
         row.addProperty("pass", false);
         row.addProperty("error", error.toString());
         init_JsonArray.add(row);
      } finally {
         JsonObject report = new JsonObject();
         report.add("checks", init_JsonArray);
         report.addProperty("done", true);
         int failed = 0;

         for (JsonElement row : init_JsonArray) {
            if (!row.getAsJsonObject().get("pass").getAsBoolean()) {
               failed++;
            }
         }

         report.addProperty("pass", init_JsonArray.size() - failed);
         report.addProperty("fail", failed);

         try {
            Files.writeString(mc.gameDirectory.toPath().resolve("tools-macro-check.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
         } catch (Exception error) {
            AutismClientAddon.LOG.warn("Tools qualification report failed", error);
         }

         AutismClientAddon.LOG.info("TOOLS_MACRO_CHECK done pass={} fail={}", init_JsonArray.size() - failed, failed);
      }
   }

   private static void load(Minecraft mc) throws InterruptedException, ExecutionException, TimeoutException {
      init_Obj_Minecraft_Supplier(mc, () -> {
         mc.gui.setScreen(null);
         return true;
      });
      CompletableFuture<Void> staged = new CompletableFuture<>();
      mc.getSingleplayerServer()
         .execute(
            () -> {
               try {
                  IntegratedServer server = mc.getSingleplayerServer();
                  CommandSourceStack source = server.createCommandSourceStack();

                  for (int cx = 749; cx <= 755; cx++) {
                     for (int cz = 749; cz <= 750; cz++) {
                        server.overworld().getChunk(cx, cz);
                     }
                  }

                  for (String command : List.of(
                     "fill 11995 149 11995 12095 149 12005 stone",
                     "fill 11995 150 11995 12095 154 12005 air",
                     "time set noon",
                     "weather clear",
                     "gamemode survival @a",
                     "tp @a 12000.5 150 12000.5 -90 0"
                  )) {
                     server.getCommands().performPrefixedCommand(source, command);
                  }

                  init_V_Z_Str(server.overworld().getBlockState(new BlockPos(12000, 149, 12000)).is(Blocks.STONE), "qualification floor was not staged");
                  staged.complete(null);
               } catch (Throwable error) {
                  staged.completeExceptionally(error);
               }
            }
         );
      staged.get(10L, TimeUnit.SECONDS);
      Thread.sleep(1000L);
   }

   private static autismclient.util.macro.al init_al_I_Str(int x, String completion) {
      autismclient.util.macro.al action = new autismclient.util.macro.al();
      action.destination = "CUSTOM";
      action.f_x_I = x;
      action.f_y_I = 150;
      action.f_z_I = 12000;
      action.completion = completion;
      action.argumentsJson = "{\"allowDig\":false,\"allowPillar\":false,\"allowBridge\":false,\"timeoutSeconds\":30}";
      return action;
   }

   private static autismclient.util.macro.al init_al_Str(String operation) {
      autismclient.util.macro.al action = new autismclient.util.macro.al();
      action.operation = operation;
      return action;
   }

   private static autismclient.util.macro.W init_W_Str(String text) {
      autismclient.util.macro.W action = new autismclient.util.macro.W(text);
      action.waitForGuiAfter = false;
      return action;
   }

   private static autismclient.util.macro.FieldCache init_FieldCache_Str(String state) {
      autismclient.util.macro.FieldCache action = new autismclient.util.macro.FieldCache();
      action.condition = a.init_a_a_CachePool(a.Inner_CachePool.cache);
      action.condition.write = state;
      action.thenSteps = 1;
      action.elseSteps = 1;
      return action;
   }

   private static autismclient.util.macro.k.Inner_StreamUtil init_k_StreamUtil_Minecraft_I(Minecraft mc, int x) throws InterruptedException, ExecutionException, TimeoutException {
      return init_Obj_Minecraft_Supplier(mc, () -> autismclient.ai.o.init_k_StreamUtil("game:goto", init_al_I_Str(x, "WAIT").init_JsonObject_Minecraft(mc)));
   }

   private static long init_J_Minecraft(Minecraft mc, String name, MacroAction... actions) throws InterruptedException, ExecutionException, TimeoutException {
      aM macro = new aM(name);
      macro.init_List.addAll(List.of(actions));
      long id = init_Obj_Minecraft_Supplier(mc, () -> MacroExecutor.init_J_aM(macro));
      init_V_Z_Str(id >= 0L, "macro refused");
      return id;
   }

   private static void init_V_J(long id) throws InterruptedException {
      init_V_BooleanSupplier_Str(() -> MacroExecutor.init_MacroExecutor_Macr_J(id) != null, "macro completion");
      init_V_Z_Str("COMPLETED".equals(MacroExecutor.init_MacroExecutor_Macr_J(id).outcome()), MacroExecutor.init_MacroExecutor_Macr_J(id).toString());
   }

   private static void init_V_BooleanSupplier_Str(BooleanSupplier condition, String description) throws InterruptedException {
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(35L);

      while (!condition.getAsBoolean()) {
         if (System.nanoTime() > deadline) {
            throw new IllegalStateException("Timed out: " + description);
         }

         Thread.sleep(20L);
      }
   }

   private static void init_V_Z_Str(boolean value, String message) {
      if (!value) {
         throw new IllegalStateException(message);
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
      return future.get(10L, TimeUnit.SECONDS);
   }

   private static boolean init_Z_Minecraft_Supplier(Minecraft mc, Supplier<Boolean> action) {
      try {
         return init_Obj_Minecraft_Supplier(mc, action);
      } catch (Exception error) {
         throw new IllegalStateException(error);
      }
   }

   @FunctionalInterface
   private interface Inner_DataUtil {
      void run() throws Exception;
   }
}
