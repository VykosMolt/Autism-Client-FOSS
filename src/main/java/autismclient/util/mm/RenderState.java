package autismclient.util.mm;

import java.util.Locale;

public final class RenderState {
   public final String init_Str;
   public final String load_Str;
   public volatile String read_Str = "";
   public volatile long init_J;
   public volatile boolean init_Z;
   public volatile String write_Str = "";
   public volatile String update_Str = "";
   public volatile boolean load_Z;
   public volatile String sync = "";
   public volatile double init_D;
   public volatile double load_D;
   public volatile double read_D;
   public volatile int init_I = -1;
   public volatile String flush = "";
   public volatile String close = "";
   public volatile boolean read_Z;
   public volatile boolean write_Z;
   public volatile String open = "user";
   public volatile boolean update_Z;

   public RenderState(byte[] senderFp) {
      this.init_Str = init_Str_BArr(senderFp);
      this.load_Str = init_Str_Str(this.init_Str);
   }

   public boolean init_Z_J(long now) {
      return now - this.init_J < 30000L;
   }

   public String init() {
      String discord = this.close == null ? "" : this.close.trim();
      return discord.isEmpty() ? "Discord member" : discord;
   }

   public String load() {
      return this.read_Str == null ? "" : this.read_Str.trim();
   }

   private static String init_Str_BArr(byte[] b) {
      StringBuilder sb = new StringBuilder(b.length * 2);

      for (byte x : b) {
         sb.append(Character.forDigit(x >> 4 & 15, 16)).append(Character.forDigit(x & 15, 16));
      }

      return sb.toString();
   }

   private static String init_Str_Str(String hex) {
      String upper = hex.toUpperCase(Locale.ROOT);
      StringBuilder out = new StringBuilder(upper.length() + upper.length() / 4);

      for (int i = 0; i < upper.length(); i += 4) {
         if (i > 0) {
            out.append('-');
         }

         out.append(upper, i, Math.min(upper.length(), i + 4));
      }

      return out.toString();
   }
}
