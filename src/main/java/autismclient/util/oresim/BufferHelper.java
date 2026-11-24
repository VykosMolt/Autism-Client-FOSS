package autismclient.util.oresim;

import java.util.Objects;

public final class BufferHelper {
   private static final BufferHelper.Inner_DataUtil init = new BufferHelper.Inner_DataUtil(BufferHelper.Inner_StreamUtil.init, null);
   private static final BufferHelper.Inner_DataUtil load = new BufferHelper.Inner_DataUtil(BufferHelper.Inner_StreamUtil.read, null);

   private BufferHelper() {
   }

   public static BufferHelper.Inner_DataUtil init(String raw) {
      if (raw == null) {
         return init;
      }

      String input = raw.strip();
      if (input.isEmpty()) {
         return init;
      }

      try {
         return new BufferHelper.Inner_DataUtil(BufferHelper.Inner_StreamUtil.load, Long.parseLong(input));
      } catch (NumberFormatException ignored) {
         return load;
      }
   }

   public record Inner_DataUtil(BufferHelper.Inner_StreamUtil init_BufferHelper_Strea, Long init_Long) {
      public Inner_DataUtil {
         Objects.requireNonNull(init_BufferHelper_Strea, "status");
         if (init_BufferHelper_Strea == BufferHelper.Inner_StreamUtil.load != (init_Long != null)) {
            throw new IllegalArgumentException("Only a valid seed may carry a value");
         }
      }

      public boolean init_Z() {
         return this.init_BufferHelper_Strea == BufferHelper.Inner_StreamUtil.load;
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}
