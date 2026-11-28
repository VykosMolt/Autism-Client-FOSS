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
   private volatile AiChatPolicy.Inner_DataUtil config;
   private final AiChatMode.Inner_BufferHelper host;
   private final AiChatMode.Inner_CachePool brains;
   private final Executor executor;
   private final Deque<AiChatPolicy.Inner_StreamUtil> history = new ArrayDeque<>();
   private final Deque<AiChatPolicy.Inner_StreamUtil> pendingLines = new ArrayDeque<>();
   private final Deque<AiChatPolicy.Inner_StreamUtil> read_Deque = new ArrayDeque<>();
   private final Deque<Long> replyTimes = new ArrayDeque<>();
   private final Map<String, Long> lastReplyBySender = new HashMap<>();
   private final Deque<String> recentSent = new ArrayDeque<>();
   private volatile boolean active;
   private boolean replyInFlight;
   private long ledgerRevision;
   private long lastLineAt;
   private long firstPendingAt;
   private long lastReplyAt;
   private long nextReplyAt;
   private int replyCount;
   private int load_I;
   private int read_I;
   private int write_I;
   private long inputTokens;
   private long outputTokens;
   private String init_Str = "";
   private String lastSender = "";
   private AiChatMode.Inner_StreamUtil brain;
   private volatile long version;

   AiChatMode(AiChatPolicy.Inner_DataUtil config, AiChatMode.Inner_BufferHelper host, AiChatMode.Inner_CachePool brains, Executor executor) {
      this.config = Objects.requireNonNull(config, "config");
      this.host = Objects.requireNonNull(host, "host");
      this.brains = Objects.requireNonNull(brains, "brains");
      this.executor = Objects.requireNonNull(executor, "executor");
   }

   void start() {
      synchronized (this) {
         if (this.active) {
            return;
         }

         this.active = true;
         this.ledgerRevision = AiChatPolicy.clampRevision(this.host.ledgerRevision());
         this.pendingLines.clear();
         this.read_Deque.clear();
         this.replyTimes.clear();
         this.lastReplyBySender.clear();
         this.recentSent.clear();
         this.read_I = this.load_I = this.replyCount = 0;
         this.init_Str = "";
         this.lastLineAt = this.firstPendingAt = 0L;
         this.lastReplyAt = 0L;
         this.nextReplyAt = 0L;
         List<AiChatLedger.ChatEntry> recent = this.host.recentChat(12);
         List<AiChatLedger.ChatEntry> oldestFirst = new ArrayList<>(recent);
         Collections.reverse(oldestFirst);

         for (AiChatLedger.ChatEntry entry : oldestFirst) {
            AiChatPolicy.Inner_StreamUtil line = AiChatPolicy.init(entry, this.host.playerName());
            if (line != null) {
               this.history.addLast(line);
            }
         }

         this.trimHistory();
      }

      this.host.enterChatting();
      this.host.saveStatus(this.statusJson());
   }

   boolean isActive() {
      return this.active;
   }

   void noteSent(String text) {
      String clean = AiChatPolicy.sanitize(text, 256);
      if (!clean.isEmpty()) {
         synchronized (this) {
            this.recentSent.addLast(clean);

            while (this.recentSent.size() > 8) {
               this.recentSent.removeFirst();
            }

            this.history.addLast(new AiChatPolicy.Inner_StreamUtil(0L, "SELF", this.host.playerName(), clean));
            this.trimHistory();
         }
      }
   }

   void applyPolicy(AiChatPolicy.Inner_DataUtil next) {
      if (next != null) {
         synchronized (this) {
            if (this.config.equals(next)) {
               return;
            }

            this.config = next;
            this.version++;
            this.pendingLines.removeIf(line -> !this.init_Z_PathHelper_StreamU(line));
            this.read_Deque.removeIf(line -> !this.init_Z_PathHelper_StreamU(line));
         }

         this.host.saveStatus(this.statusJson());
      }
   }

   void tick() {
      if (this.active) {
         if (!this.host.canSend()) {
            this.stop("Chat mode stopped: no connection");
         } else {
            long revision = this.host.ledgerRevision();
            List<AiChatLedger.ChatEntry> latest = this.host.recentChat(64);
            synchronized (this) {
               if (!this.active) {
                  return;
               }

               if (revision < this.ledgerRevision) {
                  this.ledgerRevision = revision;
               }

               List<AiChatLedger.ChatEntry> fresh = new ArrayList<>();

               for (AiChatLedger.ChatEntry entry : latest) {
                  if (entry != null && entry.revision() > this.ledgerRevision) {
                     fresh.add(entry);
                  }
               }

               fresh.sort(Comparator.comparingLong(AiChatLedger.ChatEntry::revision));
               String self = this.host.playerName();

               for (AiChatLedger.ChatEntry entry : fresh) {
                  this.ledgerRevision = Math.max(this.ledgerRevision, entry.revision());
                  AiChatPolicy.Inner_StreamUtil line = AiChatPolicy.admit(entry, self, this.config);
                  if (line != null && (!AiChatPolicy.senderOf(entry).isEmpty() || !this.wasRecentlySent(line.text()))) {
                     boolean wasEmpty = this.pendingLines.isEmpty();
                     this.pendingLines.addLast(line);
                     if (this.pendingLines.size() > 16) {
                        this.pendingLines.removeFirst();
                     }

                     if (wasEmpty) {
                        this.firstPendingAt = this.host.now();
                     }

                     this.lastLineAt = this.host.now();
                     this.lastSender = line.sender();
                  }
               }

               if (revision > this.ledgerRevision) {
                  this.ledgerRevision = revision;
               }
            }

            this.maybeStartReply();
         }
      }
   }

   void stop(String reason) {
      AiChatMode.Inner_StreamUtil closing;
      synchronized (this) {
         if (!this.active) {
            return;
         }

         this.active = false;
         this.pendingLines.clear();
         this.read_Deque.clear();
         closing = this.brain;
         this.brain = null;
      }

      if (closing != null) {
         try {
            closing.close();
         } catch (RuntimeException var5) {
         }
      }

      String message = reason != null && !reason.isBlank() ? reason : "Chat mode stopped";
      this.host.saveStatus(this.statusJson());
      this.host.stopChat(message);
   }

   String statusLine() {
      synchronized (this) {
         if (!this.active) {
            return "Chat mode off";
         }

         StringBuilder out = new StringBuilder("Chat mode: ").append(this.replyCount).append(this.replyCount == 1 ? " reply" : " replies");
         if (!this.config.players().isEmpty()) {
            out.append(", ").append(String.join("/", this.config.players()));
         }

         int waiting = this.pendingLines.size() + this.read_Deque.size();
         if (waiting > 0) {
            out.append(", ").append(waiting).append(" waiting");
         }

         if (!this.init_Str.isBlank()) {
            out.append(", retrying");
         }

         return out.toString();
      }
   }

   JsonObject statusJson() {
      synchronized (this) {
         return AiJson.objectOf(
            "active",
            this.active,
            "replies",
            this.replyCount,
            "skips",
            this.load_I,
            "failures",
            this.read_I,
            "requests",
            this.write_I,
            "inputTokens",
            this.inputTokens,
            "outputTokens",
            this.outputTokens,
            "pending",
            this.pendingLines.size() + this.read_Deque.size(),
            "lastSender",
            this.lastSender,
            "lastReplyAt",
            this.lastReplyAt,
            "lastError",
            this.init_Str,
            "summary",
            this.statusSummary(),
            "config",
            this.config.json()
         );
      }
   }

   private String statusSummary() {
      if (!this.active) {
         return "Chat mode off";
      }

      StringBuilder out = new StringBuilder("Chat mode: ").append(this.replyCount).append(this.replyCount == 1 ? " reply" : " replies");
      if (!this.config.players().isEmpty()) {
         out.append(", ").append(String.join("/", this.config.players()));
      }

      int waiting = this.pendingLines.size() + this.read_Deque.size();
      if (waiting > 0) {
         out.append(", ").append(waiting).append(" waiting");
      }

      return out.toString();
   }

   private void maybeStartReply() {
      Runnable task;
      synchronized (this) {
         if (!this.active || this.replyInFlight || this.pendingLines.isEmpty() && this.read_Deque.isEmpty()) {
            return;
         }

         long now = this.host.now();
         long debounce = this.pendingLines.isEmpty()
            ? 0L
            : AiChatPolicy.replyDelay(this.config, new ArrayList<>(this.pendingLines), this.host.playerName());
         boolean settled = this.pendingLines.isEmpty() || now - this.lastLineAt >= debounce;
         boolean heldTooLong = !this.pendingLines.isEmpty() && this.firstPendingAt != 0L && now - this.firstPendingAt >= 5000L;
         if (!settled && !heldTooLong) {
            return;
         }

         if (now < this.nextReplyAt) {
            return;
         }

         String sender = (this.read_Deque.isEmpty() ? this.pendingLines : this.read_Deque).peekFirst().sender();
         if (!AiChatPolicy.withinRateLimit(now, this.replyTimes, this.lastReplyAt, this.lastReplyBySender, sender, this.config)) {
            return;
         }

         this.replyInFlight = true;
         task = this::runReply;
      }

      try {
         this.executor.execute(task);
      } catch (RuntimeException rejected) {
         synchronized (this) {
            this.replyInFlight = false;
         }
      }
   }

   private void runReply() {
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
               if (!this.active || this.pendingLines.isEmpty() && this.read_Deque.isEmpty()) {
                  return;
               }

               List<AiChatPolicy.Inner_StreamUtil> combined = new ArrayList<>(this.read_Deque.size() + this.pendingLines.size());
               combined.addAll(this.read_Deque);
               combined.addAll(this.pendingLines);
               String recipient = init_Str_PathHelper_StreamU(combined.getFirst());
               batch = combined.stream().filter(linex -> init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient)).toList();
               this.read_Deque.removeIf(linex -> init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient));
               this.pendingLines.removeIf(linex -> init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient));
               historySnapshot = this.history
                  .stream()
                  .filter(linex -> init_Str_PathHelper_StreamU(linex).isEmpty() || init_Str_PathHelper_StreamU(linex).equalsIgnoreCase(recipient))
                  .toList();
               requestConfig = this.config;
               requestVersion = this.version;
               self = this.host.playerName();
               if (this.brain == null) {
                  this.brain = this.brains.create();
               }

               model = this.brain;
            }

            String system = AiChatPolicy.systemPrompt(requestConfig, self);
            String user = AiChatPolicy.userPrompt(historySnapshot, batch, self);
            long version = requestVersion;
            BooleanSupplier current = () -> this.active && this.version == version;
            AiChatMode.Inner_DataUtil answer = model.complete(system, user, () -> !current.getAsBoolean() || System.nanoTime() > deadline);
            if (answer != null) {
               runQuietly(
                  () -> this.host
                     .addUsage(Math.max(0L, answer.inputTokens()), Math.max(0L, answer.outputTokens()), Math.max(1L, answer.requests()))
               );
            }

            String clean = AiChatPolicy.sanitize(answer == null ? "" : answer.text(), requestConfig.maxReplyChars());
            boolean skip = AiChatPolicy.isSkipAnswer(answer == null ? null : answer.text()) || clean.isEmpty();
            boolean send = false;
            String privateTarget = null;
            long now = this.host.now();
            synchronized (this) {
               if (!this.active) {
                  return;
               }

               if (requestVersion != this.version) {
                  this.requeueRelevant(batch);
                  return;
               }

               this.write_I++;
               if (answer != null) {
                  this.inputTokens = this.inputTokens + Math.max(0L, answer.inputTokens());
                  this.outputTokens = this.outputTokens + Math.max(0L, answer.outputTokens());
               }

               this.enqueueBatch(batch);
               committed = true;
               this.read_I = 0;
               this.init_Str = "";
               String recipient = init_Str_PathHelper_StreamU(batch.getFirst());
               Iterator<AiChatPolicy.Inner_StreamUtil> previous = this.history.descendingIterator();

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
                     this.nextReplyAt = now + Math.max(500L, this.config.minGapMs() / 2L);
                  } else if (this.host.canSend()) {
                     if (!init_Str_PathHelper_StreamU(batch.getFirst()).isEmpty()) {
                        privateTarget = batch.getFirst().sender();
                     }

                     this.history
                        .addLast(
                           new AiChatPolicy.Inner_StreamUtil(
                              0L, privateTarget == null ? "SELF" : "SELF_PRIVATE", privateTarget == null ? self : privateTarget, clean
                           )
                        );
                     this.trimHistory();
                     this.replyCount++;
                     this.lastReplyAt = now;
                     this.replyTimes.addLast(now);
                     this.lastReplyBySender.put(privateTarget != null ? privateTarget : batch.getLast().sender(), now);
                     this.recentSent.addLast(clean);

                     while (this.recentSent.size() > 8) {
                        this.recentSent.removeFirst();
                     }

                     this.nextReplyAt = now + this.config.minGapMs();
                     send = true;
                  }
                  break;
               }
            }

            if (send && this.active && this.host.canSend()) {
               try {
                  if (privateTarget != null) {
                     this.host.whisper(privateTarget, clean, current);
                  } else {
                     this.host.sendChat(clean, current);
                  }
               } catch (RuntimeException sendError) {
                  runQuietly(() -> this.host.appendTranscript("error", "Chat line could not be sent: " + describeError(sendError)));
               }

               String loggedTarget = privateTarget == null ? batch.getLast().sender() : privateTarget;

               try {
                  this.host.appendTranscript("assistant", "Chat to " + loggedTarget + (privateTarget == null ? "" : " (private)") + ": " + clean);
               } catch (RuntimeException var50) {
               }
            }

            runQuietly(() -> this.host.saveStatus(this.statusJson()));
         } catch (Exception var57) {
            Exception error = var57;
            synchronized (this) {
               if (!this.active) {
                  return;
               }

               if (requestVersion != this.version) {
                  if (!committed) {
                     this.requeueRelevant(batch);
                  }

                  return;
               }

               this.read_I++;
               this.init_Str = describeError(error);
               long backoff = Math.min(60000L, 2000L << Math.min(5, this.read_I - 1));
               this.nextReplyAt = this.host.now() + backoff;
               if (!committed) {
                  this.requeueRelevant(batch);
               }

               if (this.read_I == 1 || this.read_I % 5 == 0) {
                  runQuietly(() -> this.host.appendTranscript("error", "Chat reply failed (" + this.read_I + "): " + this.init_Str));
               }
            }

            runQuietly(() -> this.host.saveStatus(this.statusJson()));
         }
      } finally {
         synchronized (this) {
            this.replyInFlight = false;
         }

         this.maybeStartReply();
      }
   }

   private void enqueueBatch(List<AiChatPolicy.Inner_StreamUtil> batch) {
      for (AiChatPolicy.Inner_StreamUtil line : batch) {
         this.history.addLast(line);
      }

      this.trimHistory();
   }

   private void requeueRelevant(List<AiChatPolicy.Inner_StreamUtil> batch) {
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
      return AiChatPolicy.admit(
            new AiChatLedger.ChatEntry(0L, line.revision(), line.source(), line.sender(), line.text(), List.of(), ""),
            this.host.playerName(),
            this.config
         )
         != null;
   }

   private static String init_Str_PathHelper_StreamU(AiChatPolicy.Inner_StreamUtil line) {
      return !line.source().equalsIgnoreCase("PLAYER_PRIVATE") && !line.source().equals("SELF_PRIVATE") ? "" : line.sender();
   }

   private void trimHistory() {
      while (this.history.size() > 24) {
         this.history.removeFirst();
      }
   }

   private boolean wasRecentlySent(String text) {
      String normalised = AiChatPolicy.normalize(text).toLowerCase(Locale.ROOT);
      if (normalised.isEmpty()) {
         return false;
      }

      for (String sent : this.recentSent) {
         String reply = AiChatPolicy.normalize(sent).toLowerCase(Locale.ROOT);
         if (!reply.isEmpty() && normalised.contains(reply)) {
            return true;
         }
      }

      return false;
   }

   private static void runQuietly(Runnable action) {
      try {
         action.run();
      } catch (RuntimeException var2) {
      }
   }

   private static String describeError(Exception error) {
      String message = error == null ? "" : Objects.toString(error.getMessage(), error.getClass().getSimpleName());
      message = message.replaceAll("(?i)\\bsk-[A-Za-z0-9_-]{8,}\\b", "[redacted]");
      message = message.replaceAll("(?i)\\bAIza[0-9A-Za-z_-]{8,}\\b", "[redacted]");
      message = message.replaceAll("(?i)\\bBearer\\s+[A-Za-z0-9._-]{8,}\\b", "[redacted]");
      return message.length() > 300 ? message.substring(0, 300) : message;
   }

   interface Inner_BufferHelper {
      long now();

      String playerName();

      boolean canSend();

      long ledgerRevision();

      List<AiChatLedger.ChatEntry> recentChat(int var1);

      void sendChat(String var1);

      default void sendChat(String text, BooleanSupplier current) {
         if (current.getAsBoolean()) {
            this.sendChat(text);
         }
      }

      default void whisper(String player, String text) {
      }

      default void whisper(String player, String text, BooleanSupplier current) {
         if (current.getAsBoolean()) {
            this.whisper(player, text);
         }
      }

      void appendTranscript(String var1, String var2);

      void saveStatus(JsonObject var1);

      void enterChatting();

      void stopChat(String var1);

      default void addUsage(long inputTokens, long outputTokens, long requests) {
      }
   }

   interface Inner_CachePool {
      AiChatMode.Inner_StreamUtil create();
   }

   static final class Inner_ConfigStore implements AiChatMode.Inner_StreamUtil {
      private final AiProvider provider;

      Inner_ConfigStore(AiSettings settings) {
         AiSettings chat = settings.copy();
         chat.builtIn = false;
         chat.outputTokens = Math.min(2048, Math.max(1024, chat.outputTokens));
         List<String> levels = AiSettings.effortLevels(chat.provider, chat.model);
         if (!levels.isEmpty()) {
            chat.effort = AiSettings.nearestEffort(levels, "low");
         }

         chat.validate();
         this.provider = new AiProvider(chat);
      }

      @Override
      public AiChatMode.Inner_DataUtil complete(String system, String user, BooleanSupplier cancelled) throws IOException, InterruptedException, ExecutionException, TimeoutException {
         AiProvider.Inner_BufferHelper reply = this.provider
            .completeWithContext(
               AiContext.PromptParts.of(system), List.of(this.provider.userMessage(user)), new JsonArray(), "none", delta -> {}, cancelled, call -> {}
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
         this.provider.close();
      }
   }

   record Inner_DataUtil(String text, long inputTokens, long outputTokens, long requests) {
      static AiChatMode.Inner_DataUtil text(String text) {
         return new AiChatMode.Inner_DataUtil(text, 0L, 0L, 1L);
      }
   }

   interface Inner_StreamUtil extends AutoCloseable {
      AiChatMode.Inner_DataUtil complete(String var1, String var2, BooleanSupplier var3) throws IOException, InterruptedException, ExecutionException, TimeoutException;

      @Override
      default void close() {
      }
   }
}
