package autismclient.modules;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class CrosshairPixelArt {
   public static final int[] SIZES = new int[]{8, 16, 32, 64};
   private static final String init_Str = "1";
   private final int init_I;
   private final int[] load_IArr;
   private final int load_I;

   public CrosshairPixelArt(int size, int[] pixels) {
      this.init_I = isValidSize(size) ? size : 16;
      int[] copy = new int[this.init_I * this.init_I];
      if (pixels != null) {
         System.arraycopy(pixels, 0, copy, 0, Math.min(pixels.length, copy.length));
      }

      this.load_IArr = copy;
      this.load_I = 31 * this.init_I + Arrays.hashCode(copy);
   }

   public static boolean isValidSize(int size) {
      for (int s : SIZES) {
         if (s == size) {
            return true;
         }
      }

      return false;
   }

   public static CrosshairPixelArt init_m(int size) {
      return new CrosshairPixelArt(size, null);
   }

   public int size() {
      return this.init_I;
   }

   public int init(int x, int y) {
      return x >= 0 && y >= 0 && x < this.init_I && y < this.init_I ? this.load_IArr[y * this.init_I + x] : 0;
   }

   public int[] pixels() {
      return (int[])this.load_IArr.clone();
   }

   public boolean init_Z() {
      for (int p : this.load_IArr) {
         if (p >>> 24 != 0) {
            return false;
         }
      }

      return true;
   }

   public CrosshairPixelArt load(int newSize) {
      if (isValidSize(newSize) && newSize != this.init_I) {
         int[] out = new int[newSize * newSize];

         for (int y = 0; y < newSize; y++) {
            int sy = (int)((y + 0.5) * this.init_I / newSize);

            for (int x = 0; x < newSize; x++) {
               int sx = (int)((x + 0.5) * this.init_I / newSize);
               out[y * newSize + x] = this.load_IArr[sy * this.init_I + sx];
            }
         }

         return new CrosshairPixelArt(newSize, out);
      } else {
         return this;
      }
   }

   public String encode() {
      byte[] raw = new byte[this.load_IArr.length * 4];

      for (int i = 0; i < this.load_IArr.length; i++) {
         int p = this.load_IArr[i];
         raw[i * 4] = (byte)(p >>> 24);
         raw[i * 4 + 1] = (byte)(p >>> 16);
         raw[i * 4 + 2] = (byte)(p >>> 8);
         raw[i * 4 + 3] = (byte)p;
      }

      Deflater deflater = new Deflater(9);

      try {
         deflater.setInput(raw);
         deflater.finish();
         ByteArrayOutputStream out = new ByteArrayOutputStream(256);
         byte[] buffer = new byte[1024];

         while (!deflater.finished()) {
            out.write(buffer, 0, deflater.deflate(buffer));
         }

         return "1:" + this.init_I + ":" + Base64.getEncoder().encodeToString(out.toByteArray());
      } finally {
         deflater.end();
      }
   }

   public static CrosshairPixelArt init(String encoded) {
      if (encoded == null) {
         return null;
      }

      String[] parts = encoded.trim().split(":", 3);
      if (parts.length == 3 && "1".equals(parts[0])) {
         int size;
         try {
            size = Integer.parseInt(parts[1]);
         } catch (NumberFormatException e) {
            return null;
         }

         if (!isValidSize(size)) {
            return null;
         }

         byte[] packed;
         try {
            packed = Base64.getDecoder().decode(parts[2]);
         } catch (IllegalArgumentException e) {
            return null;
         }

         byte[] raw = new byte[size * size * 4];
         Inflater inflater = new Inflater();

         try {
            inflater.setInput(packed);
            int read = 0;

            while (read < raw.length && !inflater.finished()) {
               int n = inflater.inflate(raw, read, raw.length - read);
               if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                  break;
               }

               read += n;
            }

            if (read != raw.length || !inflater.finished()) {
               return null;
            }
         } catch (DataFormatException e) {
            return null;
         } finally {
            inflater.end();
         }

         int[] var171 = new int[size * size];

         for (int var181 = 0; var181 < var171.length; var181++) {
            var171[var181] = (raw[var181 * 4] & 255) << 24 | (raw[var181 * 4 + 1] & 255) << 16 | (raw[var181 * 4 + 2] & 255) << 8 | raw[var181 * 4 + 3] & 255;
         }

         return new CrosshairPixelArt(size, var171);
      } else {
         return null;
      }
   }

   @Override
   public boolean equals(Object other) {
      return this == other || other instanceof CrosshairPixelArt art && art.init_I == this.init_I && art.load_I == this.load_I && Arrays.equals(art.load_IArr, this.load_IArr);
   }

   @Override
   public int hashCode() {
      return this.load_I;
   }
}
