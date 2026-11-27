package autismclient.util.macro;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class MacroToolBridge {
   private static final Map<Long, MacroToolBridge.Inner_BufferHelper> SESSIONS = new ConcurrentHashMap<>();
   private static volatile MacroToolBridge.Inner_DataUtil bridge;

   private MacroToolBridge() {
   }

   public static void init(MacroToolBridge.Inner_DataUtil value) {
      bridge = value;
   }

   public static boolean isAvailable() {
      return bridge != null;
   }

   public static List<MacroToolBridge.Inner_TaskQueue> listTools() {
      MacroToolBridge.Inner_DataUtil b = bridge;
      return b == null ? List.of() : List.copyOf(b.tools());
   }

   public static List<String> load() {
      MacroToolBridge.Inner_DataUtil b = bridge;
      return b == null ? List.of() : List.copyOf(b.load());
   }

   public static MacroToolBridge.Inner_TaskQueue findTool(String id) {
      return listTools().stream().filter(t -> t.init().equals(id)).findFirst().orElse(null);
   }

   public static String toolLabel(String id) {
      MacroToolBridge.Inner_TaskQueue t = findTool(id);
      return t == null ? id : t.load();
   }

   public static MacroToolBridge.Inner_StreamUtil startTool(long owner, String tool, String argumentsJson) {
      MacroToolBridge.Inner_DataUtil b = bridge;
      if (b == null) {
         throw new IllegalStateException("Tools requires the full client");
      }

      MacroToolBridge.Inner_StreamUtil job = b.init(tool, argumentsJson);
      MacroToolBridge.Inner_BufferHelper session = SESSIONS.computeIfAbsent(owner, ignored -> new MacroToolBridge.Inner_BufferHelper());
      session.lastJob = job;
      session.jobs.put(job.id(), job);
      job.load().whenComplete((result, failure) -> session.jobs.remove(job.id()));
      return job;
   }

   public static MacroToolBridge.Inner_StreamUtil load(long owner, String scope, String filter) {
      MacroToolBridge.Inner_DataUtil b = bridge;
      if (b == null) {
         return null;
      }

      MacroToolBridge.Inner_BufferHelper session = SESSIONS.get(owner);
      MacroToolBridge.Inner_StreamUtil job = "ANY_TOOL".equals(scope) ? b.currentJob() : (session == null ? null : session.lastJob);
      return job != null && (filter == null || filter.isBlank() || filter.equals(job.toolId())) ? job : null;
   }

   public static CompletableFuture<MacroToolBridge.Inner_CachePool> runOperation(long owner, String scope, String operation) {
      return init(load(owner, scope, ""), operation);
   }

   public static CompletableFuture<MacroToolBridge.Inner_CachePool> init(MacroToolBridge.Inner_StreamUtil job, String operation) {
      MacroToolBridge.Inner_DataUtil b = bridge;
      if (b == null) {
         return CompletableFuture.failedFuture(new IllegalStateException("Tools requires the full client"));
      } else if (job == null) {
         return CompletableFuture.failedFuture(new IllegalStateException("No matching tool has started"));
      } else {
         return "WAIT".equals(operation) ? job.load() : b.init(job, operation);
      }
   }

   public static boolean init(long owner, String scope, String filter, String state) {
      if (!isAvailable()) {
         return false;
      }

      MacroToolBridge.Inner_StreamUtil job = load(owner, scope, filter);
      if (job == null) {
         return "IDLE".equals(state);
      }

      MacroToolBridge.Inner_ConfigStore actual = job.snapshot().state();
      boolean var10000;
      switch (state == null ? "" : state) {
         case "FINISHED":
            var10000 = actual.init();
            break;
         case "ACTIVE":
            var10000 = !actual.init();
            break;
         case "IDLE":
            var10000 = actual.init();
            break;
         case "FAILED":
            var10000 = actual == MacroToolBridge.Inner_ConfigStore.FAILED || actual == MacroToolBridge.Inner_ConfigStore.TIMED_OUT || actual == MacroToolBridge.Inner_ConfigStore.UNCERTAIN;
            break;
         default:
            var10000 = actual.name().equals(state);
      }

      return var10000;
   }

   public static void init(long owner, boolean cancelled) {
      MacroToolBridge.Inner_BufferHelper session = SESSIONS.remove(owner);
      if (session != null && cancelled) {
         for (MacroToolBridge.Inner_StreamUtil job : session.jobs.values()) {
            job.cancel();
         }
      }
   }

   private static final class Inner_BufferHelper {
      volatile MacroToolBridge.Inner_StreamUtil lastJob;
      final Map<Long, MacroToolBridge.Inner_StreamUtil> jobs = new ConcurrentHashMap<>();
   }

   public record Inner_CachePool(MacroToolBridge.Inner_ConfigStore state, String message, String load) {
      public boolean isSucceeded() {
         return this.state == MacroToolBridge.Inner_ConfigStore.SUCCEEDED;
      }
   }

   public enum Inner_ConfigStore {
      IDLE,
      WAITING,
      RUNNING,
      PAUSED,
      SUCCEEDED,
      FAILED,
      CANCELLED,
      TIMED_OUT,
      UNCERTAIN;

      public boolean init() {
         return this.ordinal() >= SUCCEEDED.ordinal();
      }
   }

   public interface Inner_DataUtil {
      List<MacroToolBridge.Inner_TaskQueue> tools();

      List<String> load();

      MacroToolBridge.Inner_StreamUtil init(String var1, String var2);

      MacroToolBridge.Inner_StreamUtil currentJob();

      CompletableFuture<MacroToolBridge.Inner_CachePool> init(MacroToolBridge.Inner_StreamUtil var1, String var2);
   }

   public static final class Inner_StreamUtil {
      private static final AtomicLong NEXT_ID = new AtomicLong();
      private final long id = NEXT_ID.incrementAndGet();
      private final String toolId;
      private final AtomicReference<MacroToolBridge.Inner_CachePool> state = new AtomicReference<>(
         new MacroToolBridge.Inner_CachePool(MacroToolBridge.Inner_ConfigStore.WAITING, "Waiting", "{}")
      );
      private final CompletableFuture<Void> started = new CompletableFuture<>();
      private final CompletableFuture<MacroToolBridge.Inner_CachePool> load = new CompletableFuture<>();
      private Runnable cancelAction;
      private boolean cancelRequested;

      public Inner_StreamUtil(String tool) {
         this.toolId = tool == null ? "" : tool;
      }

      public long id() {
         return this.id;
      }

      public String toolId() {
         return this.toolId;
      }

      public MacroToolBridge.Inner_CachePool snapshot() {
         return this.state.get();
      }

      public CompletableFuture<Void> startedFuture() {
         return this.started;
      }

      public CompletableFuture<MacroToolBridge.Inner_CachePool> load() {
         return this.load;
      }

      public synchronized boolean isCancelRequested() {
         return this.cancelRequested;
      }

      public void init(Runnable action) {
         boolean invoke;
         synchronized (this) {
            this.cancelAction = action;
            invoke = this.cancelRequested && !this.load.isDone();
         }

         if (invoke && action != null) {
            action.run();
         }
      }

      public void cancel() {
         Runnable action;
         synchronized (this) {
            if (this.load.isDone() || this.cancelRequested) {
               return;
            }

            this.cancelRequested = true;
            action = this.cancelAction;
         }

         if (action != null) {
            action.run();
         }
      }

      public synchronized void init(MacroToolBridge.Inner_ConfigStore state, String message, String data) {
         if (state != null && !this.load.isDone()) {
            MacroToolBridge.Inner_CachePool next = new MacroToolBridge.Inner_CachePool(state, message == null ? "" : message, data == null ? "{}" : data);
            this.state.set(next);
            if (state == MacroToolBridge.Inner_ConfigStore.RUNNING || state.init()) {
               this.started.complete(null);
            }

            if (state.init()) {
               this.load.complete(next);
            }
         }
      }
   }

   public record Inner_TaskQueue(String init, String load, String read) {
   }
}
