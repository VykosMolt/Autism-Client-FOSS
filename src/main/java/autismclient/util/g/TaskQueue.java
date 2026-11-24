package autismclient.util.g;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class TaskQueue {
   static final int init_I = 32;
   static final int load_I = 32;
   static final int read_I = 64;
   private static final int write_I = 4096;
   private static final int update_I = 24;
   private static final int sync_I = 6;
   private static final String init_Str = "multi-connection-";
   private static final Pattern init_Pattern = Pattern.compile("multi-connection-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.txt");
   private static final Pattern load_Pattern = Pattern.compile(
      "(?i)(\\b(?:access[_-]?token|refresh[_-]?token|client[_-]?token|session[_-]?token|authorization|password|passwd|secret|api[_-]?key|token)\\b[\\\"']?\\s*[:=]\\s*)(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s,;&]+)"
   );
   private static final Pattern read_Pattern = Pattern.compile("(?i)\\bBearer\\s+[^\\s,;]+");
   private static final Pattern write_Pattern = Pattern.compile("(?i)\\b(?:https?|socks[45]?)://[^\\s<>]+");
   private static final Pattern update_Pattern = Pattern.compile("(?i)/(?:login|register|l|reg)\\s+[^\\r\\n]*");
   private static final Pattern sync_Pattern = Pattern.compile("\\beyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b");
   private final LongSupplier init_LongSupplier;
   private final ArrayDeque<String> init_ArrayDeque = new ArrayDeque<>();
   private final TaskQueue.Inner_StreamUtil init_TaskQueue_StreamUt = new TaskQueue.Inner_StreamUtil();
   private final TaskQueue.Inner_StreamUtil load_TaskQueue_StreamUt = new TaskQueue.Inner_StreamUtil();
   private long init_J;
   private long load_J;
   private long read_J;
   private long write_J;
   private int flush;
   private String load_Str = "QUEUED";
   private String read_Str = "";

   public TaskQueue() {
      this(System::nanoTime);
   }

   TaskQueue(LongSupplier clock) {
      this.init_LongSupplier = Objects.requireNonNull(clock);
      this.init_J = this.load_J = clock.getAsLong();
      this.init_ArrayDeque.add("0 ms: QUEUED");
   }

   public synchronized void init_V_I(int epoch) {
      this.flush = epoch;
      this.init_TaskQueue_StreamUt.init();
      this.load_TaskQueue_StreamUt.init();
      this.read_Str = "";
      this.init_V_Str_J("attempt " + epoch, this.init_LongSupplier.getAsLong());
   }

   public synchronized void init_V_Str(String value) {
      String next = load(value, 96);
      if (!next.equals(this.load_Str)) {
         long now = this.init_LongSupplier.getAsLong();
         this.load_Str = next;
         this.load_J = now;
         this.init_V_Str_J(next, now);
      }
   }

   public synchronized void init_V_Class(Class<?> packetType) {
      this.init_TaskQueue_StreamUt.init(packetType, this.init_LongSupplier.getAsLong());
   }

   public synchronized void load(Class<?> packetType) {
      this.load_TaskQueue_StreamUt.init(packetType, this.init_LongSupplier.getAsLong());
   }

   public synchronized void init() {
      this.write_J++;
      this.read_J = this.init_LongSupplier.getAsLong();
   }

   public synchronized void init_V_Throwable(Throwable cause) {
      if (cause != null && this.read_Str.isEmpty()) {
         StringBuilder trace = new StringBuilder();
         Map<Throwable, Boolean> seen = new IdentityHashMap<>();

         for (int causes = 0; cause != null && causes < 6 && seen.put(cause, Boolean.TRUE) == null; cause = cause.getCause()) {
            if (causes++ > 0) {
               trace.append("Caused by: ");
            }

            trace.append(cause.getClass().getName()).append('\n');
            StackTraceElement[] frames = cause.getStackTrace();
            int count = Math.min(24, frames.length);

            for (int i = 0; i < count; i++) {
               trace.append("  at ").append(load(frames[i].toString(), 256)).append('\n');
            }

            if (frames.length > count) {
               trace.append("  [additional frames omitted]\n");
            }
         }

         if (cause != null) {
            trace.append("[additional or cyclic causes omitted]\n");
         }

         this.read_Str = trace.toString();
      }
   }

   public synchronized TaskQueue.Inner_DataUtil init(String reason, String routeMode, String protocol, int epoch) {
      return this.init(reason, routeMode, protocol, epoch, "unknown", "unknown");
   }

   public synchronized TaskQueue.Inner_DataUtil init(String reason, String routeMode, String protocol, int epoch, String accountLabel, String server) {
      long now = this.init_LongSupplier.getAsLong();
      String id = UUID.randomUUID().toString();
      String safeReason = init_Str_Str(reason);
      String summary = "During " + this.load_Str + " (" + init_J_J_J(this.load_J, now) + " ms): " + read(safeReason, 240) + "; report " + id;
      StringBuilder text = new StringBuilder(4096);
      text.append("Multi connection report\n")
         .append("Report: ")
         .append(id)
         .append('\n')
         .append("Created (UTC): ")
         .append(Instant.now())
         .append('\n')
         .append("Account: ")
         .append(init_Str_Str_I(accountLabel, 96))
         .append('\n')
         .append("Server: ")
         .append(init_Str_Str_I(server, 256))
         .append('\n')
         .append("Reason: ")
         .append(safeReason)
         .append('\n')
         .append("Phase: ")
         .append(this.load_Str)
         .append('\n')
         .append("Phase elapsed: ")
         .append(init_J_J_J(this.load_J, now))
         .append(" ms\n")
         .append("Session elapsed: ")
         .append(init_J_J_J(this.init_J, now))
         .append(" ms\n")
         .append("Connection epoch: ")
         .append(epoch)
         .append(" (recorded attempt ")
         .append(this.flush)
         .append(")\n")
         .append("Route: ")
         .append(init_Str_Str_I(routeMode, 96))
         .append('\n')
         .append("Protocol: ")
         .append(init_Str_Str_I(protocol, 96))
         .append('\n')
         .append("Mode: headless session; no rendering required\n")
         .append("Scheduler ticks: ")
         .append(this.write_J)
         .append("; last tick: ")
         .append(this.write_J == 0L ? "none" : init_J_J_J(this.read_J, now) + " ms ago")
         .append("\n\n");
      this.init_TaskQueue_StreamUt.init(text, "Inbound", now);
      this.load_TaskQueue_StreamUt.init(text, "Outbound submitted", now);
      text.append("Outbound submission is local evidence only; it does not prove server receipt.\n")
         .append("Packet counts cover this connection attempt; packets are not captured.\n\n")
         .append("Recent phases (elapsed from session start):\n");

      for (String entry : this.init_ArrayDeque) {
         text.append("  ").append(entry).append('\n');
      }

      text.append("\nException types and stack frames (messages omitted):\n")
         .append(this.read_Str.isEmpty() ? "No local exception captured.\n" : this.read_Str);
      return new TaskQueue.Inner_DataUtil(id, summary, text.toString());
   }

   public static synchronized Path init_Path_Path_TaskQueue_DataUtil(Path directory, TaskQueue.Inner_DataUtil report) throws IOException {
      Objects.requireNonNull(report);
      String name = "multi-connection-" + report.init() + ".txt";
      if (!init_Pattern.matcher(name).matches()) {
         throw new IOException("Invalid report identifier");
      }

      Path root = Objects.requireNonNull(directory).toAbsolutePath().normalize();
      init_V_Path(root);
      Files.createDirectories(root);
      init_V_Path(root);
      Path destination = root.resolve(name);
      Files.writeString(destination, report.read(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
      List<Path> files = new ArrayList<>();

      try (Stream<Path> children = Files.list(root)) {
         children.filter(filex -> init_Pattern.matcher(filex.getFileName().toString()).matches())
            .filter(filex -> Files.isRegularFile(filex, LinkOption.NOFOLLOW_LINKS))
            .forEach(files::add);
      }

      files.sort(Comparator.comparingLong(TaskQueue::init_J_Path).thenComparing(path -> path.getFileName().toString()));
      int excess = files.size() - 64;

      for (Path file : files) {
         if (excess <= 0) {
            break;
         }

         if (!file.equals(destination)) {
            Files.deleteIfExists(file);
            excess--;
         }
      }

      return destination;
   }

   private static void init_V_Path(Path directory) throws IOException {
      for (Path entry = directory; entry != null; entry = entry.getParent()) {
         if (Files.isSymbolicLink(entry)) {
            throw new IOException("Report directory must not traverse symbolic links");
         }
      }
   }

   private static long init_J_Path(Path path) {
      try {
         return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
      } catch (IOException ignored) {
         return 9223372036854775807L;
      }
   }

   public static String init_Str_Str(String input) {
      String value = input != null && !input.isBlank() ? input : "Disconnected (no reason supplied)";
      return init_Str_Str_I(value, 4096);
   }

   public static String init_Str_Str_I(String input, int limit) {
      if (input != null && limit > 0) {
         String value = input;
         value = read(value, 8192);
         value = update_Pattern.matcher(value).replaceAll("[login command redacted]");
         value = write_Pattern.matcher(value).replaceAll("[URL redacted]");
         value = read_Pattern.matcher(value).replaceAll("Bearer [redacted]");
         value = load_Pattern.matcher(value).replaceAll("$1[redacted]");
         value = sync_Pattern.matcher(value).replaceAll("[token redacted]");
         return load(value, Math.min(4096, limit));
      } else {
         return "";
      }
   }

   private static String load(String value, int limit) {
      if (value == null) {
         return "unknown";
      }

      StringBuilder clean = new StringBuilder(Math.min(value.length(), limit));

      for (int i = 0; i < value.length() && clean.length() < limit; i++) {
         char c = value.charAt(i);
         if (c == 167 && i + 1 < value.length()) {
            i++;
         } else if (!Character.isISOControl(c) && Character.getType(c) != 16) {
            clean.append(c);
         } else {
            clean.append(' ');
         }
      }

      return read(clean.toString().trim(), limit);
   }

   private static String read(String value, int max) {
      return value.length() <= max ? value : (max < 4 ? value.substring(0, max) : value.substring(0, max - 3) + "...");
   }

   private void init_V_Str_J(String value, long now) {
      if (this.init_ArrayDeque.size() == 32) {
         this.init_ArrayDeque.removeFirst();
      }

      this.init_ArrayDeque.addLast(init_J_J_J(this.init_J, now) + " ms: " + value);
   }

   private static long init_J_J_J(long then, long now) {
      return Math.max(0L, (now - then) / 1000000L);
   }

   public record Inner_DataUtil(String init, String load, String read) {
      public Inner_DataUtil {
         Objects.requireNonNull(init);
         Objects.requireNonNull(load);
         Objects.requireNonNull(read);
      }
   }

   private static final class Inner_StreamUtil {
      private final Map<String, Long> init_Map = new LinkedHashMap<>();
      private long init_J;
      private long load;
      private String init_Str = "none";
      private long read;
      private long write;
      private long update;

      void init(Class<?> type, long now) {
         if (type != null) {
            String name = type.getSimpleName();
            this.init_J++;
            this.load = now;
            this.init_Str = name;
            if (name.equals("ClientboundKeepAlivePacket") || name.equals("ServerboundKeepAlivePacket")) {
               this.read++;
               this.write = now;
            }

            if (!this.init_Map.containsKey(name) && this.init_Map.size() >= 32) {
               this.update++;
            } else {
               this.init_Map.merge(name, 1L, Long::sum);
            }
         }
      }

      void init() {
         this.init_Map.clear();
         this.init_J = this.load = this.read = this.write = this.update = 0L;
         this.init_Str = "none";
      }

      void init(StringBuilder text, String label, long now) {
         text.append(label).append(": ").append(this.init_J).append(" packets\n").append("  Last packet: ").append(this.init_Str);
         if (this.init_J > 0L) {
            text.append(" (").append(TaskQueue.init_J_J_J(this.load, now)).append(" ms ago)");
         }

         text.append("\n  Keep-alives: ").append(this.read);
         if (this.read > 0L) {
            text.append(" (last ").append(TaskQueue.init_J_J_J(this.write, now)).append(" ms ago)");
         }

         text.append('\n');
         this.init_Map.forEach((name, total) -> text.append("  ").append(name).append(": ").append(total).append('\n'));
         if (this.update > 0L) {
            text.append("  Other packet types: ").append(this.update).append('\n');
         }
      }
   }
}
