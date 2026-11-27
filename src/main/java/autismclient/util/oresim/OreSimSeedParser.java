package autismclient.util.oresim;

import java.util.Objects;

public final class OreSimSeedParser {
   private static final OreSimSeedParser.Inner_DataUtil init = new OreSimSeedParser.Inner_DataUtil(OreSimSeedParser.Inner_StreamUtil.EMPTY, null);
   private static final OreSimSeedParser.Inner_DataUtil load = new OreSimSeedParser.Inner_DataUtil(OreSimSeedParser.Inner_StreamUtil.INVALID, null);

   private OreSimSeedParser() {
   }

   public static OreSimSeedParser.Inner_DataUtil init(String raw) {
      if (raw == null) {
         return init;
      }

      String input = raw.strip();
      if (input.isEmpty()) {
         return init;
      }

      try {
         return new OreSimSeedParser.Inner_DataUtil(OreSimSeedParser.Inner_StreamUtil.VALID, Long.parseLong(input));
      } catch (NumberFormatException ignored) {
         return load;
      }
   }

   public record Inner_DataUtil(OreSimSeedParser.Inner_StreamUtil status, Long seed) {
      public Inner_DataUtil {
         Objects.requireNonNull(status, "status");
         if (status == OreSimSeedParser.Inner_StreamUtil.VALID != (seed != null)) {
            throw new IllegalArgumentException("Only a valid seed may carry a value");
         }
      }

      public boolean isValid() {
         return this.status == OreSimSeedParser.Inner_StreamUtil.VALID;
      }
   }

   public enum Inner_StreamUtil {
      EMPTY,
      VALID,
      INVALID;
   }
}
