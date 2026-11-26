package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class cA {
   public static final int init_I = 2;
   private static final double[] init_DArr = new double[]{0.5, 0.85, 1.0, 1.2, 1.5, 2.0};
   private static final double init_D = 1.0E-6;
   private static int load = 0;
   private static volatile cA.Inner_StreamUtil init_cA_StreamUtil;
   private static volatile cA.Inner_DataUtil init_cA_DataUtil;

   private cA() {
   }

   public static double init_D_D(double value) {
      return value / init_F();
   }

   public static int init_I_D(double value) {
      double virtual = init_D_D(value);
      return virtual < 0.0 ? (int)Math.floor(virtual) : (int)Math.round(virtual);
   }

   private static cA.Inner_StreamUtil init_cA_StreamUtil_Minecraft(Minecraft mc) {
      int rawW = mc.getWindow().getWidth();
      int rawH = mc.getWindow().getHeight();
      double multiplier = init_D();
      cA.Inner_StreamUtil m = init_cA_StreamUtil;
      if (m != null && m.init_I == rawW && m.load == rawH && m.init_D == multiplier) {
         return m;
      }

      double scale = 2.0 * multiplier;
      int width = (int)(rawW / scale);
      if (rawW / scale > width) {
         width++;
      }

      int height = (int)(rawH / scale);
      if (rawH / scale > height) {
         height++;
      }

      m = new cA.Inner_StreamUtil(rawW, rawH, multiplier, width, height);
      init_cA_StreamUtil = m;
      return m;
   }

   public static int init_I() {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.getWindow() != null ? init_cA_StreamUtil_Minecraft(mc).read : 0;
   }

   public static int load_I() {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.getWindow() != null ? init_cA_StreamUtil_Minecraft(mc).write : 0;
   }

   public static float init_F() {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.getWindow() != null && mc.getWindow().getGuiScale() > 0 ? (float)write() / mc.getWindow().getGuiScale() : 1.0F;
   }

   public static double init_D() {
      try {
         double configScale = AutismConfig.getGlobal().overlayScale;
         cA.Inner_DataUtil cached = init_cA_DataUtil;
         if (cached == null || cached.init != configScale) {
            cached = new cA.Inner_DataUtil(configScale, load_D_D(configScale));
            init_cA_DataUtil = cached;
         }

         return cached.load;
      } catch (Throwable ignored) {
         return 1.0;
      }
   }

   public static String init_Str() {
      return init_Str_D(init_D());
   }

   public static List<Double> init_List() {
      List<Double> options = new ArrayList<>(init_DArr.length);

      for (double scale : init_DArr) {
         options.add(scale);
      }

      return List.copyOf(options);
   }

   public static double load_D_D(double multiplier) {
      if (!(Math.abs(multiplier - 0.75) < 1.0E-6) && !(Math.abs(multiplier - 0.9) < 1.0E-6)) {
         double best = 1.0;
         double bestDelta = 1.7976931348623157E308;

         for (double scale : init_DArr) {
            double delta = Math.abs(multiplier - scale);
            if (delta < bestDelta) {
               best = scale;
               bestDelta = delta;
            }
         }

         return best;
      } else {
         return 0.85;
      }
   }

   public static double load_D() {
      return init_D_I(1);
   }

   public static double read() {
      return init_D_I(-1);
   }

   private static double init_D_I(int delta) {
      double current = init_D();

      for (int i = 0; i < init_DArr.length; i++) {
         if (Math.abs(current - init_DArr[i]) < 1.0E-6) {
            return init_DArr[Math.floorMod(i + delta, init_DArr.length)];
         }
      }

      return 1.0;
   }

   public static String init_Str_D(double multiplier) {
      double normalized = load_D_D(multiplier);
      return Math.abs(normalized - Math.rint(normalized)) < 1.0E-6 ? Integer.toString((int)Math.rint(normalized)) + "x" : Double.toString(normalized) + "x";
   }

   public static void init_V_D(double multiplier) {
      double normalized = load_D_D(multiplier);
      AutismConfig config = AutismConfig.getGlobal();
      if (!(Math.abs(config.overlayScale - normalized) < 1.0E-6)) {
         config.overlayScale = normalized;
         config.save();

         try {
            OverlayManager.init_bk().flush();
         } catch (Throwable var6) {
         }
      }
   }

   private static double write() {
      return 2.0 * init_D();
   }

   public static boolean init_Z() {
      return load > 0;
   }

   public static boolean load_Z() {
      return init_Z() && Math.abs(init_F() - 1.0F) > 0.001F;
   }

   public static int init_I_I(int x) {
      return (int)Math.floor(x * write());
   }

   public static int load_I_I(int y) {
      return (int)Math.floor(y * write());
   }

   public static int read(int size) {
      return Math.max(0, (int)Math.ceil(size * write()));
   }

   public static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context) {
      if (context != null) {
         context.pose().pushMatrix();
         if (load == 0) {
            float scale = init_F();
            context.pose().scale(scale, scale);
         }

         load++;
      }
   }

   public static void load_V_GuiGraphicsExtract(GuiGraphicsExtractor context) {
      if (context != null) {
         if (load > 0) {
            load--;
         }

         context.pose().popMatrix();
      }
   }

   public static void init(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
      if (context != null) {
         if (x2 > x1 && y2 > y1) {
            int screenW = Math.max(0, init_I());
            int screenH = Math.max(0, load_I());
            int left = init(x1, 0, screenW);
            int top = init(y1, 0, screenH);
            int right = init(x2, left, Math.max(left, screenW));
            int bottom = init(y2, top, Math.max(top, screenH));
            context.enableScissor(left, top, right, bottom);
         } else {
            context.enableScissor(x1, y1, x1, y1);
         }
      }
   }

   private static int init(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }

   private record Inner_DataUtil(double init, double load) {
   }

   private record Inner_StreamUtil(int init_I, int load, double init_D, int read, int write) {
   }
}
