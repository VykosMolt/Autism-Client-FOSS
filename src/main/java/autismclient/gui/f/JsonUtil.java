package autismclient.gui.f;

import autismclient.gui.f.b.j;
import autismclient.util.ct;

public final class JsonUtil {
   public int init;
   public int load;
   public int read;
   public int write;
   public int update;
   public int sync;
   public int flush;
   public int close;
   public int open;
   public int poll;
   public int push;
   public int pop;
   public int tick;
   public int run;
   public int get;
   public int set;
   public int put;
   public int add;
   public int clear;
   public int reset;
   public int start;
   public int stop;
   public int next;
   public int prev;
   public int valid;
   public int size;
   public int data;

   public JsonUtil() {
      this.init();
   }

   public void init() {
      this.init(ct.init_ct_StreamUtil());
   }

   public void init(ct.Inner_StreamUtil state) {
      this.clear = ct.init_I_I_2(-50373, ct.Inner_DataUtil.init, state);
      this.init = 1208486416;
      this.update = this.init(1777705, 212, 0.035F);
      this.sync = this.init(1711912, 230, 0.035F);
      this.flush = this.init(3883598, 88, 0.045F);
      this.close = this.init(4805473, 120, 0.065F);
      this.open = this.init(2238772, 191, 0.035F);
      this.poll = this.init(2567737, 196, 0.035F);
      this.push = this.init(3358283, 206, 0.065F);
      this.pop = this.init(1251875, 196, 0.025F);
      this.tick = this.init(2042422, 210, 0.05F);
      this.load = this.init(3160391, 200, 0.035F);
      this.read = this.init(4345181, 212, 0.065F);
      this.run = this.init(96, 0.12F);
      this.get = this.init(60, 0.09F);
      this.set = this.init(76, 0.1F);
      this.put = this.init(34, 0.06F);
      this.add = 385875967;
      this.write = this.init(84, 0.06F);
      this.reset = ct.init_I_I_2(-7397596, ct.Inner_DataUtil.init, state);
      this.start = ct.init_I_I_2(1157577531, ct.Inner_DataUtil.init, state);
      this.stop = ct.init_I_I_2(-12588930, ct.Inner_DataUtil.sync, state);
      this.next = ct.init_I_I_2(1715464318, ct.Inner_DataUtil.sync, state);
      this.prev = ct.init_I_I_2(-854534, ct.Inner_DataUtil.read, state);
      this.valid = ct.init_I_I_2(-6774093, ct.Inner_DataUtil.read, state);
      this.size = ct.init_I_I_2(-10853776, ct.Inner_DataUtil.read, state);
      this.data = ct.init_I_I_2(-43691, ct.Inner_DataUtil.flush, state);
   }

   private int init(int rgb, int alpha, float tint) {
      int tinted = j.init_I_I(0xFF000000 | rgb & 16777215, 0xFF000000 | this.clear & 16777215, tint);
      return alpha << 24 | tinted & 16777215;
   }

   private int init(int alpha, float tint) {
      int tinted = j.init_I_I(-2300686, 0xFF000000 | this.clear & 16777215, tint);
      return alpha << 24 | tinted & 16777215;
   }
}
