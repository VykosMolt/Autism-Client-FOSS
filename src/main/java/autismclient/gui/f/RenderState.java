package autismclient.gui.f;

public final class RenderState {
   public static final int init = 14;
   public static final int load = 13;
   public static final int read = 14;
   public static final int write = 12;
   public static final int update = 18;
   public static final int sync = 12;
   public static final int flush = 4;
   public static final int close = 2;
   public static final int open = 4;
   public static final int poll = 6;
   public static final int push = 5;
   public static final int pop = 24;

   private RenderState() {
   }

   public static int init(int base) {
      float scale = autismclient.gui.e.SyncManager.init_F();
      return scale <= 1.0F ? base : base + (int)Math.ceil(10.8F * (scale - 1.0F));
   }

   public static int load(int textWidth) {
      return Math.max(24, textWidth + 10);
   }
}
