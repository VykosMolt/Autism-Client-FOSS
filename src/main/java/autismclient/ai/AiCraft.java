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
   private static final ExecutorService CRAFT_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
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

   static AiOperation craft(JsonObject args) {
      return craft(args, true);
   }

   private static AiOperation craft(final JsonObject args, final boolean capacityRecoveryAllowed) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         String spec = AiJson.getString(args, "item", "");
         final int requested = AiJson.getInt(args, "count", 1);
         if (requested >= 1 && requested <= 2304) {
            Item output = resolveCraftableItem(spec);
            final String outputId = BuiltInRegistries.ITEM.getKey(output).toString();
            final String statusTitle = "Crafting " + output.getDefaultInstance().getHoverName().getString();
            boolean additional = AiJson.getBoolean(args, "additional", false);
            final int timeoutSeconds = Math.max(5, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 180)));
            final int held = countItem(outputId);
            final int target = additional ? held + requested : requested;
            return new AiOperation() {
               final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
               final Object startLevel = mc.level;
               final Object startConnection = mc.getConnection();
               final LocalPlayer startPlayer = mc.player;
               final long startedAtNanos = System.nanoTime();
               final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
               final List<JsonObject> executedSteps = new ArrayList<>();
               long tablePlaceStartNanos;
               long stepStartNanos;
               long tablePlaceMs;
               AiCraftPlanner.CraftPlan plan;
               int stepsDone;
               int load_I;
               AiStationPlacer tablePlacer;
               CompletableFuture<autismclient.util.AutoCraftingEngine.CraftResult> craftFuture;
               volatile Thread craftThread;
               AiOperation stonecutLeg;
               AiOperation tidyRetryOp;
               AiOperation.Result tidyRetryResult;
               String statusMessage = "Planning craft";
               boolean cancelled;
               boolean needsReconciliation;
               boolean placedTable;
               final boolean reclaimTable = AiJson.getBoolean(args, "reclaimTable", false);
               AiOperation reclaimTableOp;
               AiOperation walkToTableOp;
               int tableApproachAttempts;

               @Override
               public AiOperation.Result tick() {
                  if (this.cancelled
                     || mc.player == null
                     || mc.level == null
                     || mc.level != this.startLevel
                     || mc.getConnection() != this.startConnection
                     || mc.player != this.startPlayer) {
                     return this.finish(
                        AiOperation.Status.CANCELLED,
                        this.craftThread == null && this.craftFuture == null
                           ? "Craft cancelled"
                           : "Craft cancelled mid-craft; the in-flight craft was interrupted - materials may already be consumed, inventory may have changed"
                     );
                  }

                  if (!mc.player.isAlive()) {
                     return this.finish(AiOperation.Status.FAILED, "Player died during crafting");
                  }

                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(
                        this.craftFuture != null ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                        "Craft deadline reached; any in-flight output must be reobserved before retrying"
                     );
                  }

                  if (this.tidyRetryOp != null) {
                     this.tidyRetryResult = this.tidyRetryOp.tick();
                     if (!this.tidyRetryResult.terminal()) {
                        return this.running(this.tidyRetryResult.message());
                     }

                     AiOperation.Result ended = this.finish(
                        this.tidyRetryResult.status(),
                        this.tidyRetryResult.message(),
                        AiJson.objectOf("capacityRecovery", AiRunResources.parseEvidenceJson(this.tidyRetryResult.dataJson()))
                     );
                     return new AiOperation.Result(
                        ended.status(), ended.message(), ended.dataJson(), this.tidyRetryResult.evidence(), this.tidyRetryResult.failure()
                     );
                  } else {
                     if (this.walkToTableOp != null) {
                        AiOperation.Result walked = this.walkToTableOp.tick();
                        if (!walked.terminal()) {
                           return this.running("Walking to the crafting table: " + walked.message());
                        }

                        this.walkToTableOp.cancel();
                        this.walkToTableOp = null;
                        if (walked.status() != AiOperation.Status.SUCCEEDED) {
                           AutismClientAddon.LOG.info("[AI] craft {}: walk to the table ended {}: {}", outputId, walked.status(), walked.message());
                        }
                     }

                     if (this.reclaimTableOp != null) {
                        AiOperation.Result reclaimed = this.reclaimTableOp.tick();
                        if (!reclaimed.terminal()) {
                           return this.running("Picking the crafting table back up");
                        }

                        this.reclaimTableOp.cancel();
                        this.reclaimTableOp = null;
                        return this.finish(
                           AiOperation.Status.SUCCEEDED,
                           "Crafted "
                              + outputId
                              + " to target"
                              + (AiCraft.countItem("minecraft:crafting_table") > 0 ? "; the crafting table it set down is back in the bag" : "")
                        );
                     } else if (AiCraft.countItem(outputId) < target || this.craftFuture != null || this.stonecutLeg != null) {
                        this.load_I = 0;
                        if (this.craftFuture != null) {
                           if (!this.craftFuture.isDone()) {
                              return this.running(this.statusMessage);
                           }

                           autismclient.util.AutoCraftingEngine.CraftResult result = this.awaitCraftResult();
                           if (result == null) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "Craft worker result is unknown; reobserve inventory before retrying");
                           }

                           this.executedSteps
                              .add(
                                 AiJson.objectOf(
                                    "recipe",
                                    this.plan.steps().get(this.stepsDone).recipe().key(),
                                    "crafts",
                                    this.plan.steps().get(this.stepsDone).crafts(),
                                    "success",
                                    result.success,
                                    "message",
                                    result.message,
                                    "crafted",
                                    result.craftedAmount,
                                    "uncertain",
                                    result.uncertain,
                                    "rejection",
                                    result.failureReason.name(),
                                    "ms",
                                    this.elapsedMs(this.stepStartNanos)
                                 )
                              );
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] craft step {} x{} for {}: {} ms ({} ms since the craft began)",
                                 this.plan.steps().get(this.stepsDone).recipe().key(),
                                 this.plan.steps().get(this.stepsDone).crafts(),
                                 outputId,
                                 this.elapsedMs(this.stepStartNanos),
                                 this.elapsedMs(this.startedAtNanos)
                              );
                           if (result.uncertain) {
                              return this.finish(AiOperation.Status.UNCERTAIN, result.message);
                           }

                           if (result.failureReason == autismclient.util.AutoCraftingEngine.CraftResult.FailureReason.INVENTORY_FULL) {
                              AiOperation.Result rejection = AiOperation.Result.failed(
                                 AiFailure.INVENTORY_FULL,
                                 result.message,
                                 AiJson.objectOf(
                                       "recipe",
                                       this.plan.steps().get(this.stepsDone).recipe().key(),
                                       "output",
                                       this.plan.steps().get(this.stepsDone).recipe().output(),
                                       "capacityRejectedBeforeDispatch",
                                       true
                                    )
                                    .toString()
                              );
                              int remaining = (int)Math.max(0L, (this.deadlineNanos - System.nanoTime()) / 1000000000L);
                              if (AiCraftCapacity.isCapacityFailure(result, capacityRecoveryAllowed) && remaining >= 5 && mc.player.containerMenu.getCarried().isEmpty()) {
                                 if (mc.player.containerMenu != mc.player.inventoryMenu) {
                                    mc.player.closeContainer();
                                 }

                                 JsonObject tidy = AiCraftCapacity.tidyPlan(
                                    this.plan, this.stepsDone, outputId, remaining, AiCraftPlanner.init(AiCraft.buildRecipeGraph(), outputId, target)
                                 );
                                 this.tidyRetryOp = AiCraftCapacity.tidyThenRetry(rejection, AiTidy.init(tidy), () -> {
                                    JsonObject retry = args.deepCopy();
                                    retry.addProperty("count", target);
                                    retry.addProperty("additional", false);
                                    retry.addProperty("timeoutSeconds", Math.max(5L, (this.deadlineNanos - System.nanoTime()) / 1000000000L));
                                    return AiCraft.craft(retry, false);
                                 }, AiItems::countFreeSlots);
                                 return this.running("Making room for the craft before one retry");
                              } else {
                                 AiOperation.Result ended = this.finish(
                                    rejection.status(),
                                    rejection.message(),
                                    AiJson.objectOf("capacityFailure", AiRunResources.parseEvidenceJson(rejection.dataJson()))
                                 );
                                 return new AiOperation.Result(ended.status(), ended.message(), ended.dataJson(), rejection.evidence(), rejection.failure());
                              }
                           } else {
                              if (!result.success && result.craftedAmount <= 0) {
                                 return this.finish(AiOperation.Status.FAILED, "Craft step failed: " + result.message);
                              }

                              this.stepsDone++;
                              return this.tick();
                           }
                        } else if (this.stonecutLeg != null) {
                           AiOperation.Result legResult = this.stonecutLeg.tick();
                           if (!legResult.terminal()) {
                              return this.running(legResult.message());
                           }

                           AiCraftPlanner.CraftStep step = this.plan.steps().get(this.stepsDone);
                           this.stonecutLeg = null;
                           this.executedSteps
                              .add(
                                 AiJson.objectOf(
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
                                    this.elapsedMs(this.stepStartNanos)
                                 )
                              );
                           if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                              return this.finish(legResult.status(), "Stonecutter step failed: " + legResult.message());
                           }

                           this.stepsDone++;
                           return this.running("Crafted step " + this.stepsDone + "/" + this.plan.steps().size());
                        } else {
                           if (this.plan == null || this.stepsDone >= this.plan.steps().size()) {
                              this.plan = AiCraftPlanner.init(AiCraft.buildRecipeGraph(), outputId, target);
                              this.stepsDone = 0;
                              if (this.plan.satisfied()) {
                                 return this.finish(
                                    AiOperation.Status.SUCCEEDED,
                                    "Inventory already holds the requested " + outputId + " (target " + target + "); pass additional:true to craft more"
                                 );
                              }

                              if (!this.plan.complete()) {
                                 StringBuilder inputs = new StringBuilder();

                                 for (AiCraftPlanner.RecipeSpec recipe : AiCraft.buildRecipeGraph().recipesFor(outputId)) {
                                    inputs.append(recipe.key()).append(recipe.inputs()).append(' ');
                                 }

                                 AutismClientAddon.LOG
                                    .info("[AI] craft {} incomplete: missing {}; recipes {}", outputId, this.plan.missing(), inputs);
                                 return this.finish(AiOperation.Status.FAILED, "Cannot craft " + outputId + " yet; missing " + this.plan.missing());
                              }

                              if (this.plan.steps().isEmpty()) {
                                 return this.finish(AiOperation.Status.FAILED, "No craftable route to " + outputId + "; obtain the missing materials first");
                              }

                              if (this.plan.requiresTable() && !AiCraft.isCraftingTableInReach() && AiCraft.countItem("minecraft:crafting_table") <= 0) {
                                 AiCraftPlanner.CraftPlan table = AiCraftPlanner.init(AiCraft.buildRecipeGraph(), "minecraft:crafting_table", 1);
                                 if (table.complete() && !table.requiresTable() && !table.steps().isEmpty()) {
                                    this.plan = table;
                                    return this.running("Crafting a crafting table first (" + outputId + " needs a 3x3 grid)");
                                 }

                                 AiOperation.Result walk = this.walkToCraftingTable();
                                 if (walk != null) {
                                    return walk;
                                 }

                                 JsonObject missing = new JsonObject();
                                 missing.addProperty("minecraft:crafting_table", 1);
                                 table.missing().forEach(missing::addProperty);
                                 return this.finish(
                                    AiOperation.Status.FAILED,
                                    "This craft needs a 3x3 grid; no crafting table is in reach or in the bag"
                                       + (table.missing().isEmpty() ? "" : ", and crafting one needs " + table.missing()),
                                    AiJson.objectOf("missing", missing)
                                 );
                              }
                           }

                           if (this.plan.requiresTable() && !AiCraft.isCraftingTableInReach()) {
                              AiOperation.Result station = this.ensureCraftingTable();
                              if (station != null) {
                                 return station;
                              }
                           }

                           AiCraftPlanner.CraftStep step = this.plan.steps().get(this.stepsDone);
                           this.stepStartNanos = System.nanoTime();
                           if (step.recipe().stonecutting()) {
                              this.stonecutLeg = AiCraft.stonecutStep(step, (int)Math.max(5L, (this.deadlineNanos - System.nanoTime()) / 1000000000L));
                              return this.running("Stonecutting " + step.recipe().output() + " x" + step.produced());
                           } else {
                              this.statusMessage = "Crafting "
                                 + step.recipe().output()
                                 + " x"
                                 + step.produced()
                                 + " (step "
                                 + (this.stepsDone + 1)
                                 + "/"
                                 + this.plan.steps().size()
                                 + ")";
                              CompletableFuture<autismclient.util.AutoCraftingEngine.CraftResult> future = new CompletableFuture<>();
                              this.craftFuture = future;
                              String recipeKey = step.recipe().key();
                              int produced = step.produced();
                              AiCraft.CRAFT_EXECUTOR.execute(() -> {
                                 this.craftThread = Thread.currentThread();
                                 Thread.interrupted();

                                 try {
                                    if (this.cancelled) {
                                       future.complete(autismclient.util.AutoCraftingEngine.CraftResult.failed("Craft cancelled"));
                                       return;
                                    }

                                    future.complete(autismclient.util.AutoCraftingEngine.craftWithDispatch(mc, recipeKey, -1, produced, AiClientTick::execute));
                                 } catch (Throwable t) {
                                    future.completeExceptionally(t);
                                    return;
                                 } finally {
                                    this.craftThread = null;
                                 }
                              });
                              return this.running(this.statusMessage);
                           }
                        }
                     } else if (++this.load_I >= 1) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] craft {}: {} ms total, table placement {} ms, steps {}", outputId, this.elapsedMs(this.startedAtNanos), this.tablePlaceMs, this.executedSummary()
                           );
                        if (this.executedSteps.isEmpty()) {
                           return this.finish(
                              AiOperation.Status.SUCCEEDED,
                              "Already holding "
                                 + AiCraft.countItem(outputId)
                                 + " "
                                 + outputId
                                 + " (target "
                                 + target
                                 + "), nothing crafted; pass additional:true to craft "
                                 + requested
                                 + " more"
                           );
                        }

                        if (this.reclaimTable && this.placedTable && AiCraft.countItem("minecraft:crafting_table") == 0 && !outputId.equals("minecraft:crafting_table")) {
                           this.reclaimTableOp = AiProgression.init((int)Math.max(5L, Math.min(30L, (this.deadlineNanos - System.nanoTime()) / 1000000000L)), true);
                           if (this.reclaimTableOp != null) {
                              return this.running("Picking the crafting table back up");
                           }
                        }

                        return this.finish(AiOperation.Status.SUCCEEDED, "Crafted " + outputId + " to target");
                     } else {
                        return this.running("Verifying crafted inventory");
                     }
                  }
               }

               AiOperation.Result walkToCraftingTable() {
                  BlockPos standing = this.tableApproachAttempts < 2
                     ? autismclient.ai.engine.AiStaircase.findNearestBlock(mc.level, mc.player.blockPosition(), 32, state -> state.is(Blocks.CRAFTING_TABLE))
                     : null;
                  if (standing == null) {
                     return null;
                  }

                  this.tableApproachAttempts++;
                  this.walkToTableOp = AiGoto.approachBlockCompat(
                     standing, (int)Math.max(10L, Math.min(90L, (this.deadlineNanos - System.nanoTime()) / 1000000000L))
                  );
                  return this.running("Walking to the crafting table at " + standing.toShortString());
               }

               AiOperation.Result ensureCraftingTable() {
                  if (AiCraft.countItem("minecraft:crafting_table") <= 0) {
                     AiOperation.Result walk = this.walkToCraftingTable();
                     return walk != null
                        ? walk
                        : this.finish(
                           AiOperation.Status.FAILED,
                           "This craft needs a 3x3 grid and no crafting table is in reach or in the bag",
                           AiJson.objectOf("missing", AiJson.objectOf("minecraft:crafting_table", 1))
                        );
                  }

                  if (this.tablePlacer == null) {
                     this.tablePlacer = new AiStationPlacer(BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:crafting_table")).orElseThrow(), "crafting table");
                     if (this.tablePlaceStartNanos == 0L) {
                        this.tablePlaceStartNanos = System.nanoTime();
                     }
                  }

                  AiStationPlacer.Inner_DataUtil status = this.tablePlacer.tick();
                  if (status == AiStationPlacer.Inner_DataUtil.PLACED) {
                     this.tablePlacer = null;
                     this.placedTable = true;
                     this.tablePlaceMs = this.elapsedMs(this.tablePlaceStartNanos);
                     AutismClientAddon.LOG.info("[AI] craft {}: crafting table placed in {} ms", outputId, this.tablePlaceMs);
                     return this.running("Crafting table placed");
                  } else if (status == AiStationPlacer.Inner_DataUtil.FAILED) {
                     String why = this.tablePlacer.getFailureReason();
                     this.tablePlacer = null;
                     return this.finish(AiOperation.Status.FAILED, "Could not place a crafting table: " + why);
                  } else {
                     return this.running(this.tablePlacer.getStatus());
                  }
               }

               autismclient.util.AutoCraftingEngine.CraftResult awaitCraftResult() {
                  try {
                     return this.craftFuture.join();
                  } catch (RuntimeException e) {
                     return null;
                  } finally {
                     this.craftFuture = null;
                  }
               }

               long elapsedMs(long sinceNanos) {
                  return sinceNanos == 0L ? 0L : (System.nanoTime() - sinceNanos) / 1000000L;
               }

               String executedSummary() {
                  StringBuilder out = new StringBuilder();

                  for (JsonObject entry : this.executedSteps) {
                     if (out.length() > 0) {
                        out.append(", ");
                     }

                     out.append(AiJson.getString(entry, "recipe", "?")).append(' ').append(AiJson.getInt(entry, "ms", 0)).append(" ms");
                  }

                  return out.length() == 0 ? "none" : out.toString();
               }

               JsonObject snapshotJson() {
                  JsonObject out = AiJson.objectOf(
                     "item",
                     outputId,
                     "target",
                     target,
                     "held",
                     AiCraft.countItem(outputId),
                     "startedWith",
                     held,
                     "timings",
                     AiJson.objectOf("totalMs", this.elapsedMs(this.startedAtNanos), "tablePlaceMs", this.tablePlaceMs)
                  );
                  if (this.plan != null) {
                     JsonArray steps = new JsonArray();

                     for (AiCraftPlanner.CraftStep step : this.plan.steps()) {
                        steps.add(
                           AiJson.objectOf(
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
                     out.addProperty("stepsDone", this.stepsDone);
                     if (!this.plan.missing().isEmpty()) {
                        JsonObject missing = new JsonObject();
                        this.plan.missing().forEach(missing::addProperty);
                        out.add("missing", missing);
                        JsonArray suggested = new JsonArray();

                        for (String id : this.plan.missing().keySet()) {
                           suggested.add("game:obtain " + id);
                        }

                        out.add("suggestedActions", suggested);
                     }
                  }

                  JsonArray done = new JsonArray();

                  for (JsonObject entry : this.executedSteps) {
                     done.add(entry);
                  }

                  out.add("executed", done);
                  if (this.tidyRetryResult != null) {
                     out.add("capacityRecovery", AiRunResources.parseEvidenceJson(this.tidyRetryResult.dataJson()));
                  }

                  return out;
               }

               AiOperation.Result running(String message) {
                  AiVisualization.init(this, statusTitle, AiTaskStatus.init(message, "Preparing the recipe"), AiCraft.countItem(outputId), target);
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.snapshotJson().toString(), List.of());
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  return this.finish(status, message, null);
               }

               AiOperation.Result finish(AiOperation.Status status, String message, JsonObject extra) {
                  this.needsReconciliation = this.needsReconciliation | (this.requiresReconciliationAfterEmergency() || status == AiOperation.Status.UNCERTAIN);
                  JsonObject data = this.snapshotJson();
                  if (extra != null) {
                     for (Entry<String, JsonElement> entry : extra.entrySet()) {
                        data.add(entry.getKey(), entry.getValue());
                     }
                  }

                  AiOperation.Result result = this.tidyRetryResult == null
                     ? AiGuardLayer.wrapPendingTermination(this.needsReconciliation, new AiOperation.Result(status, message, data.toString(), List.of()))
                     : AiNetherRun.init_AiOperation_Result_AiOperation_Status(status, message, data, this.needsReconciliation, this.tidyRetryResult);
                  if (this.tidyRetryResult != null) {
                     result = new AiOperation.Result(
                        result.status(), result.message(), result.dataJson(), this.tidyRetryResult.evidence(), this.tidyRetryResult.failure()
                     );
                  }

                  this.cancel();
                  return result;
               }

               @Override
               public void cancel() {
                  if (!this.cancelled) {
                     this.needsReconciliation = this.needsReconciliation | this.requiresReconciliationAfterEmergency();
                     this.cancelled = true;
                     AiVisualization.clearOwner(this);
                     this.placer.reset();
                     if (this.tablePlacer != null) {
                        this.tablePlacer.cancel();
                        this.tablePlacer = null;
                     }

                     if (this.reclaimTableOp != null) {
                        this.reclaimTableOp.cancel();
                        this.reclaimTableOp = null;
                     }

                     if (this.walkToTableOp != null) {
                        this.walkToTableOp.cancel();
                        this.walkToTableOp = null;
                     }

                     if (this.stonecutLeg != null) {
                        this.stonecutLeg.cancel();
                        this.stonecutLeg = null;
                     }

                     if (this.tidyRetryOp != null) {
                        this.tidyRetryOp.cancel();
                     }

                     Thread worker = this.craftThread;
                     if (worker != null) {
                        worker.interrupt();
                     }

                     CompletableFuture<autismclient.util.AutoCraftingEngine.CraftResult> pending = this.craftFuture;
                     this.craftFuture = null;
                     if (pending != null && !pending.isDone()) {
                        pending.cancel(true);
                     }
                  }
               }

               @Override
               public boolean canInterruptNow() {
                  return !this.needsReconciliation
                     && this.craftFuture == null
                     && (this.stonecutLeg == null || this.stonecutLeg.canInterruptNow())
                     && (this.tidyRetryOp == null || this.tidyRetryOp.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.needsReconciliation
                     || this.craftFuture != null
                     || this.stonecutLeg != null && this.stonecutLeg.requiresReconciliationAfterEmergency()
                     || this.tidyRetryOp != null && this.tidyRetryOp.requiresReconciliationAfterEmergency();
               }
            };
         } else {
            throw new IllegalArgumentException("count must be between 1 and 2304");
         }
      } else {
         throw new IllegalArgumentException("Crafting requires a connected living player");
      }
   }

   private static AiOperation stonecutStep(AiCraftPlanner.CraftStep step, final int timeoutSeconds) {
      final Minecraft mc = Minecraft.getInstance();
      final String outputId = step.recipe().output();
      final List<String> accepted = step.recipe().inputs().isEmpty() ? List.of() : step.recipe().inputs().get(0);
      final int wanted = step.produced();
      return new AiOperation() {
         final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
         BlockPos stonecutterPos;
         int aimTicks;
         int searchCooldown;
         int cutCount;
         int confirmWaitTicks;
         AiStationPlacer stonecutterPlacer;
         long pendingRevision = -1L;
         int countBeforeCut;
         int expectedCutCount;
         boolean placingStonecutter;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadlineNanos) {
                  return this.finish(this.pendingRevision >= 0L ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Stonecutting deadline reached");
               }

               if (mc.player.containerMenu instanceof StonecutterMenu menu) {
                  if (this.pendingRevision >= 0L) {
                     if (AiServerEvidence.containerRevision(menu.containerId) > this.pendingRevision) {
                        if (this.expectedCutCount > 0) {
                           int actual = AiCraft.countItem(outputId) - this.countBeforeCut;
                           if (actual < this.expectedCutCount) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "Stonecutter reply did not confirm the expected output");
                           }

                           this.cutCount += actual;
                           this.expectedCutCount = 0;
                        }

                        this.pendingRevision = -1L;
                        this.confirmWaitTicks = 0;
                        return this.running("Cut confirmed");
                     } else {
                        return ++this.confirmWaitTicks > 40
                           ? this.finish(AiOperation.Status.UNCERTAIN, "A cut was not confirmed by the server")
                           : this.running("Waiting for the cut to apply");
                     }
                  } else {
                     if (this.cutCount >= wanted) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Stonecut " + this.cutCount + " " + outputId);
                     }

                     if (menu.getSlot(0).getItem().isEmpty()) {
                        int slot = this.findInputSlot(accepted);
                        if (slot < 0) {
                           JsonObject missing = new JsonObject();
                           missing.addProperty(accepted.isEmpty() ? "unknown" : accepted.get(0), 1);
                           return this.finish(AiOperation.Status.FAILED, "Stonecutting input exhausted", AiJson.objectOf("missing", missing));
                        } else {
                           this.pendingRevision = AiServerEvidence.containerRevision(menu.containerId);
                           AiContainerTransactions.sendPredictedClick(menu.containerId, this.menuSlotFor(slot), 0, ContainerInput.QUICK_MOVE);
                           return this.running("Loading the stonecutter");
                        }
                     } else {
                        int recipeIndex = this.recipeIndexFor(menu, outputId);
                        if (recipeIndex < 0) {
                           return this.finish(AiOperation.Status.FAILED, "No stonecutter recipe produces " + outputId + " from this input");
                        }

                        if (menu.getSelectedRecipeIndex() != recipeIndex) {
                           mc.gameMode.handleInventoryButtonClick(menu.containerId, recipeIndex);
                           return this.running("Selecting the recipe");
                        }

                        ItemStack result = menu.getSlot(1).getItem();
                        if (result.isEmpty()) {
                           return this.running("Waiting for the result slot");
                        }

                        this.pendingRevision = AiServerEvidence.containerRevision(menu.containerId);
                        this.countBeforeCut = AiCraft.countItem(outputId);
                        this.expectedCutCount = result.getCount();
                        AiContainerTransactions.sendPredictedClick(menu.containerId, 1, 0, ContainerInput.QUICK_MOVE);
                        return this.running("Cutting " + this.cutCount + "/" + wanted);
                     }
                  }
               } else {
                  AiOperation.Result open = this.openStonecutter();
                  return open != null ? open : this.running("Opening the stonecutter");
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Stonecutting cancelled");
            }
         }

         AiOperation.Result openStonecutter() {
            if (this.stonecutterPos == null && this.searchCooldown-- <= 0) {
               this.stonecutterPos = this.findNearbyStonecutter();
               this.searchCooldown = 40;
            }

            if (this.stonecutterPos == null && this.searchCooldown < 40 && !this.placingStonecutter) {
               return this.running("Looking for a stonecutter");
            }

            if (this.stonecutterPos == null) {
               if (!this.placingStonecutter) {
                  if (AiCraft.countItem("minecraft:stonecutter") <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:stonecutter", 1);
                     return this.finish(AiOperation.Status.FAILED, "No stonecutter in reach or in the bag", AiJson.objectOf("missing", missing));
                  }

                  this.stonecutterPlacer = new AiStationPlacer(BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:stonecutter")).orElseThrow(), "stonecutter");
                  this.placingStonecutter = true;
                  this.aimTicks = 0;
               }

               AiStationPlacer.Inner_DataUtil status = this.stonecutterPlacer.tick();
               if (status == AiStationPlacer.Inner_DataUtil.PLACED) {
                  this.stonecutterPos = this.stonecutterPlacer.getPlaced();
                  this.placingStonecutter = false;
                  return this.running("Stonecutter placed");
               } else if (status == AiStationPlacer.Inner_DataUtil.FAILED) {
                  this.placingStonecutter = false;
                  return this.finish(AiOperation.Status.FAILED, "Could not place a stonecutter: " + this.stonecutterPlacer.getFailureReason());
               } else {
                  return this.running(this.stonecutterPlacer.getStatus());
               }
            } else {
               double reach = Math.max(4.5, mc.player.blockInteractionRange());
               if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.stonecutterPos)) > reach * reach) {
                  return this.finish(AiOperation.Status.FAILED, "The stonecutter fell out of reach");
               }

               this.aim.aimAt(Vec3.atCenterOf(this.stonecutterPos));
               this.aim.tick();
               if (++this.aimTicks > 80) {
                  return this.finish(AiOperation.Status.FAILED, "Could not open the stonecutter");
               }

               if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.stonecutterPos)) {
                  return this.running("Turning toward the stonecutter");
               }

               BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
               if (hit == null) {
                  return this.running("Turning toward the stonecutter");
               }

               mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
               Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
               return null;
            }
         }

         BlockPos findNearbyStonecutter() {
            Block wantedx = AiCraft.requireBlock("minecraft:stonecutter");
            BlockPos origin = mc.player.blockPosition();
            BlockPos best = null;
            double bestDistance = 1.7976931348623157E308;
            int radius = 16;

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
               if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wantedx) {
                  double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = pos.immutable();
                  }
               }
            }

            return best;
         }

         int findInputSlot(List<String> acceptedIds) {
            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = mc.player.getInventory().getItem(slot);
               if (!stack.isEmpty() && acceptedIds.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                  return slot;
               }
            }

            return -1;
         }

         int menuSlotFor(int inventorySlot) {
            return inventorySlot < 9 ? inventorySlot + 29 : inventorySlot - 9 + 2;
         }

         int recipeIndexFor(StonecutterMenu menu, String wantedId) {
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

         JsonObject snapshotJson() {
            return AiJson.objectOf(
               "output", outputId, "produced", this.cutCount, "wanted", wanted, "stationAt", this.stonecutterPos == null ? null : this.stonecutterPos.toShortString()
            );
         }

         AiOperation.Result running(String message) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.snapshotJson().toString(), List.of());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            return this.finish(status, message, null);
         }

         AiOperation.Result finish(AiOperation.Status status, String message, JsonObject extra) {
            this.cancel();
            JsonObject data = this.snapshotJson();
            if (extra != null) {
               for (Entry<String, JsonElement> entry : extra.entrySet()) {
                  data.add(entry.getKey(), entry.getValue());
               }
            }

            return new AiOperation.Result(status, message, data.toString(), List.of());
         }

         @Override
         public void cancel() {
            if (!this.cancelled) {
               this.cancelled = true;
               this.aim.clear();
               this.placer.reset();
               if (this.stonecutterPlacer != null) {
                  this.stonecutterPlacer.cancel();
               }
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.pendingRevision < 0L;
         }
      };
   }

   static AiCraftPlanner.RecipeGraph buildRecipeGraph() {
      Minecraft mc = Minecraft.getInstance();
      final Map<String, List<AiCraftPlanner.RecipeSpec>> byOutput = new HashMap<>();

      for (autismclient.util.AutoCraftingEngine.ResolvedRecipe plan : autismclient.util.AutoCraftingEngine.getResolvedCraftingRecipes(mc)) {
         List<List<String>> inputs = new ArrayList<>(plan.inputs().size());

         for (autismclient.util.AutoCraftingEngine.IngredientSlot ingredient : plan.inputs()) {
            List<String> accepts = new ArrayList<>(ingredient.acceptedItems().size());

            for (Item item : ingredient.acceptedItems()) {
               accepts.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }

            inputs.add(accepts);
         }

         String outputId = BuiltInRegistries.ITEM.getKey(plan.outputItem()).toString();
         byOutput.computeIfAbsent(outputId, id -> new ArrayList<>())
            .add(new AiCraftPlanner.RecipeSpec(plan.recipeKey(), outputId, plan.outputCount(), plan.requiresTable(), inputs));
      }

      for (autismclient.util.AutoCraftingEngine.StationRecipe plan : autismclient.util.AutoCraftingEngine.getStonecutterRecipes(mc)) {
         List<String> accepts = new ArrayList<>(plan.inputs().size());

         for (Item item : plan.inputs()) {
            accepts.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }

         String outputId = BuiltInRegistries.ITEM.getKey(plan.outputItem()).toString();
         byOutput.computeIfAbsent(outputId, id -> new ArrayList<>())
            .add(new AiCraftPlanner.RecipeSpec(plan.recipeKey(), outputId, plan.outputCount(), false, List.of(List.copyOf(accepts)), plan.station()));
      }

      final Map<String, Integer> counts = new HashMap<>();
      return AiObtain.init(new AiCraftPlanner.RecipeGraph() {
         @Override
         public List<AiCraftPlanner.RecipeSpec> recipesFor(String itemId) {
            return byOutput.getOrDefault(itemId, List.of());
         }

         @Override
         public int held(String itemId) {
            return counts.computeIfAbsent(itemId, AiCraft::countItem);
         }
      });
   }

   static boolean canAccessCraftingTable() {
      Minecraft mc = Minecraft.getInstance();
      if (countItem("minecraft:crafting_table") > 0) {
         return true;
      } else {
         return mc.player != null
               && mc.level != null
               && autismclient.ai.engine.AiStaircase.findNearestBlock(mc.level, mc.player.blockPosition(), 32, state -> state.is(Blocks.CRAFTING_TABLE)) != null
            ? true
            : AiCraftPlanner.init(buildRecipeGraph(), "minecraft:crafting_table", 1).complete();
      }
   }

   static int countItem(String itemId) {
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

   static boolean isCraftingTableInReach() {
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

   static BlockPos findPlacementSpot() {
      List<BlockPos> spots = candidatePlacementSpots();
      return spots.isEmpty() ? null : spots.get(0);
   }

   static BlockPos findOpenStationSpot(int radius) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         BlockPos best = null;
         double bestSq = 1.7976931348623157E308;

         for (BlockPos p : BlockPos.betweenClosed(feet.offset(-radius, -3, -radius), feet.offset(radius, 3, radius))) {
            if ((p.getX() != feet.getX() || p.getZ() != feet.getZ()) && isValidFlatSpot(mc, p)) {
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

   static BlockPos findFlatStationSpot(int radius) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         BlockPos best = null;
         double bestSq = 1.7976931348623157E308;

         for (BlockPos p : BlockPos.betweenClosed(feet.offset(-radius, -1, -radius), feet.offset(radius, 1, radius))) {
            if ((p.getX() != feet.getX() || p.getZ() != feet.getZ()) && isValidFlatSpot(mc, p)) {
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

   private static boolean isValidFlatSpot(Minecraft mc, BlockPos p) {
      return mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
         && mc.level.getFluidState(p).isEmpty()
         && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
         && mc.level.getBlockState(p.below()).isCollisionShapeFullBlock(mc.level, p.below())
         && !mc.level.getBlockState(p.below()).is(BlockTags.LEAVES);
   }

   static boolean isStandingOnLeaves() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos under = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.2, mc.player.getZ());
         return mc.level.getBlockState(under).is(BlockTags.LEAVES);
      } else {
         return false;
      }
   }

   static List<BlockPos> candidatePlacementSpots() {
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
            if (AiTerrain.isLoaded(mc.level, candidate)) {
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
            Comparator.<BlockPos>comparingInt(c -> autismclient.ai.engine.AiEditLedger.hasBreakRecord(c) ? 1 : 0)
               .thenComparingInt(c -> hasClearTopFaceSupport(mc, eye, c) ? 0 : 1)
               .thenComparingInt(c -> c.getY() < feet.getY() ? 1 : 0)
               .thenComparingDouble(c -> -new Vec3(c.getX() + 0.5, c.getY(), c.getZ() + 0.5).subtract(eye).normalize().dot(look))
         );
         out.addAll(wet);
         return out;
      } else {
         return out;
      }
   }

   static boolean hasClearTopFaceSupport(Minecraft mc, Vec3 eye, BlockPos cell) {
      Vec3 target = new Vec3(cell.getX() + 0.5, cell.getY() - 0.05, cell.getZ() + 0.5);
      if (eye.distanceToSqr(target) > 19.360000000000003) {
         return false;
      }

      BlockHitResult hit = mc.level.clip(new ClipContext(eye, target, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player));
      return hit.getType() == Type.BLOCK && hit.getBlockPos().equals(cell.below()) && hit.getDirection() == Direction.UP;
   }

   private static Item resolveCraftableItem(String spec) {
      AiItems.ItemSpec target = AiItems.resolveItem(spec);
      if (target.items().size() == 1) {
         return target.items().iterator().next();
      }

      Minecraft mc = Minecraft.getInstance();

      for (autismclient.util.AutoCraftingEngine.CraftOption option : autismclient.util.AutoCraftingEngine.scanOptionsForClient(mc)) {
         if (option.craftable && target.items().contains(option.outputStack.getItem())) {
            return option.outputStack.getItem();
         }
      }

      for (Item item : target.items()) {
         if (!autismclient.util.AutoCraftingEngine.getCraftingRecipesFor(mc, item).isEmpty()) {
            return item;
         }
      }

      throw new IllegalArgumentException("'" + spec + "' names " + target.items().size() + " items and none has a known recipe; name the exact item to craft");
   }

   static Block requireBlock(String blockId) {
      return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(blockId)).orElseThrow(() -> new IllegalArgumentException("Unknown block " + blockId));
   }
}
