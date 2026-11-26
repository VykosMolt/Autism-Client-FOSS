package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

final class AiChatMode {
   private volatile AiChatPolicy.Inner_DataUtil init_PathHelper_DataUti;
   private final AiChatMode.Inner_BufferHelper init_FieldCache_BufferH;
   private final AiChatMode.Inner_CachePool init_FieldCache_CachePo;
   private final Executor init_Executor;
   private final Deque<AiChatPolicy.Inner_StreamUtil> init_Deque = new ArrayDeque<>();
   private final Deque<AiChatPolicy.Inner_StreamUtil> load_Deque = new ArrayDeque<>();
   private final Deque<AiChatPolicy.Inner_StreamUtil> read_Deque = new ArrayDeque<>();
   private final Deque<Long> write_Deque = new ArrayDeque<>();
   private final Map<String, Long> init_Map = new HashMap<>();
   private final Deque<String> update_Deque = new ArrayDeque<>();
   private volatile boolean init_Z;
   private boolean load_Z;
   private long init_J;
   private long load_J;
   private long read_J;
   private long write_J;
   private long update_J;
   private int init_I;
   private int load_I;
   private int read_I;
   private int write_I;
   private long sync;
   private long flush;
   private String init_Str = "";
   private String load_Str = "";
   private AiChatMode.Inner_StreamUtil init_FieldCache_StreamU;
   private volatile long close;

   AiChatMode(AiChatPolicy.Inner_DataUtil config, AiChatMode.Inner_BufferHelper host, AiChatMode.Inner_CachePool brains, Executor executor) {
      this.init_PathHelper_DataUti = Objects.requireNonNull(config, "config");
      this.init_FieldCache_BufferH = Objects.requireNonNull(host, "host");
      this.init_FieldCache_CachePo = Objects.requireNonNull(brains, "brains");
      this.init_Executor = Objects.requireNonNull(executor, "executor");
   }

   void init_V() {
      synchronized (this) {
         if (this.init_Z) {
            return;
         }

         this.init_Z = true;
         this.init_J = AiChatPolicy.init(this.init_FieldCache_BufferH.load_J());
         this.load_Deque.clear();
         this.read_Deque.clear();
         this.write_Deque.clear();
         this.init_Map.clear();
         this.update_Deque.clear();
         this.read_I = this.load_I = this.init_I = 0;
         this.init_Str = "";
         this.load_J = this.read_J = 0L;
         this.write_J = 0L;
         this.update_J = 0L;
         List<AiChatLedger.Inner_StreamUtil> recent = this.init_FieldCache_BufferH.init(12);
         List<AiChatLedger.Inner_StreamUtil> oldestFirst = new ArrayList<>(recent);
         Collections.reverse(oldestFirst);

         for (AiChatLedger.Inner_StreamUtil entry : oldestFirst) {
            AiChatPolicy.Inner_StreamUtil line = AiChatPolicy.init(entry, this.init_FieldCache_BufferH.init_Str());
            if (line != null) {
               this.init_Deque.addLast(line);
            }
         }

         this.update();
      }

      this.init_FieldCache_BufferH.init_V();
      this.init_FieldCache_BufferH.init(this.init_JsonObject());
   }

   boolean init_Z() {
      return this.init_Z;
   }

   void init_V_Str(String text) {
      String clean = AiChatPolicy.init(text, 256);
      if (!clean.isEmpty()) {
         synchronized (this) {
            this.update_Deque.addLast(clean);

            while (this.update_Deque.size() > 8) {
               this.update_Deque.removeFirst();
            }

            this.init_Deque.addLast(new AiChatPolicy.Inner_StreamUtil(0L, "SELF", this.init_FieldCache_BufferH.init_Str(), clean));
            this.update();
         }
      }
   }

   void init_V_PathHelper_DataUti(AiChatPolicy.Inner_DataUtil next) {
      if (next != null) {
         synchronized (this) {
            if (this.init_PathHelper_DataUti.equals(next)) {
               return;
            }

            this.init_PathHelper_DataUti = next;
            this.close++;
            this.load_Deque.removeIf(line -> !this.init_Z_PathHelper_StreamU(line));
            this.read_Deque.removeIf(line -> !this.init_Z_PathHelper_StreamU(line));
         }

         this.init_FieldCache_BufferH.init(this.init_JsonObject());
      }
   }

   void load_V() {
      if (this.init_Z) {
         if (!this.init_FieldCache_BufferH.init_Z()) {
            this.load("Chat mode stopped: no connection");
         } else {
            long revision = this.init_FieldCache_BufferH.load_J();
            List<AiChatLedger.Inner_StreamUtil> latest = this.init_FieldCache_BufferH.init(64);
            synchronized (this) {
               if (!this.init_Z) {
                  return;
               }

               if (revision < this.init_J) {
                  this.init_J = revision;
               }

               List<AiChatLedger.Inner_StreamUtil> fresh = new ArrayList<>();

               for (AiChatLedger.Inner_StreamUtil entry : latest) {
                  if (entry != null && entry.revision() > this.init_J) {
                     fresh.add(entry);
                  }
               }

               fresh.sort(Comparator.comparingLong(AiChatLedger.Inner_StreamUtil::revision));
               String self = this.init_FieldCache_BufferH.init_Str();

               for (AiChatLedger.Inner_StreamUtil entry : fresh) {
                  this.init_J = Math.max(this.init_J, entry.revision());
                  AiChatPolicy.Inner_StreamUtil line = AiChatPolicy.init(entry, self, this.init_PathHelper_DataUti);
                  if (line != null && (!AiChatPolicy.init(entry).isEmpty() || !this.init_Z_Str(line.text()))) {
                     boolean wasEmpty = this.load_Deque.isEmpty();
                     this.load_Deque.addLast(line);
                     if (this.load_Deque.size() > 16) {
                        this.load_Deque.removeFirst();
                     }

                     if (wasEmpty) {
                        this.read_J = this.init_FieldCache_BufferH.init_J();
                     }

                     this.load_J = this.init_FieldCache_BufferH.init_J();
                     this.load_Str = line.sender();
                  }
               }

               if (revision > this.init_J) {
                  this.init_J = revision;
               }
            }

            this.read();
         }
      }
   }

   void load(String reason) {
      AiChatMode.Inner_StreamUtil closing;
      synchronized (this) {
         if (!this.init_Z) {
            return;
         }

         this.init_Z = false;
         this.load_Deque.clear();
         this.read_Deque.clear();
         closing = this.init_FieldCache_StreamU;
         this.init_FieldCache_StreamU = null;
      }

      if (closing != null) {
         try {
            closing.close();
         } catch (RuntimeException var5) {
         }
      }

      String message = reason != null && !reason.isBlank() ? reason : "Chat mode stopped";
      this.init_FieldCache_BufferH.init(this.init_JsonObject());
      this.init_FieldCache_BufferH.load(message);
   }

   String init_Str() {
      synchronized (this) {
         if (!this.init_Z) {
            return "Chat mode off";
         }

         StringBuilder out = new StringBuilder("Chat mode: ").append(this.init_I).append(this.init_I == 1 ? " reply" : " replies");
         if (!this.init_PathHelper_DataUti.players().isEmpty()) {
            out.append(", ").append(String.join("/", this.init_PathHelper_DataUti.players()));
         }

         int waiting = this.load_Deque.size() + this.read_Deque.size();
         if (waiting > 0) {
            out.append(", ").append(waiting).append(" waiting");
         }

         if (!this.init_Str.isBlank()) {
            out.append(", retrying");
         }

         return out.toString();
      }
   }

   JsonObject init_JsonObject() {
      synchronized (this) {
         return AiJson.init_JsonObject_ObjArr(
            "active",
            this.init_Z,
            "replies",
            this.init_I,
            "skips",
            this.load_I,
            "failures",
            this.read_I,
            "requests",
            this.write_I,
            "inputTokens",
            this.sync,
            "outputTokens",
            this.flush,
            "pending",
            this.load_Deque.size() + this.read_Deque.size(),
            "lastSender",
            this.load_Str,
            "lastReplyAt",
            this.write_J,
            "lastError",
            this.init_Str,
            "summary",
            this.load_Str(),
            "config",
            this.init_PathHelper_DataUti.json()
         );
      }
   }

   private String load_Str() {
      if (!this.init_Z) {
         return "Chat mode off";
      }

      StringBuilder out = new StringBuilder("Chat mode: ").append(this.init_I).append(this.init_I == 1 ? " reply" : " replies");
      if (!this.init_PathHelper_DataUti.players().isEmpty()) {
         out.append(", ").append(String.join("/", this.init_PathHelper_DataUti.players()));
      }

      int waiting = this.load_Deque.size() + this.read_Deque.size();
      if (waiting > 0) {
         out.append(", ").append(waiting).append(" waiting");
      }

      return out.toString();
   }

   private void read() {
      Runnable task;
      synchronized (this) {
         if (!this.init_Z || this.load_Z || this.load_Deque.isEmpty() && this.read_Deque.isEmpty()) {
            return;
         }

         long now = this.init_FieldCache_BufferH.init_J();
         long debounce = this.load_Deque.isEmpty()
            ? 0L
            : AiChatPolicy.init(this.init_PathHelper_DataUti, new ArrayList<>(this.load_Deque), this.init_FieldCache_BufferH.init_Str());
         boolean settled = this.load_Deque.isEmpty() || now - this.load_J >= debounce;
         boolean heldTooLong = !this.load_Deque.isEmpty() && this.read_J != 0L && now - this.read_J >= 5000L;
         if (!settled && !heldTooLong) {
            return;
         }

         if (now < this.update_J) {
            return;
         }

         String sender = (this.read_Deque.isEmpty() ? this.load_Deque : this.read_Deque).peekFirst().sender();
         if (!AiChatPolicy.init(now, this.write_Deque, this.write_J, this.init_Map, sender, this.init_PathHelper_DataUti)) {
            return;
         }

         this.load_Z = true;
         task = this::write;
      }

      try {
         this.init_Executor.execute(task);
      } catch (RuntimeException rejected) {
         synchronized (this) {
            this.load_Z = false;
         }
      }
   }

   private void write() {
      List<AiChatPolicy.Inner_StreamUtil> batch = List.of();
      long requestVersion = -1L;
      boolean committed = false;
      long deadline = System.nanoTime() + 90000000000L;

      try {
         try {
            List<AiChatPolicy.Inner_StreamUtil> historySnapshot;
            AiChatMode.Inner_StreamUtil model;
            String self;
            AiChatPolicy.Inner_DataUtil requestConfig;
            synchronized (this) {
               if (!this.init_Z || this.load_Deque.isEmpty() && this.read_Deque.isEmpty()) {
                  return;
               }

               List<AiChatPolicy.Inner_StreamUtil> combined = new ArrayList<>(this.read_Deque.size() + this.load_Deque.size());
               combined.addAll(this.read_Deque);
               combined.addAll(this.load_Deque);
               String recipient = init_Str_PathHelper_StreamU(combined.getFirst());
               batch = combined.stream().filter(linex -> init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient)).toList();
               this.read_Deque.removeIf(linex -> init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient));
               this.load_Deque.removeIf(linex -> init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient));
               historySnapshot = this.init_Deque
                  .stream()
                  .filter(linex -> init_Str_PathHelper_StreamU(linex).isEmpty() || init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient))
                  .toList();
               requestConfig = this.init_PathHelper_DataUti;
               requestVersion = this.close;
               self = this.init_FieldCache_BufferH.init_Str();
               if (this.init_FieldCache_StreamU == null) {
                  this.init_FieldCache_StreamU = this.init_FieldCache_CachePo.create();
               }

               model = this.init_FieldCache_StreamU;
            }

            String system = AiChatPolicy.init(requestConfig, self);
            String user = AiChatPolicy.init(historySnapshot, batch, self);
            long version = requestVersion;
            BooleanSupplier current = () -> this.init_Z && this.close == version;
            AiChatMode.Inner_DataUtil answer = model.init(system, user, () -> !current.getAsBoolean() || System.nanoTime() > deadline);
            if (answer != null) {
               init_V_Runnable(
                  () -> this.init_FieldCache_BufferH
                     .init(Math.max(0L, answer.inputTokens()), Math.max(0L, answer.outputTokens()), Math.max(1L, answer.requests()))
               );
            }

            String clean = AiChatPolicy.init(answer == null ? "" : answer.text(), requestConfig.maxReplyChars());
            boolean skip = AiChatPolicy.update(answer == null ? null : answer.text()) || clean.isEmpty();
            boolean send = false;
            String privateTarget = null;
            long now = this.init_FieldCache_BufferH.init_J();
            synchronized (this) {
               if (!this.init_Z) {
                  return;
               }

               if (requestVersion != this.close) {
                  this.load(batch);
                  return;
               }

               this.write_I++;
               if (answer != null) {
                  this.sync = this.sync + Math.max(0L, answer.inputTokens());
                  this.flush = this.flush + Math.max(0L, answer.outputTokens());
               }

               this.init_V_List(batch);
               committed = true;
               this.read_I = 0;
               this.init_Str = "";
               String recipient = init_Str_PathHelper_StreamU(batch.getFirst());
               Iterator<AiChatPolicy.Inner_StreamUtil> previous = this.init_Deque.descendingIterator();

               while (true) {
                  if (!skip && previous.hasNext()) {
                     AiChatPolicy.Inner_StreamUtil line = previous.next();
                     if (!line.source().startsWith("SELF") || !init_Str_PathHelper_StreamU(line).equalsIgnoreCase(recipient)) {
                        continue;
                     }

                     skip = line.text().equalsIgnoreCase(clean);
                  }

                  if (skip) {
                     this.load_I++;
                     this.update_J = now + Math.max(500L, this.init_PathHelper_DataUti.minGapMs() / 2L);
                  } else if (this.init_FieldCache_BufferH.init_Z()) {
                     if (!init_Str_PathHelper_StreamU(batch.getFirst()).isEmpty()) {
                        privateTarget = batch.getFirst().sender();
                     }

                     this.init_Deque
                        .addLast(
                           new AiChatPolicy.Inner_StreamUtil(
                              0L, privateTarget == null ? "SELF" : "SELF_PRIVATE", privateTarget == null ? self : privateTarget, clean
                           )
                        );
                     this.update();
                     this.init_I++;
                     this.write_J = now;
                     this.write_Deque.addLast(now);
                     this.init_Map.put(privateTarget != null ? privateTarget : batch.getLast().sender(), now);
                     this.update_Deque.addLast(clean);

                     while (this.update_Deque.size() > 8) {
                        this.update_Deque.removeFirst();
                     }

                     this.update_J = now + this.init_PathHelper_DataUti.minGapMs();
                     send = true;
                  }
                  break;
               }
            }

            if (send && this.init_Z && this.init_FieldCache_BufferH.init_Z()) {
               try {
                  if (privateTarget != null) {
                     this.init_FieldCache_BufferH.init(privateTarget, clean, current);
                  } else {
                     this.init_FieldCache_BufferH.init(clean, current);
                  }
               } catch (RuntimeException sendError) {
                  init_V_Runnable(() -> this.init_FieldCache_BufferH.load("error", "Chat line could not be sent: " + init_Str_Exception(sendError)));
               }

               String loggedTarget = privateTarget == null ? batch.getLast().sender() : privateTarget;

               try {
                  this.init_FieldCache_BufferH.load("assistant", "Chat to " + loggedTarget + (privateTarget == null ? "" : " (private)") + ": " + clean);
               } catch (RuntimeException var50) {
               }
            }

            init_V_Runnable(() -> this.init_FieldCache_BufferH.init(this.init_JsonObject()));
         } catch (Exception var57) {
            Exception error = var57;
            synchronized (this) {
               if (!this.init_Z) {
                  return;
               }

               if (requestVersion != this.close) {
                  if (!committed) {
                     this.load(batch);
                  }

                  return;
               }

               this.read_I++;
               this.init_Str = init_Str_Exception(error);
               long backoff = Math.min(60000L, 2000L << Math.min(5, this.read_I - 1));
               this.update_J = this.init_FieldCache_BufferH.init_J() + backoff;
               if (!committed) {
                  this.load(batch);
               }

               if (this.read_I == 1 || this.read_I % 5 == 0) {
                  init_V_Runnable(() -> this.init_FieldCache_BufferH.load("error", "Chat reply failed (" + this.read_I + "): " + this.init_Str));
               }
            }

            init_V_Runnable(() -> this.init_FieldCache_BufferH.init(this.init_JsonObject()));
         }
      } finally {
         synchronized (this) {
            this.load_Z = false;
         }

         this.read();
      }
   }

   private void init_V_List(List<AiChatPolicy.Inner_StreamUtil> batch) {
      for (AiChatPolicy.Inner_StreamUtil line : batch) {
         this.init_Deque.addLast(line);
      }

      this.update();
   }

   private void load(List<AiChatPolicy.Inner_StreamUtil> batch) {
      for (int i = batch.size() - 1; i >= 0; i--) {
         if (this.init_Z_PathHelper_StreamU(batch.get(i))) {
            this.read_Deque.addFirst(batch.get(i));
         }
      }

      while (this.read_Deque.size() > 16) {
         this.read_Deque.removeLast();
      }
   }

   private boolean init_Z_PathHelper_StreamU(AiChatPolicy.Inner_StreamUtil line) {
      return AiChatPolicy.init(
            new AiChatLedger.Inner_StreamUtil(0L, line.revision(), line.source(), line.sender(), line.text(), List.of(), ""),
            this.init_FieldCache_BufferH.init_Str(),
            this.init_PathHelper_DataUti
         )
         != null;
   }

   private static String init_Str_PathHelper_StreamU(AiChatPolicy.Inner_StreamUtil line) {
      return !line.source().equalsIgnoreCase("PLAYER_PRIVATE") && !line.source().equals("SELF_PRIVATE") ? "" : line.sender();
   }

   private void update() {
      while (this.init_Deque.size() > 24) {
         this.init_Deque.removeFirst();
      }
   }

   private boolean init_Z_Str(String text) {
      String normalised = AiChatPolicy.load_Str(text).toLowerCase(Locale.ROOT);
      if (normalised.isEmpty()) {
         return false;
      }

      for (String sent : this.update_Deque) {
         String reply = AiChatPolicy.load_Str(sent).toLowerCase(Locale.ROOT);
         if (!reply.isEmpty() && normalised.contains(reply)) {
            return true;
         }
      }

      return false;
   }

   private static void init_V_Runnable(Runnable action) {
      try {
         action.run();
      } catch (RuntimeException var2) {
      }
   }

   private static String init_Str_Exception(Exception error) {
      String message = error == null ? "" : Objects.toString(error.getMessage(), error.getClass().getSimpleName());
      message = message.replaceAll("(?i)\\bsk-[A-Za-z0-9_-]{8,}\\b", "[redacted]");
      message = message.replaceAll("(?i)\\bAIza[0-9A-Za-z_-]{8,}\\b", "[redacted]");
      message = message.replaceAll("(?i)\\bBearer\\s+[A-Za-z0-9._-]{8,}\\b", "[redacted]");
      return message.length() > 300 ? message.substring(0, 300) : message;
   }

   interface Inner_BufferHelper {
      long init_J();

      String init_Str();

      boolean init_Z();

      long load_J();

      List<AiChatLedger.Inner_StreamUtil> init(int var1);

      void init(String var1);

      default void init(String text, BooleanSupplier current) {
         if (current.getAsBoolean()) {
            this.init(text);
         }
      }

      default void init(String player, String text) {
      }

      default void init(String player, String text, BooleanSupplier current) {
         if (current.getAsBoolean()) {
            this.init(player, text);
         }
      }

      void load(String var1, String var2);

      void init(JsonObject var1);

      void init_V();

      void load(String var1);

      default void init(long inputTokens, long outputTokens, long requests) {
      }
   }

   interface Inner_CachePool {
      AiChatMode.Inner_StreamUtil create();
   }

   static final class Inner_ConfigStore implements AiChatMode.Inner_StreamUtil {
      private final AiProvider init;

      Inner_ConfigStore(AiSettings settings) {
         AiSettings chat = settings.copy();
         chat.builtIn = false;
         chat.outputTokens = Math.min(2048, Math.max(1024, chat.outputTokens));
         List<String> levels = AiSettings.effortLevels(chat.provider, chat.model);
         if (!levels.isEmpty()) {
            chat.effort = AiSettings.nearestEffort(levels, "low");
         }

         chat.validate();
         this.init = new AiProvider(chat);
      }

      @Override
      public AiChatMode.Inner_DataUtil init(String system, String user, BooleanSupplier cancelled) throws IOException, InterruptedException, ExecutionException, TimeoutException {
         AiProvider.Inner_BufferHelper reply = this.init
            .init_bA_BufferHelper_c_StreamUtil(
               AiContext.Inner_StreamUtil.of(system), List.of(this.init.init_JsonObject_Str(user)), new JsonArray(), "none", delta -> {}, cancelled, call -> {}
            );
         AiProvider.Inner_TaskQueue usage = reply.usage();
         String text = reply.text();
         String finish = reply.finish();
         if (finish != null && !finish.isBlank() && !Set.of("stop", "STOP", "end_turn", "tool_calls", "tool_use").contains(finish)) {
            text = "";
         }

         return new AiChatMode.Inner_DataUtil(text, usage.promptTokens(), usage.outputTokens(), 1L);
      }

      @Override
      public void close() {
         this.init.close();
      }
   }

   record Inner_DataUtil(String text, long inputTokens, long outputTokens, long requests) {
      static AiChatMode.Inner_DataUtil text(String text) {
         return new AiChatMode.Inner_DataUtil(text, 0L, 0L, 1L);
      }
   }

   interface Inner_StreamUtil extends AutoCloseable {
      AiChatMode.Inner_DataUtil init(String var1, String var2, BooleanSupplier var3) throws IOException, InterruptedException, ExecutionException, TimeoutException;

      @Override
      default void close() {
      }
   }
}
