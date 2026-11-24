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

public final class am implements autismclient.util.macro.k.Inner_DataUtil {
   private static final am init_am = new am();
   private volatile List<autismclient.util.macro.k.Inner_TaskQueue> init_List = List.of();
   private volatile List<String> load = List.of();
   private String init_Str = "";
   private final ArrayDeque<am.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>();
   private am.Inner_DataUtil init_am_DataUtil;

   private am() {
   }

   public static void init_V() {
      autismclient.util.macro.k.init(init_am);
      init_am.load_V_Minecraft(Minecraft.getInstance());
   }

   @Override
   public List<autismclient.util.macro.k.Inner_TaskQueue> init_List() {
      return this.init_List;
   }

   @Override
   public List<String> load() {
      return this.load;
   }

   @Override
   public autismclient.util.macro.k.Inner_StreamUtil init_k_StreamUtil() {
      return o.init_k_StreamUtil();
   }

   static boolean init_Z_Str(String id) {
      return o.load_Z(id) && !"client:run_macro".equals(id);
   }

   private void load_V_Minecraft(Minecraft mc) {
      AiCatalog.Snapshot snapshot = o.init_AiCatalog_Snapshot();
      if (!this.init_Str.equals(snapshot.revision())) {
         this.init_List = snapshot.entries()
            .stream()
            .filter(e -> init_Z_Str(e.contract().id()) && e.contract().classification() != AiCapabilityDescriptor.Classification.DISCOVER_ONLY)
            .map(
               e -> new autismclient.util.macro.k.Inner_TaskQueue(e.contract().id(), load_Str_Str(e.contract().id()), init_Str_Str(e.contract().inputSchema()))
            )
            .sorted(Comparator.comparing(autismclient.util.macro.k.Inner_TaskQueue::load).thenComparing(autismclient.util.macro.k.Inner_TaskQueue::init))
            .toList();
         this.init_Str = snapshot.revision();
      }

      this.load = AutismWaypoints.init_AutismWaypoints().init_List_Str(AuditWaypointScope.current(mc)).stream().map(AutismWaypoints.Waypoint::name).toList();
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
   public autismclient.util.macro.k.Inner_StreamUtil init(String id, String argumentsJson) {
      if (!init_Z_Str(id)) {
         throw new IllegalArgumentException("Choose a direct tool; use Start Macro for another macro");
      }

      am.Inner_DataUtil request = new am.Inner_DataUtil(id, argumentsJson);
      request.init_k_StreamUtil.init(() -> ClassCache.init_V_Runnable(() -> this.init_V_am_DataUtil(request)));
      ClassCache.init_V_Runnable(() -> {
         if (!request.init_k_StreamUtil.load().isDone()) {
            request.init_Obj = Minecraft.getInstance().level;
            this.init_ArrayDeque.add(request);
         }
      });
      return request.init_k_StreamUtil;
   }

   private void init_V_am_DataUtil(am.Inner_DataUtil request) {
      this.init_ArrayDeque.remove(request);
      if (request.load == null) {
         request.init_k_StreamUtil.init(autismclient.util.macro.k.Inner_ConfigStore.flush, "Tool cancelled before starting", "{}");
      } else {
         o.init(request.load);
      }
   }

   static void init_V_Str(String reason) {
      List<am.Inner_DataUtil> cancelled = List.copyOf(init_am.init_ArrayDeque);
      init_am.init_ArrayDeque.clear();

      for (am.Inner_DataUtil request : cancelled) {
         request.init_k_StreamUtil.init(autismclient.util.macro.k.Inner_ConfigStore.flush, reason, "{}");
      }
   }

   @Override
   public CompletableFuture<autismclient.util.macro.k.Inner_CachePool> init(autismclient.util.macro.k.Inner_StreamUtil job, String operation) {
      if (job.load().isDone()) {
         return job.load();
      } else if (!List.of("STOP", "PAUSE", "RESUME").contains(operation)) {
         return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown tool operation: " + operation));
      } else if ("STOP".equals(operation)) {
         ClassCache.init_V_Runnable(() -> {
            if (this.init_am_DataUtil != null && this.init_am_DataUtil.load == job) {
               this.init_am_DataUtil.init_k_StreamUtil.init_V();
            } else {
               job.init_V();
            }
         });
         return job.load();
      } else {
         return ClassCache.init_CompletableFuture_Supplier(
            () -> {
               am.Inner_DataUtil queued = this.init_ArrayDeque.stream().filter(r -> r.init_k_StreamUtil == job).findFirst().orElse(null);
               if (queued != null) {
                  queued.init_Z = "PAUSE".equals(operation);
                  queued.init_k_StreamUtil
                     .init(
                        queued.init_Z ? autismclient.util.macro.k.Inner_ConfigStore.write : autismclient.util.macro.k.Inner_ConfigStore.load,
                        queued.init_Z ? "Paused before starting" : "Waiting for tool runner",
                        "{}"
                     );
                  return queued.init_k_StreamUtil.init_k_CachePool();
               }

               autismclient.util.macro.k.Inner_StreamUtil inner = this.init_am_DataUtil != null && this.init_am_DataUtil.init_k_StreamUtil == job
                  ? this.init_am_DataUtil.load
                  : job;
               if (o.init_k_StreamUtil() == inner && o.init_Z()) {
                  if ("PAUSE".equals(operation)) {
                     o.read_V();
                  } else {
                     if (!"RESUME".equals(operation)) {
                        throw new IllegalArgumentException("Unknown tool operation: " + operation);
                     }

                     o.write_V();
                  }

                  if (this.init_am_DataUtil != null && this.init_am_DataUtil.load == inner) {
                     load_V_am_DataUtil(this.init_am_DataUtil);
                  }

                  return inner.init_k_CachePool();
               } else {
                  throw new IllegalStateException("That tool invocation is no longer active");
               }
            }
         );
      }
   }

   private static void load_V_am_DataUtil(am.Inner_DataUtil request) {
      autismclient.util.macro.k.Inner_CachePool result = request.load.init_k_CachePool();
      request.init_k_StreamUtil.init(result.init_k_ConfigStore(), result.init_Str(), result.load());
   }

   public static void init_V_Minecraft(Minecraft mc) {
      init_am.load_V_Minecraft(mc);
      if (init_am.init_am_DataUtil != null) {
         load_V_am_DataUtil(init_am.init_am_DataUtil);
         if (init_am.init_am_DataUtil.init_k_StreamUtil.load().isDone()) {
            init_am.init_am_DataUtil = null;
         }
      }

      init_am.init_ArrayDeque.removeIf(requestx -> {
         if (requestx.init_Obj != null && requestx.init_Obj != mc.level) {
            requestx.init_k_StreamUtil.init(autismclient.util.macro.k.Inner_ConfigStore.flush, "World changed before tool started", "{}");
            return true;
         } else {
            return requestx.init_k_StreamUtil.load().isDone();
         }
      });
      if (init_am.init_am_DataUtil == null && !o.init_Z()) {
         am.Inner_DataUtil request = init_am.init_ArrayDeque.peek();
         if (request != null && !request.init_Z) {
            init_am.init_ArrayDeque.remove();

            try {
               request.load = o.init_k_StreamUtil(request.init_k_StreamUtil.init_Str(), JsonParser.parseString(request.init_Str).getAsJsonObject());
               init_am.init_am_DataUtil = request;
               request.load.load().whenComplete((outcome, error) -> {
                  if (error != null) {
                     request.init_k_StreamUtil.init(autismclient.util.macro.k.Inner_ConfigStore.sync, "Tool completion failed", "{}");
                  } else {
                     request.init_k_StreamUtil.init(outcome.init_k_ConfigStore(), outcome.init_Str(), outcome.load());
                  }
               });
               load_V_am_DataUtil(request);
            } catch (Exception error) {
               request.init_k_StreamUtil
                  .init(autismclient.util.macro.k.Inner_ConfigStore.sync, error.getMessage() == null ? "Tool could not start" : error.getMessage(), "{}");
            }
         }
      }
   }

   private static final class Inner_DataUtil {
      final autismclient.util.macro.k.Inner_StreamUtil init_k_StreamUtil;
      final String init_Str;
      Object init_Obj;
      autismclient.util.macro.k.Inner_StreamUtil load;
      boolean init_Z;

      Inner_DataUtil(String id, String arguments) {
         this.init_k_StreamUtil = new autismclient.util.macro.k.Inner_StreamUtil(id);
         this.init_Str = arguments;
      }
   }
}
