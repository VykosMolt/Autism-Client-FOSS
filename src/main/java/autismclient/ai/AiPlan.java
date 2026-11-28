package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

final class AiPlan {
   private static final int init = 32;

   private AiPlan() {
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         List<AiPlanRunner.Inner_BufferHelper> declared = parseSteps(args);
         final List<AiPlanRunner.Inner_BufferHelper> ordered = AiPlanRunner.orderSteps(declared);
         final AiPlanRunner.Inner_DataUtil policy = parseFailurePolicy(AiJson.getString(args, "onFailure", "stop"));
         final int timeoutSeconds = Math.max(30, Math.min(7200, AiJson.getInt(args, "timeoutSeconds", 1800)));
         final boolean guarded = AiJson.getBoolean(args, "survival", true);
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final List<AiPlanRunner.Inner_StreamUtil> steps = AiPlan.toRuntimeSteps(ordered);
            final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard();
            AiOperation child;
            AiPlanRunner.Inner_StreamUtil current;
            AiOperation guardOp;
            String guardReason = "";
            int guardInterrupts;
            boolean stopPlan;
            boolean cancelled;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Plan deadline reached; " + this.tallyText());
                  }

                  if (guarded) {
                     AiOperation.Result guardResult = this.tickGuard();
                     if (guardResult != null) {
                        return guardResult;
                     }
                  }

                  if (this.child != null) {
                     AiOperation.Result result = this.child.tick();
                     if (!result.terminal()) {
                        return this.running("[" + this.current.step.id() + "] " + result.message());
                     }

                     this.child.cancel();
                     this.child = null;
                     boolean succeeded = result.status() == AiOperation.Status.SUCCEEDED;
                     if (result.status() == AiOperation.Status.CANCELLED) {
                        return this.finish(AiOperation.Status.CANCELLED, "Plan cancelled during " + this.current.step.id());
                     }

                     this.stopPlan = result.status() == AiOperation.Status.UNCERTAIN
                        ? AiPlanRunner.init(this.current, result.status() + ": " + result.message(), policy)
                        : AiPlanRunner.init(this.current, succeeded, result.status() + ": " + result.message(), policy);
                     String id = this.current.step.id();
                     this.current = null;
                     return this.stopPlan
                        ? this.finish(AiOperation.Status.FAILED, "Step '" + id + "' failed and onFailure is stop; " + this.tallyText())
                        : this.running("Step '" + id + "' " + (succeeded ? "done" : "failed") + "; " + this.tallyText());
                  } else {
                     if (AiPlanRunner.isFinished(this.steps)) {
                        return this.finishPlan();
                     }

                     this.current = AiPlanRunner.nextRunnable(this.steps);
                     if (this.current == null) {
                        return this.finishPlan();
                     }

                     this.current.state = AiPlanRunner.Inner_CachePool.RUNNING;
                     this.current.attempts++;

                     try {
                        this.child = AiTools.operation(
                           this.current.step.capability(),
                           AiJson.parseObject(this.current.step.argumentsJson()).getAsJsonObject()
                        );
                     } catch (RuntimeException e) {
                        this.child = null;
                        this.stopPlan = AiPlanRunner.init(this.current, false, "rejected: " + e.getMessage(), policy);
                        String id = this.current.step.id();
                        this.current = null;
                        if (this.stopPlan) {
                           return this.finish(AiOperation.Status.FAILED, "Step '" + id + "' was rejected and onFailure is stop; " + this.tallyText());
                        }

                        return this.running("Step '" + id + "' rejected; " + this.tallyText());
                     }

                     if (this.child == null) {
                        this.stopPlan = AiPlanRunner.init(
                           this.current, false, "unknown capability " + this.current.step.capability(), policy
                        );
                        String id = this.current.step.id();
                        this.current = null;
                        return this.stopPlan
                           ? this.finish(AiOperation.Status.FAILED, "Step '" + id + "' names an unknown capability; " + this.tallyText())
                           : this.running("Step '" + id + "' names an unknown capability; " + this.tallyText());
                     } else {
                        return this.running(
                           "Starting step '"
                              + this.current.step.id()
                              + "' ("
                              + this.current.step.capability()
                              + ")"
                        );
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Plan cancelled");
               }
            }

            AiOperation.Result finishPlan() {
               Map<String, Integer> tally = AiPlanRunner.tally(this.steps);
               int failed = tally.getOrDefault("failed", 0) + tally.getOrDefault("skipped", 0);
               if (failed == 0) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Plan complete: " + this.tallyText());
               } else {
                  return tally.getOrDefault("done", 0) == 0
                     ? this.finish(AiOperation.Status.FAILED, "No plan step completed; " + this.tallyText())
                     : this.finish(AiOperation.Status.UNCERTAIN, "Plan partially complete; " + this.tallyText());
               }
            }

            AiOperation.Result tickGuard() {
               if (this.guardOp != null) {
                  AiOperation.Result result = this.guardOp.tick();
                  if (!result.terminal()) {
                     return this.running("Interrupted: " + this.guardReason + " - " + result.message());
                  }

                  this.guardOp.cancel();
                  this.guardOp = null;
                  return this.running("Resuming the plan after " + this.guardReason);
               } else {
                  if (this.child != null) {
                     return null;
                  }

                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.secondsLeft());
                  if (action == null) {
                     return null;
                  }

                  this.guardReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                  this.guardInterrupts++;
                  this.guardOp = action;
                  return this.running("Interrupted between steps: " + this.guardReason);
               }
            }

            String tallyText() {
               Map<String, Integer> tally = AiPlanRunner.tally(this.steps);
               return tally.getOrDefault("done", 0)
                  + " done, "
                  + tally.getOrDefault("failed", 0)
                  + " failed, "
                  + tally.getOrDefault("skipped", 0)
                  + " skipped, "
                  + tally.getOrDefault("pending", 0)
                  + " not reached";
            }

            int secondsLeft() {
               return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
            }

            JsonObject progressJson() {
               JsonArray steps = new JsonArray();

               for (AiPlanRunner.Inner_StreamUtil progress : this.steps) {
                  steps.add(
                     AiJson.objectOf(
                        "id",
                        progress.step.id(),
                        "capability",
                        progress.step.capability(),
                        "state",
                        progress.state.name().toLowerCase(Locale.ROOT),
                        "attempts",
                        progress.attempts,
                        "message",
                        progress.message,
                        "needs",
                        AiJson.GSON.toJsonTree(progress.step.needs())
                     )
                  );
               }

               JsonObject out = AiJson.objectOf("onFailure", policy.name().toLowerCase(Locale.ROOT));
               out.add("steps", steps);
               JsonObject tally = new JsonObject();
               AiPlanRunner.tally(this.steps).forEach(tally::addProperty);
               out.add("tally", tally);
               if (this.current != null) {
                  out.addProperty("currentStep", this.current.step.id());
               }

               out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.guardInterrupts, this.guardReason));
               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               JsonObject data = this.progressJson();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  if (this.guardOp != null) {
                     this.guardOp.cancel();
                     this.guardOp = null;
                  }

                  if (this.child != null) {
                     this.child.cancel();
                     this.child = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Running a plan requires a connected living player");
      }
   }

   private static List<AiPlanRunner.Inner_StreamUtil> toRuntimeSteps(List<AiPlanRunner.Inner_BufferHelper> ordered) {
      List<AiPlanRunner.Inner_StreamUtil> plan = new ArrayList<>(ordered.size());

      for (AiPlanRunner.Inner_BufferHelper step : ordered) {
         plan.add(new AiPlanRunner.Inner_StreamUtil(step));
      }

      return plan;
   }

   static List<AiPlanRunner.Inner_BufferHelper> parseSteps(JsonObject args) {
      if (args.has("steps") && args.get("steps").isJsonArray()) {
         JsonArray raw = args.getAsJsonArray("steps");
         if (raw.isEmpty()) {
            throw new IllegalArgumentException("A plan needs at least one step");
         }

         if (raw.size() > 32) {
            throw new IllegalArgumentException("A plan may hold at most 32 steps; split a longer objective into several plans");
         }

         List<AiPlanRunner.Inner_BufferHelper> steps = new ArrayList<>(raw.size());
         int index = 0;

         for (JsonElement element : raw) {
            JsonObject entry = stepObject(element, ++index);
            String capability = AiJson.getString(entry, "capability", "");
            if (capability.isBlank() && AiJson.getString(entry, "id", "").startsWith("game:")) {
               capability = AiJson.getString(entry, "id", "");
            }

            String id = capability.equals(AiJson.getString(entry, "id", "")) ? "step" + index : AiJson.getString(entry, "id", "step" + index);
            AiPlanRunner.init(capability);
            JsonObject arguments = entry.has("arguments") && entry.get("arguments").isJsonObject() ? entry.getAsJsonObject("arguments") : new JsonObject();
            init(id, capability, arguments);
            List<String> needs = new ArrayList<>();
            if (entry.has("needs") && entry.get("needs").isJsonArray()) {
               for (JsonElement need : entry.getAsJsonArray("needs")) {
                  needs.add(need.getAsString());
               }
            }

            steps.add(new AiPlanRunner.Inner_BufferHelper(id, capability, arguments.toString(), needs));
         }

         return steps;
      } else {
         throw new IllegalArgumentException("steps is required: an array of {id, capability, arguments, needs}");
      }
   }

   private static JsonObject stepObject(JsonElement element, int index) {
      if (element.isJsonObject()) {
         return element.getAsJsonObject();
      }

      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
         List<String> tokens = new ArrayList<>(AiToolCallParser.tokenize(element.getAsString().trim()));
         if (!tokens.isEmpty()) {
            LocalPlayer player = Minecraft.getInstance().player;
            String sub = tokens.remove(0).toLowerCase(Locale.ROOT);

            try {
               AiToolCallParser.ToolCall call = AiToolCallParser.init(
                  sub, tokens, player == null ? 0.0 : player.getX(), player == null ? 0.0 : player.getY(), player == null ? 0.0 : player.getZ()
               );
               return AiJson.objectOf("id", "step" + index, "capability", call.id(), "arguments", call.arguments());
            } catch (IllegalArgumentException e) {
               throw new IllegalArgumentException("Plan step " + index + " '" + element.getAsString() + "': " + e.getMessage());
            }
         }
      }

      throw new IllegalArgumentException(
         "Plan step " + index + " must be an object {id, capability, arguments, needs} or a shorthand string like \"craft crafting_table 1\""
      );
   }

   private static void init(String id, String capability, JsonObject arguments) {
      AiCatalog.Entry entry = AiDirectOps.catalogSnapshot().get(capability);
      if (entry != null) {
         JsonObject schema = AiJson.parseObject(entry.contract().inputSchema());
         if (schema.has("type") || schema.has("properties")) {
            try {
               AiSchemas.validate(schema, arguments);
            } catch (IllegalArgumentException e) {
               throw new IllegalArgumentException("Plan step '" + id + "' (" + capability + "): " + e.getMessage());
            }
         }
      }
   }

   static AiPlanRunner.Inner_DataUtil parseFailurePolicy(String text) {
      String word = text.trim().toLowerCase(Locale.ROOT);
      String var10000;
      switch (word) {
         case "skip":
         case "ignore":
         case "next":
         case "proceed":
            var10000 = "continue";
            break;
         case "abort":
         case "halt":
         case "fail":
         case "end":
            var10000 = "stop";
            break;
         case "repeat":
         case "again":
            var10000 = "retry";
            break;
         default:
            var10000 = word;
      }

      word = var10000;

      try {
         return AiPlanRunner.Inner_DataUtil.valueOf(word.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("onFailure must be stop, continue or retry (got '" + text + "')");
      }
   }
}
