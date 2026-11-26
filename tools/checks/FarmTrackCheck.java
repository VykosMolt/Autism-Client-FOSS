package autismclient.util;

public class FarmTrackCheck {
   public static void main(String[] args) {
      BoundedPositionCache<String> table = new BoundedPositionCache<>(4096);
      for (long i = 0; i < 4097; i++) {
         table.init(i, "cell" + i);
      }

      if (table.init_Obj(4096L) == null) {
         fail("position #4097 was rejected at capacity");
      }

      if (table.init_I() != 4096 || table.init_Obj(0L) != null) {
         fail("oldest position not evicted, size=" + table.init_I());
      }

      table.init(1L, "replaced");
      if (!"replaced".equals(table.init_Obj(1L)) || table.init_I() != 4096) {
         fail("replacing an existing position changed the size");
      }

      System.out.println("PASS FarmTrackCheck: full table evicts the oldest position");
   }

   private static void fail(String message) {
      System.out.println("FAIL FarmTrackCheck: " + message);
      System.exit(1);
   }
}
