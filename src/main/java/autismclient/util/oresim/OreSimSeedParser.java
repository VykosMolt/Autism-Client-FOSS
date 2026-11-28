package autismclient.util.oresim;

import java.util.Objects;

public final class OreSimSeedParser {
   private static final OreSimSeedParser.Inner_DataUtil EMPTY = new OreSimSeedParser.Inner_DataUtil(OreSimSeedParser.Inner_StreamUtil.EMPTY, null);
   private static final OreSimSeedParser.Inner_DataUtil INVALID = new OreSimSeedParser.Inner_DataUtil(OreSimSeedParser.Inner_StreamUtil.INVALID, null);

   private OreSimSeedParser() {
   }

   public static OreSimSeedParser.Inner_DataUtil parse(String raw) {
      if (raw == null) {
         return EMPTY;
      }

      String input = raw.strip();
      if (input.isEmpty()) {
         return EMPTY;
      }

      try {
         return new OreSimSeedParser.Inner_DataUtil(OreSimSeedParser.Inner_StreamUtil.VALID, Long.parseLong(input));
      } catch (NumberFormatException ignored) {
         return INVALID;
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
