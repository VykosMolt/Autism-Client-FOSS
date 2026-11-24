package autismclient.util.j.a;

import java.util.Objects;
import java.util.function.Supplier;

public final class DataUtil {
   private static final ThreadLocal<Integer> init = new ThreadLocal<>();

   private DataUtil() {
   }

   public static boolean init() {
      Integer depth = init.get();
      return depth != null && depth > 0;
   }

   public static <T> T init(Supplier<T> operation) {
      Objects.requireNonNull(operation, "operation");
      Integer previous = init.get();
      init.set(previous == null ? 1 : previous + 1);

      try {
         return operation.get();
      } finally {
         if (previous == null) {
            init.remove();
         } else {
            init.set(previous);
         }
      }
   }
}
