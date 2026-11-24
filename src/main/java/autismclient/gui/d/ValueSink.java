package autismclient.gui.d;

import autismclient.util.ct;

public final class ValueSink {
   public static final int init = -15987181;
   public static final int load = -401402080;
   public static final int read = -1206840035;
   public static final int write = -14249;
   public static int update = -12761513;
   public static int sync = -12588930;
   public static int flush = -854534;
   public static int close = -6774093;
   public static int open = -13248397;
   public static int poll = -42149;

   private ValueSink() {
   }

   public static void init() {
      update = ct.init_I_I_ct_DataUtil(-9547177, ct.Inner_DataUtil.load);
      sync = ct.init_I_I_ct_DataUtil(-12588930, ct.Inner_DataUtil.sync);
      flush = ct.init_I_I_ct_DataUtil(-854534, ct.Inner_DataUtil.read);
      close = ct.init_I_I_ct_DataUtil(-6774093, ct.Inner_DataUtil.read);
      open = ct.init_I_I_ct_DataUtil(-13248397, ct.Inner_DataUtil.sync);
      poll = ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.flush);
   }

   static {
      init();
   }
}
