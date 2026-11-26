package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class x {
   static final int init_I = 256;
   private static final x init_x = new x();
   private final ArrayDeque<x.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>(256);
   private long init_J;
   private long load;
   private long read;
   private long auditLastEvictedSeq;
   private Object auditConnection;

   static x init_x() {
      return init_x;
   }

   private static long load() {
      return autismclient.util.AutismSessionState.init_ck().state_I();
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

      this.init_ArrayDeque.addLast(new x.Inner_DataUtil(++this.init_J, load(), kind, data));
   }

   synchronized List<x.Inner_DataUtil> init_List(long since, Set<String> kinds, int limit) {
      List<x.Inner_DataUtil> out = new ArrayList<>(Math.min(limit, this.init_ArrayDeque.size()));

      for (x.Inner_DataUtil event : this.init_ArrayDeque) {
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

      for (x.Inner_DataUtil event : this.init_ArrayDeque) {
         if (event.seq() > this.read) {
            pending++;
         }
      }

      return pending;
   }

   synchronized JsonObject init_JsonObject(long since, Set<String> kinds, int limit) {
      JsonArray array = new JsonArray();

      for (x.Inner_DataUtil event : this.init_List(since, kinds, limit)) {
         array.add(ag.init_JsonObject_ObjArr("seq", event.seq(), "tick", event.tick(), "kind", event.kind(), "data", event.data()));
      }

      return ag.init_JsonObject_ObjArr("events", array, "latestSeq", this.init_J, "dropped", this.init_J_J(since));
   }

   static JsonObject init_JsonObject_JsonObject(JsonObject args) {
      long since = Math.max(0, ag.init_I_JsonObject(args, "since", 0));
      int limit = Math.max(1, Math.min(256, ag.init_I_JsonObject(args, "limit", 50)));
      Set<String> kinds = null;
      String raw = ag.init_Str_JsonObject(args, "kinds", "").trim();
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
