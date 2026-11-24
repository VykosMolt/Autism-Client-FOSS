package autismclient.util.g.a;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ConfigStore {
   private static final int read_I = 1128746545;
   private static final int write_I = 1128746546;
   static final int init_I = 32;
   static final int load_I = 48;
   private final boolean init_Z;
   private final int update_I;
   private int sync_I;
   private int flush_I;
   private float[] init_FArr;
   private float[] load_FArr;
   private float[] read_FArr;
   private float[] write_FArr;
   private static final int close_I = 24;
   private static final int open_I = 48;
   private static final int poll_I = 3;
   private static final int push_I = 96;
   private static final int pop_I = 12;
   private static final int tick = 8;
   private float[] update_FArr;
   private float[] sync_FArr;
   private float[] flush_FArr;
   private float[] close_FArr;
   private float[] open_FArr;
   private float[] poll_FArr;
   private float[] push_FArr;
   private float[] pop_FArr;

   private ConfigStore(boolean cnn, int outputs) {
      this.init_Z = cnn;
      this.update_I = outputs;
   }

   public int init_I() {
      return this.update_I;
   }

   public int load() {
      return 1536;
   }

   public static ConfigStore init_ConfigStore() {
      try (InputStream is = ConfigStore.class.getResourceAsStream("/assets/autismclient/captcha/glyphnet.bin")) {
         if (is == null) {
            return null;
         } else {
            DataInputStream in = new DataInputStream(is);
            int magic = in.readInt();
            if (magic == 1128746546) {
               return init_ConfigStore_DataInputStream(in);
            } else {
               return magic == 1128746545 ? load_ConfigStore_DataInputStream(in) : null;
            }
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static ConfigStore init_ConfigStore_DataInputStream(DataInputStream in) throws IOException {
      int classes = in.readInt();
      if (classes > 0 && classes <= 256) {
         ConfigStore n = new ConfigStore(true, classes);
         n.update_FArr = init(in, 216);
         n.sync_FArr = init(in, 24);
         n.flush_FArr = init(in, 10368);
         n.close_FArr = init(in, 48);
         n.open_FArr = init(in, 442368);
         n.poll_FArr = init(in, 96);
         n.push_FArr = init(in, classes * 96);
         n.pop_FArr = init(in, classes);
         return n;
      } else {
         return null;
      }
   }

   private static ConfigStore load_ConfigStore_DataInputStream(DataInputStream in) throws IOException {
      int dim = in.readInt();
      int hid = in.readInt();
      int out = in.readInt();
      if (dim > 0 && hid > 0 && out > 0 && dim <= 1048576 && hid <= 65536 && out <= 256) {
         ConfigStore n = new ConfigStore(false, out);
         n.sync_I = dim;
         n.flush_I = hid;
         n.init_FArr = init(in, dim * hid);
         n.load_FArr = init(in, hid);
         n.read_FArr = init(in, hid * out);
         n.write_FArr = init(in, out);
         return n;
      } else {
         return null;
      }
   }

   private static float[] init(DataInputStream in, int n) throws IOException {
      float[] a = new float[n];

      for (int i = 0; i < n; i++) {
         a[i] = in.readFloat();
      }

      return a;
   }

   public float[] init_FArr_FArr(float[] x) {
      if (x != null && x.length == 1536) {
         return this.init_Z ? this.load_FArr_FArr(x) : this.read(x);
      } else {
         return null;
      }
   }

   private float[] load_FArr_FArr(float[] x) {
      float[] a1 = new float[36864];
      init(x, 1, 48, 32, this.update_FArr, this.sync_FArr, 24, a1);
      init_V_FArr(a1);
      int h1 = 24;
      int w1c = 16;
      float[] p1 = new float[24 * h1 * w1c];
      init(a1, 24, 48, 32, p1);
      float[] a2 = new float[48 * h1 * w1c];
      init(p1, 24, h1, w1c, this.flush_FArr, this.close_FArr, 48, a2);
      init_V_FArr(a2);
      float[] p2 = new float[4608];
      init(a2, 48, h1, w1c, p2);
      float[] h = new float[96];

      for (int o = 0; o < 96; o++) {
         float s = this.poll_FArr[o];
         int base = o * p2.length;

         for (int i = 0; i < p2.length; i++) {
            s += this.open_FArr[base + i] * p2[i];
         }

         h[o] = s > 0.0F ? s : 0.0F;
      }

      float[] logit = new float[this.update_I];

      for (int o = 0; o < this.update_I; o++) {
         float s = this.pop_FArr[o];
         int base = o * 96;

         for (int j = 0; j < 96; j++) {
            s += this.push_FArr[base + j] * h[j];
         }

         logit[o] = s;
      }

      return write(logit);
   }

   private static void init(float[] in, int inC, int H, int W, float[] w, float[] b, int outC, float[] out) {
      for (int oc = 0; oc < outC; oc++) {
         int obase = oc * H * W;
         float bias = b[oc];

         for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
               float s = bias;

               for (int ic = 0; ic < inC; ic++) {
                  int ibase = ic * H * W;
                  int wbase = (oc * inC + ic) * 3 * 3;

                  for (int ky = 0; ky < 3; ky++) {
                     int iy = y + ky - 1;
                     if (iy >= 0 && iy < H) {
                        for (int kx = 0; kx < 3; kx++) {
                           int ix = x + kx - 1;
                           if (ix >= 0 && ix < W) {
                              s += in[ibase + iy * W + ix] * w[wbase + ky * 3 + kx];
                           }
                        }
                     }
                  }
               }

               out[obase + y * W + x] = s;
            }
         }
      }
   }

   private static void init(float[] in, int C, int H, int W, float[] out) {
      int oh = H / 2;
      int ow = W / 2;

      for (int c = 0; c < C; c++) {
         int ibase = c * H * W;
         int obase = c * oh * ow;

         for (int y = 0; y < oh; y++) {
            for (int x = 0; x < ow; x++) {
               int iy = y * 2;
               int ix = x * 2;
               float m = in[ibase + iy * W + ix];
               m = Math.max(m, in[ibase + iy * W + ix + 1]);
               m = Math.max(m, in[ibase + (iy + 1) * W + ix]);
               m = Math.max(m, in[ibase + (iy + 1) * W + ix + 1]);
               out[obase + y * ow + x] = m;
            }
         }
      }
   }

   private static void init_V_FArr(float[] a) {
      for (int i = 0; i < a.length; i++) {
         if (a[i] < 0.0F) {
            a[i] = 0.0F;
         }
      }
   }

   private float[] read(float[] x) {
      float[] h = new float[this.flush_I];

      for (int j = 0; j < this.flush_I; j++) {
         h[j] = this.load_FArr[j];
      }

      for (int i = 0; i < this.sync_I; i++) {
         float xi = x[i];
         if (xi != 0.0F) {
            int base = i * this.flush_I;

            for (int j = 0; j < this.flush_I; j++) {
               h[j] += xi * this.init_FArr[base + j];
            }
         }
      }

      for (int j = 0; j < this.flush_I; j++) {
         if (h[j] < 0.0F) {
            h[j] = 0.0F;
         }
      }

      float[] logit = new float[this.update_I];

      for (int o = 0; o < this.update_I; o++) {
         logit[o] = this.write_FArr[o];
      }

      for (int j = 0; j < this.flush_I; j++) {
         float hj = h[j];
         if (hj != 0.0F) {
            int base = j * this.update_I;

            for (int o = 0; o < this.update_I; o++) {
               logit[o] += hj * this.read_FArr[base + o];
            }
         }
      }

      return write(logit);
   }

   private static float[] write(float[] logit) {
      float max = -1.0F / 0.0F;

      for (float l : logit) {
         if (l > max) {
            max = l;
         }
      }

      float sum = 0.0F;

      for (int o = 0; o < logit.length; o++) {
         logit[o] = (float)Math.exp(logit[o] - max);
         sum += logit[o];
      }

      if (sum <= 0.0F) {
         return null;
      }

      for (int o = 0; o < logit.length; o++) {
         logit[o] /= sum;
      }

      return logit;
   }
}
