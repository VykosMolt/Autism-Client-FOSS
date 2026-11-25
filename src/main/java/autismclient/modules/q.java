package autismclient.modules;


public final class q {
   private static volatile q.Inner_DataUtil init;

   private q() {
   }

   static void init(q.Inner_DataUtil value) {
      init = value;
   }

   static void init_V() {
      init = null;
   }

   public static q.Inner_DataUtil init_q_DataUtil() {
      q.Inner_DataUtil current = init;
      return current != null && init_Z() ? current : null;
   }

   private static boolean init_Z() {
      return true;
   }

   public record Inner_DataUtil(
      p.Inner_NetHelper init_p_NetHelper,
      double init_D,
      double load_D,
      double read_D,
      boolean init_Z,
      int init_I,
      float init_F,
      boolean load_Z,
      int load_I,
      double write_D,
      boolean read_Z,
      int read_I,
      int write_I,
      boolean write_Z,
      double update,
      m init_m
   ) {
   }
}
