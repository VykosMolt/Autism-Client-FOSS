package autismclient.ai;

import autismclient.api.ai.AiCapabilityDescriptor;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismWaypoints;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;

public final class AiMacroTools implements autismclient.util.macro.MacroToolBridge.ToolBackend {
   private static final AiMacroTools INSTANCE = new AiMacroTools();
   private volatile List<autismclient.util.macro.MacroToolBridge.ToolInfo> tools = List.of();
   private volatile List<String> waypointNames = List.of();
   private String catalogRevision = "";
   private final ArrayDeque<AiMacroTools.ToolRequest> pending = new ArrayDeque<>();
   private AiMacroTools.ToolRequest request;

   private AiMacroTools() {
   }

   public static void install() {
      autismclient.util.macro.MacroToolBridge.setBackend(INSTANCE);
      INSTANCE.refresh(Minecraft.getInstance());
   }

   @Override
   public List<autismclient.util.macro.MacroToolBridge.ToolInfo> tools() {
      return this.tools;
   }

   @Override
   public List<String> toolIds() {
      return this.waypointNames;
   }

   @Override
   public autismclient.util.macro.MacroToolBridge.ToolJob currentJob() {
      return AiDirectOps.currentHandle();
   }

   static boolean isMacroCallableTool(String id) {
      return AiDirectOps.isToolId(id) && !"client:run_macro".equals(id);
   }

   private void refresh(Minecraft mc) {
      AiCatalog.Snapshot snapshot = AiDirectOps.catalogSnapshot();
      if (!this.catalogRevision.equals(snapshot.revision())) {
         this.tools = snapshot.entries()
            .stream()
            .filter(e -> isMacroCallableTool(e.contract().id()) && e.contract().classification() != AiCapabilityDescriptor.Classification.DISCOVER_ONLY)
            .map(
               e -> new autismclient.util.macro.MacroToolBridge.ToolInfo(e.contract().id(), toolLabel(e.contract().id()), withStrategyEnum(e.contract().inputSchema()))
            )
            .sorted(Comparator.comparing(autismclient.util.macro.MacroToolBridge.ToolInfo::load).thenComparing(autismclient.util.macro.MacroToolBridge.ToolInfo::init))
            .toList();
         this.catalogRevision = snapshot.revision();
      }

      this.waypointNames = AutismWaypoints.getInstance().list(AuditWaypointScope.current(mc)).stream().map(AutismWaypoints.Waypoint::name).toList();
   }

   static String withStrategyEnum(String schemaJson) {
      JsonObject schema = JsonParser.parseString(schemaJson).getAsJsonObject();
      JsonObject properties = schema.getAsJsonObject("properties");
      if (properties != null && properties.has("strategy")) {
         JsonObject strategy = properties.getAsJsonObject("strategy");
         if (!strategy.has("enum")) {
            JsonArray options = new JsonArray();
            AiToolSettings.STRATEGIES.stream().sorted().forEach(options::add);
            strategy.add("enum", options);
         }
      }

      return schema.toString();
   }

   static String toolLabel(String id) {
      String name = id.substring(id.indexOf(58) + 1).replace('_', ' ');
      if (name.equals("goto")) {
         name = "go to";
      }

      StringBuilder label = new StringBuilder();

      for (String word : name.split(" ")) {
         if (!word.isBlank()) {
            if (!label.isEmpty()) {
               label.append(' ');
            }

            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
         }
      }

      if (id.startsWith("client:")) {
         label.append(" (Client)");
      }

      return label.toString();
   }

   @Override
   public autismclient.util.macro.MacroToolBridge.ToolJob start(String id, String argumentsJson) {
      if (!isMacroCallableTool(id)) {
         throw new IllegalArgumentException("Choose a direct tool; use Start Macro for another macro");
      }

      AiMacroTools.ToolRequest request = new AiMacroTools.ToolRequest(id, argumentsJson);
      request.job.onCancel(() -> AiClientTick.execute(() -> this.cancelRequest(request)));
      AiClientTick.execute(() -> {
         if (!request.job.future().isDone()) {
            request.startLevel = Minecraft.getInstance().level;
            this.pending.add(request);
         }
      });
      return request.job;
   }

   private void cancelRequest(AiMacroTools.ToolRequest request) {
      this.pending.remove(request);
      if (request.load == null) {
         request.job.publish(autismclient.util.macro.MacroToolBridge.JobState.CANCELLED, "Tool cancelled before starting", "{}");
      } else {
         AiDirectOps.cancelJob(request.load);
      }
   }

   static void cancelAll(String reason) {
      List<AiMacroTools.ToolRequest> cancelled = List.copyOf(INSTANCE.pending);
      INSTANCE.pending.clear();

      for (AiMacroTools.ToolRequest request : cancelled) {
         request.job.publish(autismclient.util.macro.MacroToolBridge.JobState.CANCELLED, reason, "{}");
      }
   }

   @Override
   public CompletableFuture<autismclient.util.macro.MacroToolBridge.JobSnapshot> control(autismclient.util.macro.MacroToolBridge.ToolJob job, String operation) {
      if (job.future().isDone()) {
         return job.future();
      } else if (!List.of("STOP", "PAUSE", "RESUME").contains(operation)) {
         return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown tool operation: " + operation));
      } else if ("STOP".equals(operation)) {
         AiClientTick.execute(() -> {
            if (this.request != null && this.request.load == job) {
               this.request.job.cancel();
            } else {
               job.cancel();
            }
         });
         return job.future();
      } else {
         return AiClientTick.supply(
            () -> {
               AiMacroTools.ToolRequest queued = this.pending.stream().filter(r -> r.job == job).findFirst().orElse(null);
               if (queued != null) {
                  queued.paused = "PAUSE".equals(operation);
                  queued.job
                     .publish(
                        queued.paused ? autismclient.util.macro.MacroToolBridge.JobState.PAUSED : autismclient.util.macro.MacroToolBridge.JobState.WAITING,
                        queued.paused ? "Paused before starting" : "Waiting for tool runner",
                        "{}"
                     );
                  return queued.job.snapshot();
               }

               autismclient.util.macro.MacroToolBridge.ToolJob inner = this.request != null && this.request.job == job
                  ? this.request.load
                  : job;
               if (AiDirectOps.currentHandle() == inner && AiDirectOps.isRunning()) {
                  if ("PAUSE".equals(operation)) {
                     AiDirectOps.pause();
                  } else {
                     if (!"RESUME".equals(operation)) {
                        throw new IllegalArgumentException("Unknown tool operation: " + operation);
                     }

                     AiDirectOps.resume();
                  }

                  if (this.request != null && this.request.load == inner) {
                     publishSnapshot(this.request);
                  }

                  return inner.snapshot();
               } else {
                  throw new IllegalStateException("That tool invocation is no longer active");
               }
            }
         );
      }
   }

   private static void publishSnapshot(AiMacroTools.ToolRequest request) {
      autismclient.util.macro.MacroToolBridge.JobSnapshot result = request.load.snapshot();
      request.job.publish(result.state(), result.message(), result.load());
   }

   public static void tick(Minecraft mc) {
      INSTANCE.refresh(mc);
      if (INSTANCE.request != null) {
         publishSnapshot(INSTANCE.request);
         if (INSTANCE.request.job.future().isDone()) {
            INSTANCE.request = null;
         }
      }

      INSTANCE.pending.removeIf(requestx -> {
         if (requestx.startLevel != null && requestx.startLevel != mc.level) {
            requestx.job.publish(autismclient.util.macro.MacroToolBridge.JobState.CANCELLED, "World changed before tool started", "{}");
            return true;
         } else {
            return requestx.job.future().isDone();
         }
      });
      if (INSTANCE.request == null && !AiDirectOps.isRunning()) {
         AiMacroTools.ToolRequest request = INSTANCE.pending.peek();
         if (request != null && !request.paused) {
            INSTANCE.pending.remove();

            try {
               request.load = AiDirectOps.start(request.job.toolId(), JsonParser.parseString(request.arguments).getAsJsonObject());
               INSTANCE.request = request;
               request.load.future().whenComplete((outcome, error) -> {
                  if (error != null) {
                     request.job.publish(autismclient.util.macro.MacroToolBridge.JobState.FAILED, "Tool completion failed", "{}");
                  } else {
                     request.job.publish(outcome.state(), outcome.message(), outcome.load());
                  }
               });
               publishSnapshot(request);
            } catch (Exception error) {
               request.job
                  .publish(autismclient.util.macro.MacroToolBridge.JobState.FAILED, error.getMessage() == null ? "Tool could not start" : error.getMessage(), "{}");
            }
         }
      }
   }

   private static final class ToolRequest {
      final autismclient.util.macro.MacroToolBridge.ToolJob job;
      final String arguments;
      Object startLevel;
      autismclient.util.macro.MacroToolBridge.ToolJob load;
      boolean paused;

      ToolRequest(String id, String arguments) {
         this.job = new autismclient.util.macro.MacroToolBridge.ToolJob(id);
         this.arguments = arguments;
      }
   }
}
