package autismclient.util;

public class FarmTrackCheck {
   public static void main(String[] args) {
      BoundedPositionCache<String> table = new BoundedPositionCache<>(4096);
      for (long i = 0; i < 4097; i++) {
         table.put(i, "cell" + i);
      }

      if (table.get(4096L) == null) {
         fail("position #4097 was rejected at capacity");
      }

      if (table.size() != 4096 || table.get(0L) != null) {
         fail("oldest position not evicted, size=" + table.size());
      }

      table.put(1L, "replaced");
      if (!"replaced".equals(table.get(1L)) || table.size() != 4096) {
         fail("replacing an existing position changed the size");
      }

      System.out.println("PASS FarmTrackCheck: full table evicts the oldest position");
   }

   private static void fail(String message) {
      System.out.println("FAIL FarmTrackCheck: " + message);
      System.exit(1);
   }
}
