package autismclient.util.mm;

public final class CachePool {
   public static final int init_I = 4;
   public static final int load_I = 4;
   public static final int read_I = 8;
   public static final int write = 50;
   public static final int update = 50;
   public static final int sync = 500;
   public static final int flush = 500;
   public final String init_Str;
   public final boolean init_Z;
   public final int close;
   public final char[] init_CArr;
   public final String load_Str;
   public final String read_Str;
   public final boolean load_Z;

   public CachePool(String name, boolean isPublic, int maxPlayers, char[] passphrase) {
      this(name, isPublic, maxPlayers, passphrase, "", "", false);
   }

   public CachePool(String name, boolean isPublic, int maxPlayers, char[] passphrase, String server, String plugins) {
      this(name, isPublic, maxPlayers, passphrase, server, plugins, false);
   }

   public CachePool(String name, boolean isPublic, int maxPlayers, char[] passphrase, String server, String plugins, boolean announcement) {
      this.init_Str = name != null && !name.isBlank() ? name.trim() : "Lobby";
      this.init_Z = isPublic;
      this.close = maxPlayers <= 0 ? 500 : Math.max(2, Math.min(500, maxPlayers));
      this.init_CArr = passphrase;
      this.load_Str = server == null ? "" : server.trim();
      this.read_Str = plugins == null ? "" : plugins.trim();
      this.load_Z = announcement;
   }
}
