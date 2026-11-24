package autismclient.util.mm;

import java.util.Comparator;

public final class StreamUtil {
   public static final Comparator<StreamUtil> init_Comparator = Comparator.<StreamUtil, Boolean>comparing(l -> !l.load_Z)
      .thenComparing(l -> l.load_Str, String.CASE_INSENSITIVE_ORDER);
   public final String init_Str;
   public volatile String load_Str;
   public volatile int init_I;
   public volatile int load_I;
   public volatile boolean init_Z;
   public volatile boolean load_Z;
   public volatile String read_Str = "";
   public volatile String write = "";
   public volatile int read_I = -1;
   public volatile boolean read_Z;
   public final String update;
   public final String sync;
   public volatile String flush = "";
   public volatile long init_J;

   public StreamUtil(String lobbyId, byte[] hostFp) {
      this.init_Str = lobbyId;
      this.update = init_Str_BArr(hostFp);
      this.sync = autismclient.util.mm.a.CachePool.init_Str_BArr(hostFp);
   }

   public boolean init() {
      return this.load_I > 0 && this.init_I >= this.load_I;
   }

   public boolean init_Z_J(long now) {
      return now - this.init_J < 45000L;
   }

   private static String init_Str_BArr(byte[] b) {
      StringBuilder sb = new StringBuilder(b.length * 2);

      for (byte x : b) {
         sb.append(Character.forDigit(x >> 4 & 15, 16)).append(Character.forDigit(x & 15, 16));
      }

      return sb.toString();
   }
}
