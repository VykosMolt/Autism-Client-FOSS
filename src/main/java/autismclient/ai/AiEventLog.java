package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class AiEventLog {
   static final int init_I = 256;
   private static final AiEventLog init_x = new AiEventLog();
   private final ArrayDeque<AiEventLog.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>(256);
   private long init_J;
   private long load;
   private long read;
   private long auditLastEvictedSeq;
   private Object auditConnection;

   static AiEventLog init_x() {
      return init_x;
   }

   private static long load() {
      return autismclient.util.AutismSessionState.getInstance().getClientTick();
   }

   // Clears only on a new server connection, so a dimension change keeps death/item events.
   // Seq stays monotonic so a reader's old "since" cannot hide new events.
   synchronized void auditReset(Object connection) {
      if (connection == this.auditConnection) {
         return;
      }

      this.auditConnection = connection;
      this.init_ArrayDeque.clear();
      this.load = 0L;
      this.auditLastEvictedSeq = 0L;
   }

   synchronized void init(String kind, JsonObject data) {
      if (this.init_ArrayDeque.size() >= 256) {
         this.auditLastEvictedSeq = this.init_ArrayDeque.pollFirst().seq();
         this.load++;
      }

      this.init_ArrayDeque.addLast(new AiEventLog.Inner_DataUtil(++this.init_J, load(), kind, data));
   }

   synchronized List<AiEventLog.Inner_DataUtil> init_List(long since, Set<String> kinds, int limit) {
      List<AiEventLog.Inner_DataUtil> out = new ArrayList<>(Math.min(limit, this.init_ArrayDeque.size()));

      for (AiEventLog.Inner_DataUtil event : this.init_ArrayDeque) {
         if (event.seq() > since && (kinds == null || kinds.isEmpty() || kinds.contains(event.kind()))) {
            if (out.size() >= limit) {
               break;
            }

            out.add(event);
         }
      }

      this.read = Math.max(this.read, out.isEmpty() ? since : out.get(out.size() - 1).seq());
      return out;
   }

   synchronized long init_J_J(long since) {
      return Math.max(0L, Math.min(this.load, this.auditLastEvictedSeq - since));
   }

   synchronized long init_J() {
      return this.init_J;
   }

   synchronized int init_I() {
      int pending = 0;

      for (AiEventLog.Inner_DataUtil event : this.init_ArrayDeque) {
         if (event.seq() > this.read) {
            pending++;
         }
      }

      return pending;
   }

   synchronized JsonObject init_JsonObject(long since, Set<String> kinds, int limit) {
      JsonArray array = new JsonArray();

      for (AiEventLog.Inner_DataUtil event : this.init_List(since, kinds, limit)) {
         array.add(AiJson.objectOf("seq", event.seq(), "tick", event.tick(), "kind", event.kind(), "data", event.data()));
      }

      return AiJson.objectOf("events", array, "latestSeq", this.init_J, "dropped", this.init_J_J(since));
   }

   static JsonObject init_JsonObject_JsonObject(JsonObject args) {
      long since = Math.max(0, AiJson.getInt(args, "since", 0));
      int limit = Math.max(1, Math.min(256, AiJson.getInt(args, "limit", 50)));
      Set<String> kinds = null;
      String raw = AiJson.getString(args, "kinds", "").trim();
      if (!raw.isEmpty()) {
         kinds = new LinkedHashSet<>();

         for (String kind : raw.split("\\s*,\\s*")) {
            if (!kind.isEmpty()) {
               kinds.add(kind);
            }
         }
      }

      return init_x.init_JsonObject(since, kinds, limit);
   }

   record Inner_DataUtil(long seq, long tick, String kind, JsonObject data) {
   }
}
