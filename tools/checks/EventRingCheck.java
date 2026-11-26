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
      AiEventLog ring = AiEventLog.init_x();
      for (int i = 0; i < 300; i++) {
         ring.init("e", new JsonObject());
      }

      // Seqs 1..44 were evicted, 45..300 are kept.
      check(ring.init_J_J(0) == 44, "since 0 counts every eviction, got " + ring.init_J_J(0));
      check(ring.init_J_J(10) == 34, "since 10 counts seq 11..44, got " + ring.init_J_J(10));
      check(ring.init_J_J(44) == 0 && ring.init_J_J(200) == 0, "nothing dropped after the last evicted seq");
      check(ring.init_List(0, null, 256).size() == 256, "ring keeps 256");

      Object connection = new Object();
      ring.auditReset(connection);
      check(ring.init_List(0, null, 256).isEmpty() && ring.init_J_J(0) == 0, "reset clears the ring and the drop count");
      ring.init("after", new JsonObject());
      check(ring.init_List(300, null, 10).size() == 1 && ring.init_J() == 301, "seq stays monotonic after reset");
      ring.auditReset(connection);
      check(ring.init_List(300, null, 10).size() == 1, "same connection (dimension change) keeps events");
      for (int i = 0; i < 260; i++) {
         ring.init("e", new JsonObject());
      }

      // Seqs 301..305 were evicted after the reset.
      check(ring.init_J_J(0) == 5 && ring.init_J_J(303) == 2 && ring.init_J_J(100) == 5, "drops after reset, got " + ring.init_J_J(0) + "/" + ring.init_J_J(303) + "/" + ring.init_J_J(100));
      System.out.println("PASS EventRingCheck");
   }
}
