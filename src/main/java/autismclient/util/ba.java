package autismclient.util;

import java.util.EnumMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class ba {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final double init_D = 0.05;
   private static final EnumMap<ba.Inner_StreamUtil, dh> init_EnumMap = new EnumMap<>(ba.Inner_StreamUtil.class);
   private static final dk init_dk = new dk();
   private static volatile ba.Inner_StreamUtil init_ba_StreamUtil;
   private static boolean init_Z;
   private static long init_J;

   private ba() {
   }

   public static void init_V_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil current, ce.Inner_DataUtil target) {
      init_V_ba_StreamUtil_4(ba.Inner_StreamUtil.init, current, target);
   }

   public static void init_V_ba_StreamUtil_4(ba.Inner_StreamUtil source, ce.Inner_DataUtil current, ce.Inner_DataUtil target) {
      if (current != null && target != null) {
         float yawDelta = ce.init(target.init(), current.init());
         float pitchDelta = Mth.clamp(target.load(), -90.0F, 90.0F) - Mth.clamp(current.load(), -90.0F, 90.0F);
         init_V_ba_StreamUtil_2(source, yawDelta, pitchDelta);
      }
   }

   public static void init_V_F_F(float yawDelta, float pitchDelta) {
      init_V_ba_StreamUtil_2(ba.Inner_StreamUtil.init, yawDelta, pitchDelta);
   }

   public static void init_V_ba_StreamUtil_2(ba.Inner_StreamUtil source, float yawDelta, float pitchDelta) {
      if (!read()) {
         init_V(source);
      } else {
         double degreesPerRawInput = ce.load();
         if (!(degreesPerRawInput <= 1.0E-8) && Double.isFinite(degreesPerRawInput)) {
            double rawX = yawDelta / degreesPerRawInput;
            double rawY = pitchDelta / degreesPerRawInput;
            if (init_Minecraft.options.invertMouseX().get()) {
               rawX = -rawX;
            }

            if (init_Minecraft.options.invertMouseY().get()) {
               rawY = -rawY;
            }

            init_V_ba_StreamUtil(source, rawX, rawY);
         }
      }
   }

   public static void init_V_ba_StreamUtil_3(ba.Inner_StreamUtil source, long yawCounts, long pitchCounts) {
      if (!read()) {
         init_V(source);
      } else {
         double rawX = yawCounts;
         double rawY = pitchCounts;
         if (init_Minecraft.options.invertMouseX().get()) {
            rawX = -rawX;
         }

         if (init_Minecraft.options.invertMouseY().get()) {
            rawY = -rawY;
         }

         init_V_ba_StreamUtil(source, rawX, rawY);
      }
   }

   public static void init_V_D_D(double deltaX, double deltaY) {
      init_V_ba_StreamUtil(ba.Inner_StreamUtil.init, deltaX, deltaY);
   }

   public static void init_V_ba_StreamUtil(ba.Inner_StreamUtil source, double deltaX, double deltaY) {
      if (!read()) {
         init_V(source);
      } else if (Double.isFinite(deltaX) && Double.isFinite(deltaY)) {
         if (source == ba.Inner_StreamUtil.load) {
            init_dk.init_V_D(deltaX, deltaY, 0.05);
         } else if (source != ba.Inner_StreamUtil.write && source != ba.Inner_StreamUtil.read) {
            if (!(Math.abs(deltaX) < 1.0E-7) || !(Math.abs(deltaY) < 1.0E-7)) {
               init_dh(source).init(deltaX, deltaY);
            }
         } else {
            init_dh(source).load(deltaX, deltaY);
         }
      }
   }

   public static ba.Inner_DataUtil init_ba_DataUtil() {
      if (!read()) {
         load_V();
         return new ba.Inner_DataUtil(0.0, 0.0);
      }

      long now = System.nanoTime();
      double elapsedSeconds = init_J == 0L ? 0.016666666666666666 : Math.clamp((now - init_J) / 1.0E9, 0.001, 0.05);
      init_J = now;
      ba.Inner_StreamUtil exclusive = init_ba_StreamUtil;
      if (exclusive != null) {
         load(exclusive);
         dh.Inner_DataUtil counts = init_dh_DataUtil_ba_StreamUtil_D(exclusive, elapsedSeconds);
         return new ba.Inner_DataUtil(counts.init(), counts.load());
      }

      long x = 0L;
      long y = 0L;

      for (Entry<ba.Inner_StreamUtil, dh> entry : init_EnumMap.entrySet()) {
         if (entry.getKey() != ba.Inner_StreamUtil.load && entry.getKey() != ba.Inner_StreamUtil.write) {
            dh accumulator = entry.getValue();
            dh.Inner_DataUtil counts = accumulator.init_dh_DataUtil();
            x += counts.init();
            y += counts.load();
         }
      }

      dh.Inner_DataUtil aim = init_dk.init_dh_DataUtil_D(elapsedSeconds);
      x += aim.init();
      y += aim.load();
      dh.Inner_DataUtil telly = init_dh(ba.Inner_StreamUtil.write).init_dh_DataUtil();
      x += telly.init();
      y += telly.load();
      return new ba.Inner_DataUtil(x, y);
   }

   public static void init_V() {
      if (!read()) {
         load_V();
      }
   }

   public static void load_V() {
      for (dh accumulator : init_EnumMap.values()) {
         accumulator.init_V();
      }

      init_dk.init_V();
      init_ba_StreamUtil = null;
      init_Z = false;
      init_J = 0L;
   }

   public static void init_V(ba.Inner_StreamUtil source) {
      if (source == ba.Inner_StreamUtil.read) {
         init_Z = false;
      }

      if (source == ba.Inner_StreamUtil.load) {
         init_dk.init_V();
         init_dh(source).init_V();
      } else if (source == ba.Inner_StreamUtil.write) {
         init_dh(source).init_V();
      } else {
         init_dh(source).init_V();
      }
   }

   public static void init(boolean active) {
      init_Z = active;
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static void init_V_ba_StreamUtil_Z(ba.Inner_StreamUtil source, boolean exclusive) {
      if (exclusive) {
         if (source != null) {
            init_ba_StreamUtil = source;
            load(source);
         }
      } else if (source == null || init_ba_StreamUtil == source) {
         init_ba_StreamUtil = null;
         if (source != null) {
            init_V(source);
         }
      }
   }

   public static boolean load_Z() {
      return init_ba_StreamUtil != null;
   }

   public static boolean init_Z(ba.Inner_StreamUtil source) {
      return source != null && init_ba_StreamUtil == source;
   }

   public static boolean read() {
      return init_Minecraft != null
         && init_Minecraft.player != null
         && init_Minecraft.level != null
         && init_Minecraft.options != null
         && init_Minecraft.getWindow() != null
         && init_Minecraft.mouseHandler != null
         && init_Minecraft.mouseHandler.isMouseGrabbed()
         && init_Minecraft.gui.screen() == null
         && init_Minecraft.gui.overlay() == null
         && !autismclient.modules.aD.init_Z();
   }

   private static dh init_dh(ba.Inner_StreamUtil source) {
      return init_EnumMap.get(source == null ? ba.Inner_StreamUtil.init : source);
   }

   private static dh.Inner_DataUtil init_dh_DataUtil_ba_StreamUtil_D(ba.Inner_StreamUtil source, double elapsedSeconds) {
      if (source == ba.Inner_StreamUtil.load) {
         return init_dk.init_dh_DataUtil_D(elapsedSeconds);
      } else {
         return source == ba.Inner_StreamUtil.write ? init_dh(source).init_dh_DataUtil() : init_dh(source).init_dh_DataUtil();
      }
   }

   private static void load(ba.Inner_StreamUtil retained) {
      if (retained != ba.Inner_StreamUtil.read) {
         init_Z = false;
      }

      for (Entry<ba.Inner_StreamUtil, dh> entry : init_EnumMap.entrySet()) {
         if (entry.getKey() != retained) {
            entry.getValue().init_V();
         }
      }

      if (retained != ba.Inner_StreamUtil.load) {
         init_dk.init_V();
      }
   }

   static {
      for (ba.Inner_StreamUtil source : ba.Inner_StreamUtil.values()) {
         init_EnumMap.put(source, new dh());
      }
   }

   public record Inner_DataUtil(double init, double load) {
      public boolean init_Z() {
         return Math.abs(this.init) < 1.0E-7 && Math.abs(this.load) < 1.0E-7;
      }

      public double init_D() {
         return this.init;
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
