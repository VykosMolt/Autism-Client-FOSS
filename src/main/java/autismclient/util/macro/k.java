package autismclient.util.macro;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class k {
   private static final Map<Long, k.Inner_BufferHelper> init_Map = new ConcurrentHashMap<>();
   private static volatile k.Inner_DataUtil init_k_DataUtil;

   private k() {
   }

   public static void init(k.Inner_DataUtil value) {
      init_k_DataUtil = value;
   }

   public static boolean init_Z() {
      return init_k_DataUtil != null;
   }

   public static List<k.Inner_TaskQueue> init_List() {
      k.Inner_DataUtil b = init_k_DataUtil;
      return b == null ? List.of() : List.copyOf(b.init_List());
   }

   public static List<String> load() {
      k.Inner_DataUtil b = init_k_DataUtil;
      return b == null ? List.of() : List.copyOf(b.load());
   }

   public static k.Inner_TaskQueue init_k_TaskQueue(String id) {
      return init_List().stream().filter(t -> t.init().equals(id)).findFirst().orElse(null);
   }

   public static String init_Str(String id) {
      k.Inner_TaskQueue t = init_k_TaskQueue(id);
      return t == null ? id : t.load();
   }

   public static k.Inner_StreamUtil init_k_StreamUtil(long owner, String tool, String argumentsJson) {
      k.Inner_DataUtil b = init_k_DataUtil;
      if (b == null) {
         throw new IllegalStateException("Tools requires the full client");
      }

      k.Inner_StreamUtil job = b.init(tool, argumentsJson);
      k.Inner_BufferHelper session = init_Map.computeIfAbsent(owner, ignored -> new k.Inner_BufferHelper());
      session.init_k_StreamUtil = job;
      session.init_Map.put(job.init_J(), job);
      job.load().whenComplete((result, failure) -> session.init_Map.remove(job.init_J()));
      return job;
   }

   public static k.Inner_StreamUtil load(long owner, String scope, String filter) {
      k.Inner_DataUtil b = init_k_DataUtil;
      if (b == null) {
         return null;
      }

      k.Inner_BufferHelper session = init_Map.get(owner);
      k.Inner_StreamUtil job = "ANY_TOOL".equals(scope) ? b.init_k_StreamUtil() : (session == null ? null : session.init_k_StreamUtil);
      return job != null && (filter == null || filter.isBlank() || filter.equals(job.init_Str())) ? job : null;
   }

   public static CompletableFuture<k.Inner_CachePool> init_CompletableFuture(long owner, String scope, String operation) {
      return init(load(owner, scope, ""), operation);
   }

   public static CompletableFuture<k.Inner_CachePool> init(k.Inner_StreamUtil job, String operation) {
      k.Inner_DataUtil b = init_k_DataUtil;
      if (b == null) {
         return CompletableFuture.failedFuture(new IllegalStateException("Tools requires the full client"));
      } else if (job == null) {
         return CompletableFuture.failedFuture(new IllegalStateException("No matching tool has started"));
      } else {
         return "WAIT".equals(operation) ? job.load() : b.init(job, operation);
      }
   }

   public static boolean init(long owner, String scope, String filter, String state) {
      if (!init_Z()) {
         return false;
      }

      k.Inner_StreamUtil job = load(owner, scope, filter);
      if (job == null) {
         return "IDLE".equals(state);
      }

      k.Inner_ConfigStore actual = job.init_k_CachePool().init_k_ConfigStore();
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
            var10000 = actual == k.Inner_ConfigStore.sync || actual == k.Inner_ConfigStore.close || actual == k.Inner_ConfigStore.open;
            break;
         default:
            var10000 = actual.name().equals(state);
      }

      return var10000;
   }

   public static void init(long owner, boolean cancelled) {
      k.Inner_BufferHelper session = init_Map.remove(owner);
      if (session != null && cancelled) {
         for (k.Inner_StreamUtil job : session.init_Map.values()) {
            job.init_V();
         }
      }
   }

   private static final class Inner_BufferHelper {
      volatile k.Inner_StreamUtil init_k_StreamUtil;
      final Map<Long, k.Inner_StreamUtil> init_Map = new ConcurrentHashMap<>();
   }

   public record Inner_CachePool(k.Inner_ConfigStore init_k_ConfigStore, String init_Str, String load) {
      public boolean init_Z() {
         return this.init_k_ConfigStore == k.Inner_ConfigStore.update;
      }
   }

   public enum Inner_ConfigStore {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open;

      public boolean init() {
         return this.ordinal() >= update.ordinal();
      }
   }

   public interface Inner_DataUtil {
      List<k.Inner_TaskQueue> init_List();

      List<String> load();

      k.Inner_StreamUtil init(String var1, String var2);

      k.Inner_StreamUtil init_k_StreamUtil();

      CompletableFuture<k.Inner_CachePool> init(k.Inner_StreamUtil var1, String var2);
   }

   public static final class Inner_StreamUtil {
      private static final AtomicLong init_AtomicLong = new AtomicLong();
      private final long init_J = init_AtomicLong.incrementAndGet();
      private final String init_Str;
      private final AtomicReference<k.Inner_CachePool> init_AtomicReference = new AtomicReference<>(
         new k.Inner_CachePool(k.Inner_ConfigStore.load, "Waiting", "{}")
      );
      private final CompletableFuture<Void> init_CompletableFuture = new CompletableFuture<>();
      private final CompletableFuture<k.Inner_CachePool> load = new CompletableFuture<>();
      private Runnable init_Runnable;
      private boolean init_Z;

      public Inner_StreamUtil(String tool) {
         this.init_Str = tool == null ? "" : tool;
      }

      public long init_J() {
         return this.init_J;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public k.Inner_CachePool init_k_CachePool() {
         return this.init_AtomicReference.get();
      }

      public CompletableFuture<Void> init_CompletableFuture() {
         return this.init_CompletableFuture;
      }

      public CompletableFuture<k.Inner_CachePool> load() {
         return this.load;
      }

      public synchronized boolean init_Z() {
         return this.init_Z;
      }

      public void init(Runnable action) {
         boolean invoke;
         synchronized (this) {
            this.init_Runnable = action;
            invoke = this.init_Z && !this.load.isDone();
         }

         if (invoke && action != null) {
            action.run();
         }
      }

      public void init_V() {
         Runnable action;
         synchronized (this) {
            if (this.load.isDone() || this.init_Z) {
               return;
            }

            this.init_Z = true;
            action = this.init_Runnable;
         }

         if (action != null) {
            action.run();
         }
      }

      public synchronized void init(k.Inner_ConfigStore state, String message, String data) {
         if (state != null && !this.load.isDone()) {
            k.Inner_CachePool next = new k.Inner_CachePool(state, message == null ? "" : message, data == null ? "{}" : data);
            this.init_AtomicReference.set(next);
            if (state == k.Inner_ConfigStore.read || state.init()) {
               this.init_CompletableFuture.complete(null);
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
