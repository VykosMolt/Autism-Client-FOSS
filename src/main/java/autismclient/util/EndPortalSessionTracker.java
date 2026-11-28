package autismclient.util;

import java.util.Objects;

public final class EndPortalSessionTracker {
   private final EndPortalMemoryCache cache;
   private Object world;
   private Object connection;
   private String server;
   private String dimension;
   private EndPortalMemoryCache.DimensionMemory memory;

   public EndPortalSessionTracker(EndPortalMemoryCache memory) {
      this.cache = Objects.requireNonNull(memory);
   }

   public boolean init(Object world, Object connection, String server, String dimension) {
      Objects.requireNonNull(world);
      Objects.requireNonNull(connection);
      Objects.requireNonNull(server);
      Objects.requireNonNull(dimension);
      if (this.world == world && this.connection == connection && server.equals(this.server) && dimension.equals(this.dimension)) {
         return false;
      }

      this.world = world;
      this.connection = connection;
      this.server = server;
      this.dimension = dimension;
      this.memory = this.cache.memoryFor(server, dimension);
      return true;
   }

   public boolean isSameSession(Object world, Object connection) {
      return world != null && connection != null && this.world == world && this.connection == connection;
   }

   public EndPortalMemoryCache.DimensionMemory getMemory() {
      return this.memory;
   }

   public void reset() {
      this.world = this.connection = null;
      this.server = this.dimension = null;
      this.memory = null;
   }

   public static boolean isStrongholdDimension(boolean skylight, boolean dragonFight) {
      return skylight && !dragonFight;
   }
}
