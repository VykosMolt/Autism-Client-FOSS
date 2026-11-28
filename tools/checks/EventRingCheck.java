package autismclient.ai;

import com.google.gson.JsonObject;

public class EventRingCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL EventRingCheck: " + what);
         System.exit(1);
      }
   }

   public static void main(String[] args) {
      AiEventLog ring = AiEventLog.getInstance();
      for (int i = 0; i < 300; i++) {
         ring.log("e", new JsonObject());
      }

      // Seqs 1..44 were evicted, 45..300 are kept.
      check(ring.droppedSince(0) == 44, "since 0 counts every eviction, got " + ring.droppedSince(0));
      check(ring.droppedSince(10) == 34, "since 10 counts seq 11..44, got " + ring.droppedSince(10));
      check(ring.droppedSince(44) == 0 && ring.droppedSince(200) == 0, "nothing dropped after the last evicted seq");
      check(ring.read(0, null, 256).size() == 256, "ring keeps 256");

      Object connection = new Object();
      ring.auditReset(connection);
      check(ring.read(0, null, 256).isEmpty() && ring.droppedSince(0) == 0, "reset clears the ring and the drop count");
      ring.log("after", new JsonObject());
      check(ring.read(300, null, 10).size() == 1 && ring.nextSeq() == 301, "seq stays monotonic after reset");
      ring.auditReset(connection);
      check(ring.read(300, null, 10).size() == 1, "same connection (dimension change) keeps events");
      for (int i = 0; i < 260; i++) {
         ring.log("e", new JsonObject());
      }

      // Seqs 301..305 were evicted after the reset.
      check(ring.droppedSince(0) == 5 && ring.droppedSince(303) == 2 && ring.droppedSince(100) == 5, "drops after reset, got " + ring.droppedSince(0) + "/" + ring.droppedSince(303) + "/" + ring.droppedSince(100));
      System.out.println("PASS EventRingCheck");
   }
}
