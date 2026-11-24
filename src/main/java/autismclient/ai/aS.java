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

final class aS {
   private static final int init = 32;

   private aS() {
   }

   static AiOperation init_AiOperation(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         List<aU.Inner_BufferHelper> declared = init_List(args);
         final List<aU.Inner_BufferHelper> ordered = aU.init_List(declared);
         final aU.Inner_DataUtil policy = init(ag.init_Str_JsonObject(args, "onFailure", "stop"));
         final int timeoutSeconds = Math.max(30, Math.min(7200, ag.init_I_JsonObject(args, "timeoutSeconds", 1800)));
         final boolean guarded = ag.init_Z_JsonObject(args, "survival", true);
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final List<aU.Inner_StreamUtil> init_List = aS.init(ordered);
            final X.Inner_CachePool init_X_CachePool = X.Inner_CachePool.standard();
            AiOperation init_AiOperation;
            aU.Inner_StreamUtil init_aU_StreamUtil;
            AiOperation load_AiOperation;
            String init_Str = "";
            int init_I;
            boolean init_Z;
            boolean load_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.load_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Plan deadline reached; " + this.init_Str_12());
                  }

                  if (guarded) {
                     AiOperation.Result guardResult = this.load();
                     if (guardResult != null) {
                        return guardResult;
                     }
                  }

                  if (this.init_AiOperation != null) {
                     AiOperation.Result result = this.init_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.init("[" + this.init_aU_StreamUtil.init_aU_BufferHelper.id() + "] " + result.message());
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     boolean succeeded = result.status() == AiOperation.Status.SUCCEEDED;
                     if (result.status() == AiOperation.Status.CANCELLED) {
                        return this.init(AiOperation.Status.CANCELLED, "Plan cancelled during " + this.init_aU_StreamUtil.init_aU_BufferHelper.id());
                     }

                     this.init_Z = result.status() == AiOperation.Status.UNCERTAIN
                        ? aU.init(this.init_aU_StreamUtil, result.status() + ": " + result.message(), policy)
                        : aU.init(this.init_aU_StreamUtil, succeeded, result.status() + ": " + result.message(), policy);
                     String id = this.init_aU_StreamUtil.init_aU_BufferHelper.id();
                     this.init_aU_StreamUtil = null;
                     return this.init_Z
                        ? this.init(AiOperation.Status.FAILED, "Step '" + id + "' failed and onFailure is stop; " + this.init_Str_12())
                        : this.init("Step '" + id + "' " + (succeeded ? "done" : "failed") + "; " + this.init_Str_12());
                  } else {
                     if (aU.init_Z(this.init_List)) {
                        return this.init_AiOperation_Result_20();
                     }

                     this.init_aU_StreamUtil = aU.init_aU_StreamUtil(this.init_List);
                     if (this.init_aU_StreamUtil == null) {
                        return this.init_AiOperation_Result_20();
                     }

                     this.init_aU_StreamUtil.init_aU_CachePool = aU.Inner_CachePool.load;
                     this.init_aU_StreamUtil.init_I++;

                     try {
                        this.init_AiOperation = cz.init_AiOperation_Str_JsonObject(
                           this.init_aU_StreamUtil.init_aU_BufferHelper.capability(),
                           ag.init_JsonObject_Str(this.init_aU_StreamUtil.init_aU_BufferHelper.argumentsJson()).getAsJsonObject()
                        );
                     } catch (RuntimeException e) {
                        this.init_AiOperation = null;
                        this.init_Z = aU.init(this.init_aU_StreamUtil, false, "rejected: " + e.getMessage(), policy);
                        String id = this.init_aU_StreamUtil.init_aU_BufferHelper.id();
                        this.init_aU_StreamUtil = null;
                        if (this.init_Z) {
                           return this.init(AiOperation.Status.FAILED, "Step '" + id + "' was rejected and onFailure is stop; " + this.init_Str_12());
                        }

                        return this.init("Step '" + id + "' rejected; " + this.init_Str_12());
                     }

                     if (this.init_AiOperation == null) {
                        this.init_Z = aU.init(
                           this.init_aU_StreamUtil, false, "unknown capability " + this.init_aU_StreamUtil.init_aU_BufferHelper.capability(), policy
                        );
                        String id = this.init_aU_StreamUtil.init_aU_BufferHelper.id();
                        this.init_aU_StreamUtil = null;
                        return this.init_Z
                           ? this.init(AiOperation.Status.FAILED, "Step '" + id + "' names an unknown capability; " + this.init_Str_12())
                           : this.init("Step '" + id + "' names an unknown capability; " + this.init_Str_12());
                     } else {
                        return this.init(
                           "Starting step '"
                              + this.init_aU_StreamUtil.init_aU_BufferHelper.id()
                              + "' ("
                              + this.init_aU_StreamUtil.init_aU_BufferHelper.capability()
                              + ")"
                        );
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Plan cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_20() {
               Map<String, Integer> tally = aU.init_Map(this.init_List);
               int failed = tally.getOrDefault("failed", 0) + tally.getOrDefault("skipped", 0);
               if (failed == 0) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Plan complete: " + this.init_Str_12());
               } else {
                  return tally.getOrDefault("done", 0) == 0
                     ? this.init(AiOperation.Status.FAILED, "No plan step completed; " + this.init_Str_12())
                     : this.init(AiOperation.Status.UNCERTAIN, "Plan partially complete; " + this.init_Str_12());
               }
            }

            AiOperation.Result load() {
               if (this.load_AiOperation != null) {
                  AiOperation.Result result = this.load_AiOperation.tick();
                  if (!result.terminal()) {
                     return this.init("Interrupted: " + this.init_Str + " - " + result.message());
                  }

                  this.load_AiOperation.cancel();
                  this.load_AiOperation = null;
                  return this.init("Resuming the plan after " + this.init_Str);
               } else {
                  if (this.init_AiOperation != null) {
                     return null;
                  }

                  X.Inner_BufferHelper vitals = X.init_X_BufferHelper();
                  AiOperation action = X.init_AiOperation_X_CachePool(this.init_X_CachePool, vitals, this.init_I_21());
                  if (action == null) {
                     return null;
                  }

                  this.init_Str = X.init_Str(this.init_X_CachePool, vitals);
                  this.init_I++;
                  this.load_AiOperation = action;
                  return this.init("Interrupted between steps: " + this.init_Str);
               }
            }

            String init_Str_12() {
               Map<String, Integer> tally = aU.init_Map(this.init_List);
               return tally.getOrDefault("done", 0)
                  + " done, "
                  + tally.getOrDefault("failed", 0)
                  + " failed, "
                  + tally.getOrDefault("skipped", 0)
                  + " skipped, "
                  + tally.getOrDefault("pending", 0)
                  + " not reached";
            }

            int init_I_21() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            JsonObject init_JsonObject_35() {
               JsonArray steps = new JsonArray();

               for (aU.Inner_StreamUtil progress : this.init_List) {
                  steps.add(
                     ag.init_JsonObject_ObjArr(
                        "id",
                        progress.init_aU_BufferHelper.id(),
                        "capability",
                        progress.init_aU_BufferHelper.capability(),
                        "state",
                        progress.init_aU_CachePool.name().toLowerCase(Locale.ROOT),
                        "attempts",
                        progress.init_I,
                        "message",
                        progress.init_Str,
                        "needs",
                        ag.init.toJsonTree(progress.init_aU_BufferHelper.needs())
                     )
                  );
               }

               JsonObject out = ag.init_JsonObject_ObjArr("onFailure", policy.name().toLowerCase(Locale.ROOT));
               out.add("steps", steps);
               JsonObject tally = new JsonObject();
               aU.init_Map(this.init_List).forEach(tally::addProperty);
               out.add("tally", tally);
               if (this.init_aU_StreamUtil != null) {
                  out.addProperty("currentStep", this.init_aU_StreamUtil.init_aU_BufferHelper.id());
               }

               out.add("survival", X.init_JsonObject_X_BufferHelper(X.init_X_BufferHelper(), this.init_I, this.init_Str));
               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_35().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               JsonObject data = this.init_JsonObject_35();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.load_Z) {
                  this.load_Z = true;
                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Running a plan requires a connected living player");
      }
   }

   private static List<aU.Inner_StreamUtil> init(List<aU.Inner_BufferHelper> ordered) {
      List<aU.Inner_StreamUtil> plan = new ArrayList<>(ordered.size());

      for (aU.Inner_BufferHelper step : ordered) {
         plan.add(new aU.Inner_StreamUtil(step));
      }

      return plan;
   }

   static List<aU.Inner_BufferHelper> init_List(JsonObject args) {
      if (args.has("steps") && args.get("steps").isJsonArray()) {
         JsonArray raw = args.getAsJsonArray("steps");
         if (raw.isEmpty()) {
            throw new IllegalArgumentException("A plan needs at least one step");
         }

         if (raw.size() > 32) {
            throw new IllegalArgumentException("A plan may hold at most 32 steps; split a longer objective into several plans");
         }

         List<aU.Inner_BufferHelper> steps = new ArrayList<>(raw.size());
         int index = 0;

         for (JsonElement element : raw) {
            JsonObject entry = init(element, ++index);
            String capability = ag.init_Str_JsonObject(entry, "capability", "");
            if (capability.isBlank() && ag.init_Str_JsonObject(entry, "id", "").startsWith("game:")) {
               capability = ag.init_Str_JsonObject(entry, "id", "");
            }

            String id = capability.equals(ag.init_Str_JsonObject(entry, "id", "")) ? "step" + index : ag.init_Str_JsonObject(entry, "id", "step" + index);
            aU.init(capability);
            JsonObject arguments = entry.has("arguments") && entry.get("arguments").isJsonObject() ? entry.getAsJsonObject("arguments") : new JsonObject();
            init(id, capability, arguments);
            List<String> needs = new ArrayList<>();
            if (entry.has("needs") && entry.get("needs").isJsonArray()) {
               for (JsonElement need : entry.getAsJsonArray("needs")) {
                  needs.add(need.getAsString());
               }
            }

            steps.add(new aU.Inner_BufferHelper(id, capability, arguments.toString(), needs));
         }

         return steps;
      } else {
         throw new IllegalArgumentException("steps is required: an array of {id, capability, arguments, needs}");
      }
   }

   private static JsonObject init(JsonElement element, int index) {
      if (element.isJsonObject()) {
         return element.getAsJsonObject();
      }

      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
         List<String> tokens = new ArrayList<>(cy.init_List(element.getAsString().trim()));
         if (!tokens.isEmpty()) {
            LocalPlayer player = Minecraft.getInstance().player;
            String sub = tokens.remove(0).toLowerCase(Locale.ROOT);

            try {
               cy.Inner_StreamUtil call = cy.init(
                  sub, tokens, player == null ? 0.0 : player.getX(), player == null ? 0.0 : player.getY(), player == null ? 0.0 : player.getZ()
               );
               return ag.init_JsonObject_ObjArr("id", "step" + index, "capability", call.id(), "arguments", call.arguments());
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
      AiCatalog.Entry entry = o.init_AiCatalog_Snapshot().get(capability);
      if (entry != null) {
         JsonObject schema = ag.init_JsonObject_Str(entry.contract().inputSchema());
         if (schema.has("type") || schema.has("properties")) {
            try {
               bO.init(schema, arguments);
            } catch (IllegalArgumentException e) {
               throw new IllegalArgumentException("Plan step '" + id + "' (" + capability + "): " + e.getMessage());
            }
         }
      }
   }

   static aU.Inner_DataUtil init(String text) {
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
         return aU.Inner_DataUtil.valueOf(word.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("onFailure must be stop, continue or retry (got '" + text + "')");
      }
   }
}
