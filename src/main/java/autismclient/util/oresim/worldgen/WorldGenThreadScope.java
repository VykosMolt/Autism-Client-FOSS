package autismclient.util.oresim.worldgen;

import java.util.Objects;
import java.util.function.Supplier;

public final class WorldGenThreadScope {
   private static final ThreadLocal<Integer> DEPTH = new ThreadLocal<>();

   private WorldGenThreadScope() {
   }

   public static boolean isActive() {
      Integer depth = DEPTH.get();
      return depth != null && depth > 0;
   }

   public static <T> T init(Supplier<T> operation) {
      Objects.requireNonNull(operation, "operation");
      Integer previous = DEPTH.get();
      DEPTH.set(previous == null ? 1 : previous + 1);

      try {
         return operation.get();
      } finally {
         if (previous == null) {
            DEPTH.remove();
         } else {
            DEPTH.set(previous);
         }
      }
   }
}
