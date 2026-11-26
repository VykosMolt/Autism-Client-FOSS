package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.SelectableRecipe.SingleInputEntry;
import net.minecraft.world.item.crafting.SelectableRecipe.SingleInputSet;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiCraft {
   private static final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "ai-craft");
      thread.setDaemon(true);
      return thread;
   });
   private static final int read = 180;
   private static final int write = 2304;
   private static final int update = 1;
   static final int init_I = 32;
   static final int load = 8;

   private AiCraft() {
   }

   static AiOperation init(JsonObject args) {
      return init(args, true);
   }

   private static AiOperation init(final JsonObject args, final boolean capacityRecoveryAllowed) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         String spec = AiJson.init_Str_JsonObject(args, "item", "");
         final int requested = AiJson.init_I_JsonObject(args, "count", 1);
         if (requested >= 1 && requested <= 2304) {
            Item output = init_Item(spec);
            final String outputId = BuiltInRegistries.ITEM.getKey(output).toString();
            final String statusTitle = "Crafting " + output.getDefaultInstance().getHoverName().getString();
            boolean additional = AiJson.init_Z_JsonObject(args, "additional", false);
            final int timeoutSeconds = Math.max(5, Math.min(1800, AiJson.init_I_JsonObject(args, "timeoutSeconds", 180)));
            final int held = init_I(outputId);
            final int target = additional ? held + requested : requested;
            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final Object init_Obj = mc.level;
               final Object load_Obj = mc.getConnection();
               final LocalPlayer init_LocalPlayer = mc.player;
               final long load_J = System.nanoTime();
               final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
               final List<JsonObject> init_List = new ArrayList<>();
               long read_J;
               long write_J;
               long update;
               AiCraftPlanner.CraftPlan init_g_CachePool;
               int init_I;
               int load_I;
               AiStationPlacer init_cn;
               CompletableFuture<autismclient.util.AutoCraftingEngine.CraftResult> init_CompletableFuture;
               volatile Thread init_Thread;
               AiOperation init_AiOperation;
               AiOperation load_AiOperation;
               AiOperation.Result init_AiOperation_Result;
               String init_Str = "Planning craft";
               boolean init_Z;
               boolean load_Z;
               boolean read_Z;
               final boolean write_Z = AiJson.init_Z_JsonObject(args, "reclaimTable", false);
               AiOperation read_AiOperation;
               AiOperation write_AiOperation;
               int read_I;

               @Override
               public AiOperation.Result tick() {
                  if (this.init_Z
                     || mc.player == null
                     || mc.level == null
                     || mc.level != this.init_Obj
                     || mc.getConnection() != this.load_Obj
                     || mc.player != this.init_LocalPlayer) {
                     return this.init(
                        AiOperation.Status.CANCELLED,
                        this.init_Thread == null && this.init_CompletableFuture == null
                           ? "Craft cancelled"
                           : "Craft cancelled mid-craft; the in-flight craft was interrupted - materials may already be consumed, inventory may have changed"
                     );
                  }

                  if (!mc.player.isAlive()) {
                     return this.init(AiOperation.Status.FAILED, "Player died during crafting");
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        this.init_CompletableFuture != null ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                        "Craft deadline reached; any in-flight output must be reobserved before retrying"
                     );
                  }

                  if (this.load_AiOperation != null) {
                     this.init_AiOperation_Result = this.load_AiOperation.tick();
                     if (!this.init_AiOperation_Result.terminal()) {
                        return this.init(this.init_AiOperation_Result.message());
                     }

                     AiOperation.Result ended = this.init(
                        this.init_AiOperation_Result.status(),
                        this.init_AiOperation_Result.message(),
                        AiJson.init_JsonObject_ObjArr("capacityRecovery", AiRunResources.init_JsonObject(this.init_AiOperation_Result.dataJson()))
                     );
                     return new AiOperation.Result(
                        ended.status(), ended.message(), ended.dataJson(), this.init_AiOperation_Result.evidence(), this.init_AiOperation_Result.failure()
                     );
                  } else {
                     if (this.write_AiOperation != null) {
                        AiOperation.Result walked = this.write_AiOperation.tick();
                        if (!walked.terminal()) {
                           return this.init("Walking to the crafting table: " + walked.message());
                        }

                        this.write_AiOperation.cancel();
                        this.write_AiOperation = null;
                        if (walked.status() != AiOperation.Status.SUCCEEDED) {
                           AutismClientAddon.LOG.info("[AI] craft {}: walk to the table ended {}: {}", outputId, walked.status(), walked.message());
                        }
                     }

                     if (this.read_AiOperation != null) {
                        AiOperation.Result reclaimed = this.read_AiOperation.tick();
                        if (!reclaimed.terminal()) {
                           return this.init("Picking the crafting table back up");
                        }

                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                        return this.init(
                           AiOperation.Status.SUCCEEDED,
                           "Crafted "
                              + outputId
                              + " to target"
                              + (AiCraft.init_I("minecraft:crafting_table") > 0 ? "; the crafting table it set down is back in the bag" : "")
                        );
                     } else if (AiCraft.init_I(outputId) < target || this.init_CompletableFuture != null || this.init_AiOperation != null) {
                        this.load_I = 0;
                        if (this.init_CompletableFuture != null) {
                           if (!this.init_CompletableFuture.isDone()) {
                              return this.init(this.init_Str);
                           }

                           autismclient.util.AutoCraftingEngine.CraftResult result = this.init_J_CachePool();
                           if (result == null) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Craft worker result is unknown; reobserve inventory before retrying");
                           }

                           this.init_List
                              .add(
                                 AiJson.init_JsonObject_ObjArr(
                                    "recipe",
                                    this.init_g_CachePool.steps().get(this.init_I).recipe().key(),
                                    "crafts",
                                    this.init_g_CachePool.steps().get(this.init_I).crafts(),
                                    "success",
                                    result.init_Z,
                                    "message",
                                    result.init_Str,
                                    "crafted",
                                    result.init_I,
                                    "uncertain",
                                    result.load,
                                    "rejection",
                                    result.init_J_CachePool_DataUt.name(),
                                    "ms",
                                    this.init(this.write_J)
                                 )
                              );
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] craft step {} x{} for {}: {} ms ({} ms since the craft began)",
                                 this.init_g_CachePool.steps().get(this.init_I).recipe().key(),
                                 this.init_g_CachePool.steps().get(this.init_I).crafts(),
                                 outputId,
                                 this.init(this.write_J),
                                 this.init(this.load_J)
                              );
                           if (result.load) {
                              return this.init(AiOperation.Status.UNCERTAIN, result.init_Str);
                           }

                           if (result.init_J_CachePool_DataUt == autismclient.util.AutoCraftingEngine.CraftResult.FailureReason.INVENTORY_FULL) {
                              AiOperation.Result rejection = AiOperation.Result.failed(
                                 AiFailure.INVENTORY_FULL,
                                 result.init_Str,
                                 AiJson.init_JsonObject_ObjArr(
                                       "recipe",
                                       this.init_g_CachePool.steps().get(this.init_I).recipe().key(),
                                       "output",
                                       this.init_g_CachePool.steps().get(this.init_I).recipe().output(),
                                       "capacityRejectedBeforeDispatch",
                                       true
                                    )
                                    .toString()
                              );
                              int remaining = (int)Math.max(0L, (this.init_J - System.nanoTime()) / 1000000000L);
                              if (AiCraftCapacity.init_Z_J_CachePool_Z(result, capacityRecoveryAllowed) && remaining >= 5 && mc.player.containerMenu.getCarried().isEmpty()) {
                                 if (mc.player.containerMenu != mc.player.inventoryMenu) {
                                    mc.player.closeContainer();
                                 }

                                 JsonObject tidy = AiCraftCapacity.init_JsonObject_g_CachePool(
                                    this.init_g_CachePool, this.init_I, outputId, remaining, AiCraftPlanner.init(AiCraft.init_g_StreamUtil(), outputId, target)
                                 );
                                 this.load_AiOperation = AiCraftCapacity.init_AiOperation_AiOperation_Result(rejection, AiTidy.init(tidy), () -> {
                                    JsonObject retry = args.deepCopy();
                                    retry.addProperty("count", target);
                                    retry.addProperty("additional", false);
                                    retry.addProperty("timeoutSeconds", Math.max(5L, (this.init_J - System.nanoTime()) / 1000000000L));
                                    return AiCraft.init(retry, false);
                                 }, AiItems::init_I);
                                 return this.init("Making room for the craft before one retry");
                              } else {
                                 AiOperation.Result ended = this.init(
                                    rejection.status(),
                                    rejection.message(),
                                    AiJson.init_JsonObject_ObjArr("capacityFailure", AiRunResources.init_JsonObject(rejection.dataJson()))
                                 );
                                 return new AiOperation.Result(ended.status(), ended.message(), ended.dataJson(), rejection.evidence(), rejection.failure());
                              }
                           } else {
                              if (!result.init_Z && result.init_I <= 0) {
                                 return this.init(AiOperation.Status.FAILED, "Craft step failed: " + result.init_Str);
                              }

                              this.init_I++;
                              return this.tick();
                           }
                        } else if (this.init_AiOperation != null) {
                           AiOperation.Result legResult = this.init_AiOperation.tick();
                           if (!legResult.terminal()) {
                              return this.init(legResult.message());
                           }

                           AiCraftPlanner.CraftStep step = this.init_g_CachePool.steps().get(this.init_I);
                           this.init_AiOperation = null;
                           this.init_List
                              .add(
                                 AiJson.init_JsonObject_ObjArr(
                                    "recipe",
                                    step.recipe().key(),
                                    "crafts",
                                    step.crafts(),
                                    "success",
                                    legResult.status() == AiOperation.Status.SUCCEEDED,
                                    "message",
                                    legResult.message(),
                                    "crafted",
                                    step.produced(),
                                    "ms",
                                    this.init(this.write_J)
                                 )
                              );
                           if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                              return this.init(legResult.status(), "Stonecutter step failed: " + legResult.message());
                           }

                           this.init_I++;
                           return this.init("Crafted step " + this.init_I + "/" + this.init_g_CachePool.steps().size());
                        } else {
                           if (this.init_g_CachePool == null || this.init_I >= this.init_g_CachePool.steps().size()) {
                              this.init_g_CachePool = AiCraftPlanner.init(AiCraft.init_g_StreamUtil(), outputId, target);
                              this.init_I = 0;
                              if (this.init_g_CachePool.satisfied()) {
                                 return this.init(
                                    AiOperation.Status.SUCCEEDED,
                                    "Inventory already holds the requested " + outputId + " (target " + target + "); pass additional:true to craft more"
                                 );
                              }

                              if (!this.init_g_CachePool.complete()) {
                                 StringBuilder inputs = new StringBuilder();

                                 for (AiCraftPlanner.RecipeSpec recipe : AiCraft.init_g_StreamUtil().init_List(outputId)) {
                                    inputs.append(recipe.key()).append(recipe.inputs()).append(' ');
                                 }

                                 AutismClientAddon.LOG
                                    .info("[AI] craft {} incomplete: missing {}; recipes {}", outputId, this.init_g_CachePool.missing(), inputs);
                                 return this.init(AiOperation.Status.FAILED, "Cannot craft " + outputId + " yet; missing " + this.init_g_CachePool.missing());
                              }

                              if (this.init_g_CachePool.steps().isEmpty()) {
                                 return this.init(AiOperation.Status.FAILED, "No craftable route to " + outputId + "; obtain the missing materials first");
                              }

                              if (this.init_g_CachePool.requiresTable() && !AiCraft.load() && AiCraft.init_I("minecraft:crafting_table") <= 0) {
                                 AiCraftPlanner.CraftPlan table = AiCraftPlanner.init(AiCraft.init_g_StreamUtil(), "minecraft:crafting_table", 1);
                                 if (table.complete() && !table.requiresTable() && !table.steps().isEmpty()) {
                                    this.init_g_CachePool = table;
                                    return this.init("Crafting a crafting table first (" + outputId + " needs a 3x3 grid)");
                                 }

                                 AiOperation.Result walk = this.init_AiOperation_Result_38();
                                 if (walk != null) {
                                    return walk;
                                 }

                                 JsonObject missing = new JsonObject();
                                 missing.addProperty("minecraft:crafting_table", 1);
                                 table.missing().forEach(missing::addProperty);
                                 return this.init(
                                    AiOperation.Status.FAILED,
                                    "This craft needs a 3x3 grid; no crafting table is in reach or in the bag"
                                       + (table.missing().isEmpty() ? "" : ", and crafting one needs " + table.missing()),
                                    AiJson.init_JsonObject_ObjArr("missing", missing)
                                 );
                              }
                           }

                           if (this.init_g_CachePool.requiresTable() && !AiCraft.load()) {
                              AiOperation.Result station = this.load();
                              if (station != null) {
                                 return station;
                              }
                           }

                           AiCraftPlanner.CraftStep step = this.init_g_CachePool.steps().get(this.init_I);
                           this.write_J = System.nanoTime();
                           if (step.recipe().stonecutting()) {
                              this.init_AiOperation = AiCraft.init(step, (int)Math.max(5L, (this.init_J - System.nanoTime()) / 1000000000L));
                              return this.init("Stonecutting " + step.recipe().output() + " x" + step.produced());
                           } else {
                              this.init_Str = "Crafting "
                                 + step.recipe().output()
                                 + " x"
                                 + step.produced()
                                 + " (step "
                                 + (this.init_I + 1)
                                 + "/"
                                 + this.init_g_CachePool.steps().size()
                                 + ")";
                              CompletableFuture<autismclient.util.AutoCraftingEngine.CraftResult> future = new CompletableFuture<>();
                              this.init_CompletableFuture = future;
                              String recipeKey = step.recipe().key();
                              int produced = step.produced();
                              AiCraft.init_ExecutorService.execute(() -> {
                                 this.init_Thread = Thread.currentThread();
                                 Thread.interrupted();

                                 try {
                                    if (this.init_Z) {
                                       future.complete(autismclient.util.AutoCraftingEngine.CraftResult.init("Craft cancelled"));
                                       return;
                                    }

                                    future.complete(autismclient.util.AutoCraftingEngine.init(mc, recipeKey, -1, produced, AiClientTick::init_V_Runnable));
                                 } catch (Throwable t) {
                                    future.completeExceptionally(t);
                                    return;
                                 } finally {
                                    this.init_Thread = null;
                                 }
                              });
                              return this.init(this.init_Str);
                           }
                        }
                     } else if (++this.load_I >= 1) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] craft {}: {} ms total, table placement {} ms, steps {}", outputId, this.init(this.load_J), this.update, this.init_Str_17()
                           );
                        if (this.init_List.isEmpty()) {
                           return this.init(
                              AiOperation.Status.SUCCEEDED,
                              "Already holding "
                                 + AiCraft.init_I(outputId)
                                 + " "
                                 + outputId
                                 + " (target "
                                 + target
                                 + "), nothing crafted; pass additional:true to craft "
                                 + requested
                                 + " more"
                           );
                        }

                        if (this.write_Z && this.read_Z && AiCraft.init_I("minecraft:crafting_table") == 0 && !outputId.equals("minecraft:crafting_table")) {
                           this.read_AiOperation = AiProgression.init((int)Math.max(5L, Math.min(30L, (this.init_J - System.nanoTime()) / 1000000000L)), true);
                           if (this.read_AiOperation != null) {
                              return this.init("Picking the crafting table back up");
                           }
                        }

                        return this.init(AiOperation.Status.SUCCEEDED, "Crafted " + outputId + " to target");
                     } else {
                        return this.init("Verifying crafted inventory");
                     }
                  }
               }

               AiOperation.Result init_AiOperation_Result_38() {
                  BlockPos standing = this.read_I < 2
                     ? autismclient.ai.engine.AiStaircase.init_BlockPos_Level(mc.level, mc.player.blockPosition(), 32, state -> state.is(Blocks.CRAFTING_TABLE))
                     : null;
                  if (standing == null) {
                     return null;
                  }

                  this.read_I++;
                  this.write_AiOperation = AiGoto.init_AiOperation(
                     standing, (int)Math.max(10L, Math.min(90L, (this.init_J - System.nanoTime()) / 1000000000L))
                  );
                  return this.init("Walking to the crafting table at " + standing.toShortString());
               }

               AiOperation.Result load() {
                  if (AiCraft.init_I("minecraft:crafting_table") <= 0) {
                     AiOperation.Result walk = this.init_AiOperation_Result_38();
                     return walk != null
                        ? walk
                        : this.init(
                           AiOperation.Status.FAILED,
                           "This craft needs a 3x3 grid and no crafting table is in reach or in the bag",
                           AiJson.init_JsonObject_ObjArr("missing", AiJson.init_JsonObject_ObjArr("minecraft:crafting_table", 1))
                        );
                  }

                  if (this.init_cn == null) {
                     this.init_cn = new AiStationPlacer(BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:crafting_table")).orElseThrow(), "crafting table");
                     if (this.read_J == 0L) {
                        this.read_J = System.nanoTime();
                     }
                  }

                  AiStationPlacer.Inner_DataUtil status = this.init_cn.init_cn_DataUtil();
                  if (status == AiStationPlacer.Inner_DataUtil.PLACED) {
                     this.init_cn = null;
                     this.read_Z = true;
                     this.update = this.init(this.read_J);
                     AutismClientAddon.LOG.info("[AI] craft {}: crafting table placed in {} ms", outputId, this.update);
                     return this.init("Crafting table placed");
                  } else if (status == AiStationPlacer.Inner_DataUtil.FAILED) {
                     String why = this.init_cn.load_Str();
                     this.init_cn = null;
                     return this.init(AiOperation.Status.FAILED, "Could not place a crafting table: " + why);
                  } else {
                     return this.init(this.init_cn.init_Str());
                  }
               }

               autismclient.util.AutoCraftingEngine.CraftResult init_J_CachePool() {
                  try {
                     return this.init_CompletableFuture.join();
                  } catch (RuntimeException e) {
                     return null;
                  } finally {
                     this.init_CompletableFuture = null;
                  }
               }

               long init(long sinceNanos) {
                  return sinceNanos == 0L ? 0L : (System.nanoTime() - sinceNanos) / 1000000L;
               }

               String init_Str_17() {
                  StringBuilder out = new StringBuilder();

                  for (JsonObject entry : this.init_List) {
                     if (out.length() > 0) {
                        out.append(", ");
                     }

                     out.append(AiJson.init_Str_JsonObject(entry, "recipe", "?")).append(' ').append(AiJson.init_I_JsonObject(entry, "ms", 0)).append(" ms");
                  }

                  return out.length() == 0 ? "none" : out.toString();
               }

               JsonObject init_JsonObject_56() {
                  JsonObject out = AiJson.init_JsonObject_ObjArr(
                     "item",
                     outputId,
                     "target",
                     target,
                     "held",
                     AiCraft.init_I(outputId),
                     "startedWith",
                     held,
                     "timings",
                     AiJson.init_JsonObject_ObjArr("totalMs", this.init(this.load_J), "tablePlaceMs", this.update)
                  );
                  if (this.init_g_CachePool != null) {
                     JsonArray steps = new JsonArray();

                     for (AiCraftPlanner.CraftStep step : this.init_g_CachePool.steps()) {
                        steps.add(
                           AiJson.init_JsonObject_ObjArr(
                              "recipe",
                              step.recipe().key(),
                              "output",
                              step.recipe().output(),
                              "crafts",
                              step.crafts(),
                              "requiresTable",
                              step.recipe().requiresTable()
                           )
                        );
                     }

                     out.add("plan", steps);
                     out.addProperty("stepsDone", this.init_I);
                     if (!this.init_g_CachePool.missing().isEmpty()) {
                        JsonObject missing = new JsonObject();
                        this.init_g_CachePool.missing().forEach(missing::addProperty);
                        out.add("missing", missing);
                        JsonArray suggested = new JsonArray();

                        for (String id : this.init_g_CachePool.missing().keySet()) {
                           suggested.add("game:obtain " + id);
                        }

                        out.add("suggestedActions", suggested);
                     }
                  }

                  JsonArray done = new JsonArray();

                  for (JsonObject entry : this.init_List) {
                     done.add(entry);
                  }

                  out.add("executed", done);
                  if (this.init_AiOperation_Result != null) {
                     out.add("capacityRecovery", AiRunResources.init_JsonObject(this.init_AiOperation_Result.dataJson()));
                  }

                  return out;
               }

               AiOperation.Result init(String message) {
                  AiVisualization.init(this, statusTitle, AiTaskStatus.init(message, "Preparing the recipe"), AiCraft.init_I(outputId), target);
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_56().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  return this.init(status, message, null);
               }

               AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
                  this.load_Z = this.load_Z | (this.requiresReconciliationAfterEmergency() || status == AiOperation.Status.UNCERTAIN);
                  JsonObject data = this.init_JsonObject_56();
                  if (extra != null) {
                     for (Entry<String, JsonElement> entry : extra.entrySet()) {
                        data.add(entry.getKey(), entry.getValue());
                     }
                  }

                  AiOperation.Result result = this.init_AiOperation_Result == null
                     ? AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(this.load_Z, new AiOperation.Result(status, message, data.toString(), List.of()))
                     : AiNetherRun.init_AiOperation_Result_AiOperation_Status(status, message, data, this.load_Z, this.init_AiOperation_Result);
                  if (this.init_AiOperation_Result != null) {
                     result = new AiOperation.Result(
                        result.status(), result.message(), result.dataJson(), this.init_AiOperation_Result.evidence(), this.init_AiOperation_Result.failure()
                     );
                  }

                  this.cancel();
                  return result;
               }

               @Override
               public void cancel() {
                  if (!this.init_Z) {
                     this.load_Z = this.load_Z | this.requiresReconciliationAfterEmergency();
                     this.init_Z = true;
                     AiVisualization.init_V(this);
                     this.init_DataPipe.init_V();
                     if (this.init_cn != null) {
                        this.init_cn.init_V();
                        this.init_cn = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                     }

                     if (this.write_AiOperation != null) {
                        this.write_AiOperation.cancel();
                        this.write_AiOperation = null;
                     }

                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                     }

                     Thread worker = this.init_Thread;
                     if (worker != null) {
                        worker.interrupt();
                     }

                     CompletableFuture<autismclient.util.AutoCraftingEngine.CraftResult> pending = this.init_CompletableFuture;
                     this.init_CompletableFuture = null;
                     if (pending != null && !pending.isDone()) {
                        pending.cancel(true);
                     }
                  }
               }

               @Override
               public boolean canInterruptNow() {
                  return !this.load_Z
                     && this.init_CompletableFuture == null
                     && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.load_Z
                     || this.init_CompletableFuture != null
                     || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
               }
            };
         } else {
            throw new IllegalArgumentException("count must be between 1 and 2304");
         }
      } else {
         throw new IllegalArgumentException("Crafting requires a connected living player");
      }
   }

   private static AiOperation init(AiCraftPlanner.CraftStep step, final int timeoutSeconds) {
      final Minecraft mc = Minecraft.getInstance();
      final String outputId = step.recipe().output();
      final List<String> accepted = step.recipe().inputs().isEmpty() ? List.of() : step.recipe().inputs().get(0);
      final int wanted = step.produced();
      return new AiOperation() {
         final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
         final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
         BlockPos init_BlockPos;
         int init_I;
         int load_I;
         int read;
         int write;
         AiStationPlacer init_cn;
         long load_J = -1L;
         int update;
         int sync;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.load_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(this.load_J >= 0L ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Stonecutting deadline reached");
               }

               if (mc.player.containerMenu instanceof StonecutterMenu menu) {
                  if (this.load_J >= 0L) {
                     if (AiServerEvidence.init_J_I(menu.containerId) > this.load_J) {
                        if (this.sync > 0) {
                           int actual = AiCraft.init_I(outputId) - this.update;
                           if (actual < this.sync) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Stonecutter reply did not confirm the expected output");
                           }

                           this.read += actual;
                           this.sync = 0;
                        }

                        this.load_J = -1L;
                        this.write = 0;
                        return this.init("Cut confirmed");
                     } else {
                        return ++this.write > 40
                           ? this.init(AiOperation.Status.UNCERTAIN, "A cut was not confirmed by the server")
                           : this.init("Waiting for the cut to apply");
                     }
                  } else {
                     if (this.read >= wanted) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Stonecut " + this.read + " " + outputId);
                     }

                     if (menu.getSlot(0).getItem().isEmpty()) {
                        int slot = this.init(accepted);
                        if (slot < 0) {
                           JsonObject missing = new JsonObject();
                           missing.addProperty(accepted.isEmpty() ? "unknown" : accepted.get(0), 1);
                           return this.init(AiOperation.Status.FAILED, "Stonecutting input exhausted", AiJson.init_JsonObject_ObjArr("missing", missing));
                        } else {
                           this.load_J = AiServerEvidence.init_J_I(menu.containerId);
                           AiContainerTransactions.init(menu.containerId, this.init(slot), 0, ContainerInput.QUICK_MOVE);
                           return this.init("Loading the stonecutter");
                        }
                     } else {
                        int recipeIndex = this.init(menu, outputId);
                        if (recipeIndex < 0) {
                           return this.init(AiOperation.Status.FAILED, "No stonecutter recipe produces " + outputId + " from this input");
                        }

                        if (menu.getSelectedRecipeIndex() != recipeIndex) {
                           mc.gameMode.handleInventoryButtonClick(menu.containerId, recipeIndex);
                           return this.init("Selecting the recipe");
                        }

                        ItemStack result = menu.getSlot(1).getItem();
                        if (result.isEmpty()) {
                           return this.init("Waiting for the result slot");
                        }

                        this.load_J = AiServerEvidence.init_J_I(menu.containerId);
                        this.update = AiCraft.init_I(outputId);
                        this.sync = result.getCount();
                        AiContainerTransactions.init(menu.containerId, 1, 0, ContainerInput.QUICK_MOVE);
                        return this.init("Cutting " + this.read + "/" + wanted);
                     }
                  }
               } else {
                  AiOperation.Result open = this.init_AiOperation_Result_39();
                  return open != null ? open : this.init("Opening the stonecutter");
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Stonecutting cancelled");
            }
         }

         AiOperation.Result init_AiOperation_Result_39() {
            if (this.init_BlockPos == null && this.load_I-- <= 0) {
               this.init_BlockPos = this.init_BlockPos_17();
               this.load_I = 40;
            }

            if (this.init_BlockPos == null && this.load_I < 40 && !this.init_Z) {
               return this.init("Looking for a stonecutter");
            }

            if (this.init_BlockPos == null) {
               if (!this.init_Z) {
                  if (AiCraft.init_I("minecraft:stonecutter") <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:stonecutter", 1);
                     return this.init(AiOperation.Status.FAILED, "No stonecutter in reach or in the bag", AiJson.init_JsonObject_ObjArr("missing", missing));
                  }

                  this.init_cn = new AiStationPlacer(BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:stonecutter")).orElseThrow(), "stonecutter");
                  this.init_Z = true;
                  this.init_I = 0;
               }

               AiStationPlacer.Inner_DataUtil status = this.init_cn.init_cn_DataUtil();
               if (status == AiStationPlacer.Inner_DataUtil.PLACED) {
                  this.init_BlockPos = this.init_cn.init_BlockPos();
                  this.init_Z = false;
                  return this.init("Stonecutter placed");
               } else if (status == AiStationPlacer.Inner_DataUtil.FAILED) {
                  this.init_Z = false;
                  return this.init(AiOperation.Status.FAILED, "Could not place a stonecutter: " + this.init_cn.load_Str());
               } else {
                  return this.init(this.init_cn.init_Str());
               }
            } else {
               double reach = Math.max(4.5, mc.player.blockInteractionRange());
               if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos)) > reach * reach) {
                  return this.init(AiOperation.Status.FAILED, "The stonecutter fell out of reach");
               }

               this.init_DataUtil.init_V_Vec3(Vec3.atCenterOf(this.init_BlockPos));
               this.init_DataUtil.load_V();
               if (++this.init_I > 80) {
                  return this.init(AiOperation.Status.FAILED, "Could not open the stonecutter");
               }

               if (!autismclient.ai.engine.AiAim.init_Z_BlockPos(this.init_BlockPos)) {
                  return this.init("Turning toward the stonecutter");
               }

               BlockHitResult hit = autismclient.ai.engine.AiAim.init_BlockHitResult();
               if (hit == null) {
                  return this.init("Turning toward the stonecutter");
               }

               mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
               Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
               return null;
            }
         }

         BlockPos init_BlockPos_17() {
            Block wantedx = AiCraft.init_Block("minecraft:stonecutter");
            BlockPos origin = mc.player.blockPosition();
            BlockPos best = null;
            double bestDistance = 1.7976931348623157E308;
            int radius = 16;

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
               if (AiTerrain.init(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wantedx) {
                  double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = pos.immutable();
                  }
               }
            }

            return best;
         }

         int init(List<String> acceptedIds) {
            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = mc.player.getInventory().getItem(slot);
               if (!stack.isEmpty() && acceptedIds.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                  return slot;
               }
            }

            return -1;
         }

         int init(int inventorySlot) {
            return inventorySlot < 9 ? inventorySlot + 29 : inventorySlot - 9 + 2;
         }

         int init(StonecutterMenu menu, String wantedId) {
            SingleInputSet<StonecutterRecipe> visible = menu.getVisibleRecipes();

            for (int index = 0; index < visible.size(); index++) {
               SingleInputEntry entry = visible.entries().get(index);
               ItemStack option = entry.recipe().optionDisplay().resolveForFirstStack(SlotDisplayContext.fromLevel(mc.level));
               if (option != null && !option.isEmpty() && BuiltInRegistries.ITEM.getKey(option.getItem()).toString().equals(wantedId)) {
                  return index;
               }
            }

            return -1;
         }

         JsonObject init_JsonObject_57() {
            return AiJson.init_JsonObject_ObjArr(
               "output", outputId, "produced", this.read, "wanted", wanted, "stationAt", this.init_BlockPos == null ? null : this.init_BlockPos.toShortString()
            );
         }

         AiOperation.Result init(String message) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_57().toString(), List.of());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return this.init(status, message, null);
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
            this.cancel();
            JsonObject data = this.init_JsonObject_57();
            if (extra != null) {
               for (Entry<String, JsonElement> entry : extra.entrySet()) {
                  data.add(entry.getKey(), entry.getValue());
               }
            }

            return new AiOperation.Result(status, message, data.toString(), List.of());
         }

         @Override
         public void cancel() {
            if (!this.load_Z) {
               this.load_Z = true;
               this.init_DataUtil.init_V();
               this.init_DataPipe.init_V();
               if (this.init_cn != null) {
                  this.init_cn.init_V();
               }
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.load_J < 0L;
         }
      };
   }

   static AiCraftPlanner.RecipeGraph init_g_StreamUtil() {
      Minecraft mc = Minecraft.getInstance();
      final Map<String, List<AiCraftPlanner.RecipeSpec>> byOutput = new HashMap<>();

      for (autismclient.util.AutoCraftingEngine.ResolvedRecipe plan : autismclient.util.AutoCraftingEngine.load_List(mc)) {
         List<List<String>> inputs = new ArrayList<>(plan.init_List().size());

         for (autismclient.util.AutoCraftingEngine.IngredientSlot ingredient : plan.init_List()) {
            List<String> accepts = new ArrayList<>(ingredient.init().size());

            for (Item item : ingredient.init()) {
               accepts.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }

            inputs.add(accepts);
         }

         String outputId = BuiltInRegistries.ITEM.getKey(plan.init_Item()).toString();
         byOutput.computeIfAbsent(outputId, id -> new ArrayList<>())
            .add(new AiCraftPlanner.RecipeSpec(plan.init_Str(), outputId, plan.init_I(), plan.init_Z(), inputs));
      }

      for (autismclient.util.AutoCraftingEngine.StationRecipe plan : autismclient.util.AutoCraftingEngine.write(mc)) {
         List<String> accepts = new ArrayList<>(plan.init_List().size());

         for (Item item : plan.init_List()) {
            accepts.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }

         String outputId = BuiltInRegistries.ITEM.getKey(plan.init_Item()).toString();
         byOutput.computeIfAbsent(outputId, id -> new ArrayList<>())
            .add(new AiCraftPlanner.RecipeSpec(plan.init_Str(), outputId, plan.init_I(), false, List.of(List.copyOf(accepts)), plan.load()));
      }

      final Map<String, Integer> counts = new HashMap<>();
      return AiObtain.init(new AiCraftPlanner.RecipeGraph() {
         @Override
         public List<AiCraftPlanner.RecipeSpec> init_List(String itemId) {
            return byOutput.getOrDefault(itemId, List.of());
         }

         @Override
         public int init_I(String itemId) {
            return counts.computeIfAbsent(itemId, AiCraft::init_I);
         }
      });
   }

   static boolean init_Z() {
      Minecraft mc = Minecraft.getInstance();
      if (init_I("minecraft:crafting_table") > 0) {
         return true;
      } else {
         return mc.player != null
               && mc.level != null
               && autismclient.ai.engine.AiStaircase.init_BlockPos_Level(mc.level, mc.player.blockPosition(), 32, state -> state.is(Blocks.CRAFTING_TABLE)) != null
            ? true
            : AiCraftPlanner.init(init_g_StreamUtil(), "minecraft:crafting_table", 1).complete();
      }
   }

   static int init_I(String itemId) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int total = 0;

      for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId)) {
            total += stack.getCount();
         }
      }

      return total;
   }

   static boolean load() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         double reach = Math.max(4.5, mc.player.blockInteractionRange());
         int radius = (int)Math.ceil(reach);
         BlockPos origin = mc.player.blockPosition();

         for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            if (mc.level.getBlockState(pos).is(Blocks.CRAFTING_TABLE) && mc.player.distanceToSqr(Vec3.atCenterOf(pos)) <= reach * reach) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static BlockPos init_BlockPos() {
      List<BlockPos> spots = init_List();
      return spots.isEmpty() ? null : spots.get(0);
   }

   static BlockPos init(int radius) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         BlockPos best = null;
         double bestSq = 1.7976931348623157E308;

         for (BlockPos p : BlockPos.betweenClosed(feet.offset(-radius, -3, -radius), feet.offset(radius, 3, radius))) {
            if ((p.getX() != feet.getX() || p.getZ() != feet.getZ()) && init(mc, p)) {
               int free = 0;

               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos c = p.relative(side);
                  if (mc.level.getBlockState(c).canBeReplaced()
                     && mc.level.getFluidState(c).isEmpty()
                     && !mc.level.getBlockState(c.below()).getCollisionShape(mc.level, c.below()).isEmpty()) {
                     free++;
                  }
               }

               if (free >= 2) {
                  double sq = p.distSqr(feet);
                  if (sq < bestSq) {
                     bestSq = sq;
                     best = p.immutable();
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   static BlockPos load(int radius) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         BlockPos best = null;
         double bestSq = 1.7976931348623157E308;

         for (BlockPos p : BlockPos.betweenClosed(feet.offset(-radius, -1, -radius), feet.offset(radius, 1, radius))) {
            if ((p.getX() != feet.getX() || p.getZ() != feet.getZ()) && init(mc, p)) {
               double sq = p.distSqr(feet);
               if (sq < bestSq) {
                  bestSq = sq;
                  best = p.immutable();
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private static boolean init(Minecraft mc, BlockPos p) {
      return mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
         && mc.level.getFluidState(p).isEmpty()
         && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
         && mc.level.getBlockState(p.below()).isCollisionShapeFullBlock(mc.level, p.below())
         && !mc.level.getBlockState(p.below()).is(BlockTags.LEAVES);
   }

   static boolean read() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos under = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.2, mc.player.getZ());
         return mc.level.getBlockState(under).is(BlockTags.LEAVES);
      } else {
         return false;
      }
   }

   static List<BlockPos> init_List() {
      Minecraft mc = Minecraft.getInstance();
      List<BlockPos> out = new ArrayList<>();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         List<BlockPos> candidates = new ArrayList<>();

         for (Direction side : Plane.HORIZONTAL) {
            candidates.add(feet.relative(side));
            candidates.add(feet.above().relative(side));
            candidates.add(feet.below().relative(side).relative(side));
         }

         List<BlockPos> wider = new ArrayList<>();

         for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
               for (int dz = -2; dz <= 2; dz++) {
                  BlockPos c = feet.offset(dx, dy, dz);
                  if (!candidates.contains(c)) {
                     wider.add(c);
                  }
               }
            }
         }

         wider.sort(Comparator.comparingDouble(c -> c.distSqr(feet.above())));
         candidates.addAll(wider);
         List<BlockPos> wet = new ArrayList<>();

         for (BlockPos candidate : candidates) {
            if (AiTerrain.init(mc.level, candidate)) {
               BlockState state = mc.level.getBlockState(candidate);
               boolean water = state.getFluidState().is(FluidTags.WATER);
               if (state.canBeReplaced() && (state.getFluidState().isEmpty() || water) && !mc.player.getBoundingBox().intersects(new AABB(candidate))) {
                  BlockState below = mc.level.getBlockState(candidate.below());
                  if (!below.getCollisionShape(mc.level, candidate.below()).isEmpty()) {
                     (water ? wet : out).add(candidate.immutable());
                  }
               }
            }
         }

         Vec3 eye = mc.player.getEyePosition();
         Vec3 look = mc.player.getLookAngle();
         out.sort(
            Comparator.<BlockPos>comparingInt(c -> autismclient.ai.engine.AiEditLedger.write_Z(c) ? 1 : 0)
               .thenComparingInt(c -> init(mc, eye, c) ? 0 : 1)
               .thenComparingInt(c -> c.getY() < feet.getY() ? 1 : 0)
               .thenComparingDouble(c -> -new Vec3(c.getX() + 0.5, c.getY(), c.getZ() + 0.5).subtract(eye).normalize().dot(look))
         );
         out.addAll(wet);
         return out;
      } else {
         return out;
      }
   }

   static boolean init(Minecraft mc, Vec3 eye, BlockPos cell) {
      Vec3 target = new Vec3(cell.getX() + 0.5, cell.getY() - 0.05, cell.getZ() + 0.5);
      if (eye.distanceToSqr(target) > 19.360000000000003) {
         return false;
      }

      BlockHitResult hit = mc.level.clip(new ClipContext(eye, target, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player));
      return hit.getType() == Type.BLOCK && hit.getBlockPos().equals(cell.below()) && hit.getDirection() == Direction.UP;
   }

   private static Item init_Item(String spec) {
      AiItems.ItemSpec target = AiItems.load_af_DataUtil(spec);
      if (target.items().size() == 1) {
         return target.items().iterator().next();
      }

      Minecraft mc = Minecraft.getInstance();

      for (autismclient.util.AutoCraftingEngine.CraftOption option : autismclient.util.AutoCraftingEngine.init_List(mc)) {
         if (option.read_Z && target.items().contains(option.init_ItemStack.getItem())) {
            return option.init_ItemStack.getItem();
         }
      }

      for (Item item : target.items()) {
         if (!autismclient.util.AutoCraftingEngine.init(mc, item).isEmpty()) {
            return item;
         }
      }

      throw new IllegalArgumentException("'" + spec + "' names " + target.items().size() + " items and none has a known recipe; name the exact item to craft");
   }

   static Block init_Block(String blockId) {
      return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(blockId)).orElseThrow(() -> new IllegalArgumentException("Unknown block " + blockId));
   }
}
