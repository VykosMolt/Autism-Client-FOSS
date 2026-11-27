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

public final class AiMacroTools implements autismclient.util.macro.MacroToolBridge.Inner_DataUtil {
   private static final AiMacroTools init_am = new AiMacroTools();
   private volatile List<autismclient.util.macro.MacroToolBridge.Inner_TaskQueue> tools = List.of();
   private volatile List<String> load = List.of();
   private String init_Str = "";
   private final ArrayDeque<AiMacroTools.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>();
   private AiMacroTools.Inner_DataUtil request;

   private AiMacroTools() {
   }

   public static void install() {
      autismclient.util.macro.MacroToolBridge.init(init_am);
      init_am.load_V_Minecraft(Minecraft.getInstance());
   }

   @Override
   public List<autismclient.util.macro.MacroToolBridge.Inner_TaskQueue> tools() {
      return this.tools;
   }

   @Override
   public List<String> load() {
      return this.load;
   }

   @Override
   public autismclient.util.macro.MacroToolBridge.Inner_StreamUtil currentJob() {
      return AiDirectOps.currentHandle();
   }

   static boolean isMacroCallableTool(String id) {
      return AiDirectOps.isToolId(id) && !"client:run_macro".equals(id);
   }

   private void load_V_Minecraft(Minecraft mc) {
      AiCatalog.Snapshot snapshot = AiDirectOps.catalogSnapshot();
      if (!this.init_Str.equals(snapshot.revision())) {
         this.tools = snapshot.entries()
            .stream()
            .filter(e -> isMacroCallableTool(e.contract().id()) && e.contract().classification() != AiCapabilityDescriptor.Classification.DISCOVER_ONLY)
            .map(
               e -> new autismclient.util.macro.MacroToolBridge.Inner_TaskQueue(e.contract().id(), load_Str_Str(e.contract().id()), init_Str_Str(e.contract().inputSchema()))
            )
            .sorted(Comparator.comparing(autismclient.util.macro.MacroToolBridge.Inner_TaskQueue::load).thenComparing(autismclient.util.macro.MacroToolBridge.Inner_TaskQueue::init))
            .toList();
         this.init_Str = snapshot.revision();
      }

      this.load = AutismWaypoints.getInstance().list(AuditWaypointScope.current(mc)).stream().map(AutismWaypoints.Waypoint::name).toList();
   }

   static String init_Str_Str(String schemaJson) {
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

   static String load_Str_Str(String id) {
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
   public autismclient.util.macro.MacroToolBridge.Inner_StreamUtil init(String id, String argumentsJson) {
      if (!isMacroCallableTool(id)) {
         throw new IllegalArgumentException("Choose a direct tool; use Start Macro for another macro");
      }

      AiMacroTools.Inner_DataUtil request = new AiMacroTools.Inner_DataUtil(id, argumentsJson);
      request.init_k_StreamUtil.init(() -> AiClientTick.execute(() -> this.init_V_am_DataUtil(request)));
      AiClientTick.execute(() -> {
         if (!request.init_k_StreamUtil.load().isDone()) {
            request.init_Obj = Minecraft.getInstance().level;
            this.init_ArrayDeque.add(request);
         }
      });
      return request.init_k_StreamUtil;
   }

   private void init_V_am_DataUtil(AiMacroTools.Inner_DataUtil request) {
      this.init_ArrayDeque.remove(request);
      if (request.load == null) {
         request.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.CANCELLED, "Tool cancelled before starting", "{}");
      } else {
         AiDirectOps.init(request.load);
      }
   }

   static void cancelAll(String reason) {
      List<AiMacroTools.Inner_DataUtil> cancelled = List.copyOf(init_am.init_ArrayDeque);
      init_am.init_ArrayDeque.clear();

      for (AiMacroTools.Inner_DataUtil request : cancelled) {
         request.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.CANCELLED, reason, "{}");
      }
   }

   @Override
   public CompletableFuture<autismclient.util.macro.MacroToolBridge.Inner_CachePool> init(autismclient.util.macro.MacroToolBridge.Inner_StreamUtil job, String operation) {
      if (job.load().isDone()) {
         return job.load();
      } else if (!List.of("STOP", "PAUSE", "RESUME").contains(operation)) {
         return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown tool operation: " + operation));
      } else if ("STOP".equals(operation)) {
         AiClientTick.execute(() -> {
            if (this.request != null && this.request.load == job) {
               this.request.init_k_StreamUtil.cancel();
            } else {
               job.cancel();
            }
         });
         return job.load();
      } else {
         return AiClientTick.supply(
            () -> {
               AiMacroTools.Inner_DataUtil queued = this.init_ArrayDeque.stream().filter(r -> r.init_k_StreamUtil == job).findFirst().orElse(null);
               if (queued != null) {
                  queued.init_Z = "PAUSE".equals(operation);
                  queued.init_k_StreamUtil
                     .init(
                        queued.init_Z ? autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.PAUSED : autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.WAITING,
                        queued.init_Z ? "Paused before starting" : "Waiting for tool runner",
                        "{}"
                     );
                  return queued.init_k_StreamUtil.snapshot();
               }

               autismclient.util.macro.MacroToolBridge.Inner_StreamUtil inner = this.request != null && this.request.init_k_StreamUtil == job
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
                     load_V_am_DataUtil(this.request);
                  }

                  return inner.snapshot();
               } else {
                  throw new IllegalStateException("That tool invocation is no longer active");
               }
            }
         );
      }
   }

   private static void load_V_am_DataUtil(AiMacroTools.Inner_DataUtil request) {
      autismclient.util.macro.MacroToolBridge.Inner_CachePool result = request.load.snapshot();
      request.init_k_StreamUtil.init(result.state(), result.message(), result.load());
   }

   public static void tick(Minecraft mc) {
      init_am.load_V_Minecraft(mc);
      if (init_am.request != null) {
         load_V_am_DataUtil(init_am.request);
         if (init_am.request.init_k_StreamUtil.load().isDone()) {
            init_am.request = null;
         }
      }

      init_am.init_ArrayDeque.removeIf(requestx -> {
         if (requestx.init_Obj != null && requestx.init_Obj != mc.level) {
            requestx.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.CANCELLED, "World changed before tool started", "{}");
            return true;
         } else {
            return requestx.init_k_StreamUtil.load().isDone();
         }
      });
      if (init_am.request == null && !AiDirectOps.isRunning()) {
         AiMacroTools.Inner_DataUtil request = init_am.init_ArrayDeque.peek();
         if (request != null && !request.init_Z) {
            init_am.init_ArrayDeque.remove();

            try {
               request.load = AiDirectOps.start(request.init_k_StreamUtil.toolId(), JsonParser.parseString(request.arguments).getAsJsonObject());
               init_am.request = request;
               request.load.load().whenComplete((outcome, error) -> {
                  if (error != null) {
                     request.init_k_StreamUtil.init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.FAILED, "Tool completion failed", "{}");
                  } else {
                     request.init_k_StreamUtil.init(outcome.state(), outcome.message(), outcome.load());
                  }
               });
               load_V_am_DataUtil(request);
            } catch (Exception error) {
               request.init_k_StreamUtil
                  .init(autismclient.util.macro.MacroToolBridge.Inner_ConfigStore.FAILED, error.getMessage() == null ? "Tool could not start" : error.getMessage(), "{}");
            }
         }
      }
   }

   private static final class Inner_DataUtil {
      final autismclient.util.macro.MacroToolBridge.Inner_StreamUtil init_k_StreamUtil;
      final String arguments;
      Object init_Obj;
      autismclient.util.macro.MacroToolBridge.Inner_StreamUtil load;
      boolean init_Z;

      Inner_DataUtil(String id, String arguments) {
         this.init_k_StreamUtil = new autismclient.util.macro.MacroToolBridge.Inner_StreamUtil(id);
         this.arguments = arguments;
      }
   }
}
