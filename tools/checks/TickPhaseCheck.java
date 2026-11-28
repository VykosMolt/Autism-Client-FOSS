package autismclient.ai;

import java.util.ArrayList;
import java.util.List;

public class TickPhaseCheck {
   public static void main(String[] args) {
      AiTickQueue phase = new AiTickQueue();
      List<Throwable> failures = new ArrayList<>();
      try {
         phase.runPhase(() -> {
            throw new IllegalStateException("tick boom");
         }, failures::add);
      } catch (Throwable escaped) {
         System.out.println("FAIL TickPhaseCheck: tick exception escaped: " + escaped);
         System.exit(1);
      }

      boolean[] ran = new boolean[1];
      phase.runPhase(() -> ran[0] = true, failures::add);
      if (failures.size() != 1 || !"tick boom".equals(failures.get(0).getMessage()) || !ran[0]) {
         System.out.println("FAIL TickPhaseCheck: failures=" + failures + " ranAgain=" + ran[0]);
         System.exit(1);
      }

      System.out.println("PASS TickPhaseCheck");
   }
}
