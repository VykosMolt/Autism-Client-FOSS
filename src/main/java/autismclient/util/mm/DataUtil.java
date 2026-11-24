package autismclient.util.mm;

public final class DataUtil {
   public volatile autismclient.util.mm.a.ConfigStore init_ConfigStore;
   public final String init_Str;
   public final String load_Str;
   public final boolean init_Z;
   public final int init_I;
   public final long init_J;
   public final String read_Str;
   public final String write;
   public final String update;
   public final boolean load_Z;
   public final String sync;
   public volatile int load_I;
   public volatile boolean read_Z;

   public DataUtil(
      autismclient.util.mm.a.ConfigStore key,
      String name,
      boolean isPublic,
      int maxPlayers,
      String server,
      String plugins,
      String shareCode,
      boolean announcement,
      String ownerDid,
      int speakerVersion,
      boolean canSend
   ) {
      this.init_ConfigStore = key;
      this.init_Str = key.init_Str();
      this.load_Str = name;
      this.init_Z = isPublic;
      this.init_I = maxPlayers;
      this.init_J = System.currentTimeMillis();
      this.read_Str = server == null ? "" : server;
      this.write = plugins == null ? "" : plugins;
      this.update = shareCode == null ? "" : shareCode;
      this.load_Z = announcement;
      this.sync = ownerDid == null ? "" : ownerDid;
      this.load_I = speakerVersion;
      this.read_Z = canSend;
   }

   public String init() {
      return this.init_ConfigStore.load();
   }

   public String load() {
      return this.init_ConfigStore.write();
   }

   public String read() {
      return this.init_ConfigStore.update();
   }

   public void init(autismclient.util.mm.a.ConfigStore updated) {
      if (updated != null) {
         this.init_ConfigStore = updated;
      }
   }
}
