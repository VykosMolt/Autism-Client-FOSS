package autismclient.util.oresim;

import java.util.Objects;

public final class OreSimSeedParser {
   private static final OreSimSeedParser.ParseResult EMPTY = new OreSimSeedParser.ParseResult(OreSimSeedParser.Status.EMPTY, null);
   private static final OreSimSeedParser.ParseResult INVALID = new OreSimSeedParser.ParseResult(OreSimSeedParser.Status.INVALID, null);

   private OreSimSeedParser() {
   }

   public static OreSimSeedParser.ParseResult parse(String raw) {
      if (raw == null) {
         return EMPTY;
      }

      String input = raw.strip();
      if (input.isEmpty()) {
         return EMPTY;
      }

      try {
         return new OreSimSeedParser.ParseResult(OreSimSeedParser.Status.VALID, Long.parseLong(input));
      } catch (NumberFormatException ignored) {
         return INVALID;
      }
   }

   public record ParseResult(OreSimSeedParser.Status status, Long seed) {
      public ParseResult {
         Objects.requireNonNull(status, "status");
         if (status == OreSimSeedParser.Status.VALID != (seed != null)) {
            throw new IllegalArgumentException("Only a valid seed may carry a value");
         }
      }

      public boolean isValid() {
         return this.status == OreSimSeedParser.Status.VALID;
      }
   }

   public enum Status {
      EMPTY,
      VALID,
      INVALID;
   }
}
