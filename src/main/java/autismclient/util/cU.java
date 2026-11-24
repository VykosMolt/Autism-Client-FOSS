package autismclient.util;

import java.util.Objects;

public final class cU {
   private final cR init_cR;
   private Object init_Obj;
   private Object load_Obj;
   private String init_Str;
   private String load_Str;
   private cR.Inner_DataUtil init_cR_DataUtil;

   public cU(cR memory) {
      this.init_cR = Objects.requireNonNull(memory);
   }

   public boolean init(Object world, Object connection, String server, String dimension) {
      Objects.requireNonNull(world);
      Objects.requireNonNull(connection);
      Objects.requireNonNull(server);
      Objects.requireNonNull(dimension);
      if (this.init_Obj == world && this.load_Obj == connection && server.equals(this.init_Str) && dimension.equals(this.load_Str)) {
         return false;
      }

      this.init_Obj = world;
      this.load_Obj = connection;
      this.init_Str = server;
      this.load_Str = dimension;
      this.init_cR_DataUtil = this.init_cR.init(server, dimension);
      return true;
   }

   public boolean init_Z_Obj_Obj(Object world, Object connection) {
      return world != null && connection != null && this.init_Obj == world && this.load_Obj == connection;
   }

   public cR.Inner_DataUtil init_cR_DataUtil() {
      return this.init_cR_DataUtil;
   }

   public void init_V() {
      this.init_Obj = this.load_Obj = null;
      this.init_Str = this.load_Str = null;
      this.init_cR_DataUtil = null;
   }

   public static boolean init_Z_Z_Z(boolean skylight, boolean dragonFight) {
      return skylight && !dragonFight;
   }
}
