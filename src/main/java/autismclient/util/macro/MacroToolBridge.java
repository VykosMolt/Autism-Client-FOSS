package autismclient.util.macro;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class MacroToolBridge {
   private static final Map<Long, MacroToolBridge.ToolSession> SESSIONS = new ConcurrentHashMap<>();
   private static volatile MacroToolBridge.ToolBackend bridge;

   private MacroToolBridge() {
   }

   public static void setBackend(MacroToolBridge.ToolBackend value) {
      bridge = value;
   }

   public static boolean isAvailable() {
      return bridge != null;
   }

   public static List<MacroToolBridge.ToolInfo> listTools() {
      MacroToolBridge.ToolBackend b = bridge;
      return b == null ? List.of() : List.copyOf(b.tools());
   }

   public static List<String> waypointNames() {
      MacroToolBridge.ToolBackend b = bridge;
      return b == null ? List.of() : List.copyOf(b.waypointNames());
   }

   public static MacroToolBridge.ToolInfo findTool(String id) {
      return listTools().stream().filter(t -> t.id().equals(id)).findFirst().orElse(null);
   }

   public static String toolLabel(String id) {
      MacroToolBridge.ToolInfo t = findTool(id);
      return t == null ? id : t.name();
   }

   public static MacroToolBridge.ToolJob startTool(long owner, String tool, String argumentsJson) {
      MacroToolBridge.ToolBackend b = bridge;
      if (b == null) {
         throw new IllegalStateException("Tools requires the full client");
      }

      MacroToolBridge.ToolJob job = b.start(tool, argumentsJson);
      MacroToolBridge.ToolSession session = SESSIONS.computeIfAbsent(owner, ignored -> new MacroToolBridge.ToolSession());
      session.lastJob = job;
      session.jobs.put(job.id(), job);
      job.future().whenComplete((result, failure) -> session.jobs.remove(job.id()));
      return job;
   }

   public static MacroToolBridge.ToolJob start(long owner, String scope, String filter) {
      MacroToolBridge.ToolBackend b = bridge;
      if (b == null) {
         return null;
      }

      MacroToolBridge.ToolSession session = SESSIONS.get(owner);
      MacroToolBridge.ToolJob job = "ANY_TOOL".equals(scope) ? b.currentJob() : (session == null ? null : session.lastJob);
      return job != null && (filter == null || filter.isBlank() || filter.equals(job.toolId())) ? job : null;
   }

   public static CompletableFuture<MacroToolBridge.JobSnapshot> runOperation(long owner, String scope, String operation) {
      return control(start(owner, scope, ""), operation);
   }

   public static CompletableFuture<MacroToolBridge.JobSnapshot> control(MacroToolBridge.ToolJob job, String operation) {
      MacroToolBridge.ToolBackend b = bridge;
      if (b == null) {
         return CompletableFuture.failedFuture(new IllegalStateException("Tools requires the full client"));
      } else if (job == null) {
         return CompletableFuture.failedFuture(new IllegalStateException("No matching tool has started"));
      } else {
         return "WAIT".equals(operation) ? job.future() : b.control(job, operation);
      }
   }

   public static boolean matchesJobState(long owner, String scope, String filter, String state) {
      if (!isAvailable()) {
         return false;
      }

      MacroToolBridge.ToolJob job = start(owner, scope, filter);
      if (job == null) {
         return "IDLE".equals(state);
      }

      MacroToolBridge.JobState actual = job.snapshot().state();
      boolean var10000;
      switch (state == null ? "" : state) {
         case "FINISHED":
            var10000 = actual.isTerminal();
            break;
         case "ACTIVE":
            var10000 = !actual.isTerminal();
            break;
         case "IDLE":
            var10000 = actual.isTerminal();
            break;
         case "FAILED":
            var10000 = actual == MacroToolBridge.JobState.FAILED || actual == MacroToolBridge.JobState.TIMED_OUT || actual == MacroToolBridge.JobState.UNCERTAIN;
            break;
         default:
            var10000 = actual.name().equals(state);
      }

      return var10000;
   }

   public static void endSession(long owner, boolean cancelled) {
      MacroToolBridge.ToolSession session = SESSIONS.remove(owner);
      if (session != null && cancelled) {
         for (MacroToolBridge.ToolJob job : session.jobs.values()) {
            job.cancel();
         }
      }
   }

   private static final class ToolSession {
      volatile MacroToolBridge.ToolJob lastJob;
      final Map<Long, MacroToolBridge.ToolJob> jobs = new ConcurrentHashMap<>();
   }

   public record JobSnapshot(MacroToolBridge.JobState state, String message, String data) {
      public boolean isSucceeded() {
         return this.state == MacroToolBridge.JobState.SUCCEEDED;
      }
   }

   public enum JobState {
      IDLE,
      WAITING,
      RUNNING,
      PAUSED,
      SUCCEEDED,
      FAILED,
      CANCELLED,
      TIMED_OUT,
      UNCERTAIN;

      public boolean isTerminal() {
         return this.ordinal() >= SUCCEEDED.ordinal();
      }
   }

   public interface ToolBackend {
      List<MacroToolBridge.ToolInfo> tools();

      List<String> waypointNames();

      MacroToolBridge.ToolJob start(String var1, String var2);

      MacroToolBridge.ToolJob currentJob();

      CompletableFuture<MacroToolBridge.JobSnapshot> control(MacroToolBridge.ToolJob var1, String var2);
   }

   public static final class ToolJob {
      private static final AtomicLong NEXT_ID = new AtomicLong();
      private final long id = NEXT_ID.incrementAndGet();
      private final String toolId;
      private final AtomicReference<MacroToolBridge.JobSnapshot> state = new AtomicReference<>(
         new MacroToolBridge.JobSnapshot(MacroToolBridge.JobState.WAITING, "Waiting", "{}")
      );
      private final CompletableFuture<Void> started = new CompletableFuture<>();
      private final CompletableFuture<MacroToolBridge.JobSnapshot> completion = new CompletableFuture<>();
      private Runnable cancelAction;
      private boolean cancelRequested;

      public ToolJob(String tool) {
         this.toolId = tool == null ? "" : tool;
      }

      public long id() {
         return this.id;
      }

      public String toolId() {
         return this.toolId;
      }

      public MacroToolBridge.JobSnapshot snapshot() {
         return this.state.get();
      }

      public CompletableFuture<Void> startedFuture() {
         return this.started;
      }

      public CompletableFuture<MacroToolBridge.JobSnapshot> future() {
         return this.completion;
      }

      public synchronized boolean isCancelRequested() {
         return this.cancelRequested;
      }

      public void onCancel(Runnable action) {
         boolean invoke;
         synchronized (this) {
            this.cancelAction = action;
            invoke = this.cancelRequested && !this.completion.isDone();
         }

         if (invoke && action != null) {
            action.run();
         }
      }

      public void cancel() {
         Runnable action;
         synchronized (this) {
            if (this.completion.isDone() || this.cancelRequested) {
               return;
            }

            this.cancelRequested = true;
            action = this.cancelAction;
         }

         if (action != null) {
            action.run();
         }
      }

      public synchronized void publish(MacroToolBridge.JobState state, String message, String data) {
         if (state != null && !this.completion.isDone()) {
            MacroToolBridge.JobSnapshot next = new MacroToolBridge.JobSnapshot(state, message == null ? "" : message, data == null ? "{}" : data);
            this.state.set(next);
            if (state == MacroToolBridge.JobState.RUNNING || state.isTerminal()) {
               this.started.complete(null);
            }

            if (state.isTerminal()) {
               this.completion.complete(next);
            }
         }
      }
   }

   public record ToolInfo(String id, String name, String inputSchema) {
   }
}
