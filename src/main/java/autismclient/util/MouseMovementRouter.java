package autismclient.util;

import java.util.EnumMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class MouseMovementRouter {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final double init_D = 0.05;
   private static final EnumMap<MouseMovementRouter.Inner_StreamUtil, FractionalCountAccumulator> init_EnumMap = new EnumMap<>(MouseMovementRouter.Inner_StreamUtil.class);
   private static final TimedMouseSmoother init_dk = new TimedMouseSmoother();
   private static volatile MouseMovementRouter.Inner_StreamUtil init_ba_StreamUtil;
   private static boolean init_Z;
   private static long init_J;

   private MouseMovementRouter() {
   }

   public static void init_V_ce_DataUtil_ce_DataUtil(RotationMath.Rotation current, RotationMath.Rotation target) {
      submitRotation(MouseMovementRouter.Inner_StreamUtil.GENERIC, current, target);
   }

   public static void submitRotation(MouseMovementRouter.Inner_StreamUtil source, RotationMath.Rotation current, RotationMath.Rotation target) {
      if (current != null && target != null) {
         float yawDelta = RotationMath.angleDelta(target.yaw(), current.yaw());
         float pitchDelta = Mth.clamp(target.pitch(), -90.0F, 90.0F) - Mth.clamp(current.pitch(), -90.0F, 90.0F);
         submitDegrees(source, yawDelta, pitchDelta);
      }
   }

   public static void init_V_F_F(float yawDelta, float pitchDelta) {
      submitDegrees(MouseMovementRouter.Inner_StreamUtil.GENERIC, yawDelta, pitchDelta);
   }

   public static void submitDegrees(MouseMovementRouter.Inner_StreamUtil source, float yawDelta, float pitchDelta) {
      if (!read()) {
         release(source);
      } else {
         double degreesPerRawInput = RotationMath.mouseGcd();
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

   public static void submitCounts(MouseMovementRouter.Inner_StreamUtil source, long yawCounts, long pitchCounts) {
      if (!read()) {
         release(source);
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
      init_V_ba_StreamUtil(MouseMovementRouter.Inner_StreamUtil.GENERIC, deltaX, deltaY);
   }

   public static void init_V_ba_StreamUtil(MouseMovementRouter.Inner_StreamUtil source, double deltaX, double deltaY) {
      if (!read()) {
         release(source);
      } else if (Double.isFinite(deltaX) && Double.isFinite(deltaY)) {
         if (source == MouseMovementRouter.Inner_StreamUtil.AIM_ASSIST) {
            init_dk.push(deltaX, deltaY, 0.05);
         } else if (source != MouseMovementRouter.Inner_StreamUtil.SCAFFOLD_TELLY && source != MouseMovementRouter.Inner_StreamUtil.AUTO_FISH) {
            if (!(Math.abs(deltaX) < 1.0E-7) || !(Math.abs(deltaY) < 1.0E-7)) {
               init_dh(source).init(deltaX, deltaY);
            }
         } else {
            init_dh(source).load(deltaX, deltaY);
         }
      }
   }

   public static MouseMovementRouter.Inner_DataUtil consumeDelta() {
      if (!read()) {
         reset();
         return new MouseMovementRouter.Inner_DataUtil(0.0, 0.0);
      }

      long now = System.nanoTime();
      double elapsedSeconds = init_J == 0L ? 0.016666666666666666 : Math.clamp((now - init_J) / 1.0E9, 0.001, 0.05);
      init_J = now;
      MouseMovementRouter.Inner_StreamUtil exclusive = init_ba_StreamUtil;
      if (exclusive != null) {
         load(exclusive);
         FractionalCountAccumulator.Inner_DataUtil counts = init_dh_DataUtil_ba_StreamUtil_D(exclusive, elapsedSeconds);
         return new MouseMovementRouter.Inner_DataUtil(counts.init(), counts.load());
      }

      long x = 0L;
      long y = 0L;

      for (Entry<MouseMovementRouter.Inner_StreamUtil, FractionalCountAccumulator> entry : init_EnumMap.entrySet()) {
         if (entry.getKey() != MouseMovementRouter.Inner_StreamUtil.AIM_ASSIST && entry.getKey() != MouseMovementRouter.Inner_StreamUtil.SCAFFOLD_TELLY) {
            FractionalCountAccumulator accumulator = entry.getValue();
            FractionalCountAccumulator.Inner_DataUtil counts = accumulator.takeWhole();
            x += counts.init();
            y += counts.load();
         }
      }

      FractionalCountAccumulator.Inner_DataUtil aim = init_dk.drain(elapsedSeconds);
      x += aim.init();
      y += aim.load();
      FractionalCountAccumulator.Inner_DataUtil telly = init_dh(MouseMovementRouter.Inner_StreamUtil.SCAFFOLD_TELLY).takeWhole();
      x += telly.init();
      y += telly.load();
      return new MouseMovementRouter.Inner_DataUtil(x, y);
   }

   public static void init_V() {
      if (!read()) {
         reset();
      }
   }

   public static void reset() {
      for (FractionalCountAccumulator accumulator : init_EnumMap.values()) {
         accumulator.reset();
      }

      init_dk.reset();
      init_ba_StreamUtil = null;
      init_Z = false;
      init_J = 0L;
   }

   public static void release(MouseMovementRouter.Inner_StreamUtil source) {
      if (source == MouseMovementRouter.Inner_StreamUtil.AUTO_FISH) {
         init_Z = false;
      }

      if (source == MouseMovementRouter.Inner_StreamUtil.AIM_ASSIST) {
         init_dk.reset();
         init_dh(source).reset();
      } else if (source == MouseMovementRouter.Inner_StreamUtil.SCAFFOLD_TELLY) {
         init_dh(source).reset();
      } else {
         init_dh(source).reset();
      }
   }

   public static void init(boolean active) {
      init_Z = active;
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static void init_V_ba_StreamUtil_Z(MouseMovementRouter.Inner_StreamUtil source, boolean exclusive) {
      if (exclusive) {
         if (source != null) {
            init_ba_StreamUtil = source;
            load(source);
         }
      } else if (source == null || init_ba_StreamUtil == source) {
         init_ba_StreamUtil = null;
         if (source != null) {
            release(source);
         }
      }
   }

   public static boolean hasActiveSource() {
      return init_ba_StreamUtil != null;
   }

   public static boolean isActive(MouseMovementRouter.Inner_StreamUtil source) {
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

   private static FractionalCountAccumulator init_dh(MouseMovementRouter.Inner_StreamUtil source) {
      return init_EnumMap.get(source == null ? MouseMovementRouter.Inner_StreamUtil.GENERIC : source);
   }

   private static FractionalCountAccumulator.Inner_DataUtil init_dh_DataUtil_ba_StreamUtil_D(MouseMovementRouter.Inner_StreamUtil source, double elapsedSeconds) {
      if (source == MouseMovementRouter.Inner_StreamUtil.AIM_ASSIST) {
         return init_dk.drain(elapsedSeconds);
      } else {
         return source == MouseMovementRouter.Inner_StreamUtil.SCAFFOLD_TELLY ? init_dh(source).takeWhole() : init_dh(source).takeWhole();
      }
   }

   private static void load(MouseMovementRouter.Inner_StreamUtil retained) {
      if (retained != MouseMovementRouter.Inner_StreamUtil.AUTO_FISH) {
         init_Z = false;
      }

      for (Entry<MouseMovementRouter.Inner_StreamUtil, FractionalCountAccumulator> entry : init_EnumMap.entrySet()) {
         if (entry.getKey() != retained) {
            entry.getValue().reset();
         }
      }

      if (retained != MouseMovementRouter.Inner_StreamUtil.AIM_ASSIST) {
         init_dk.reset();
      }
   }

   static {
      for (MouseMovementRouter.Inner_StreamUtil source : MouseMovementRouter.Inner_StreamUtil.values()) {
         init_EnumMap.put(source, new FractionalCountAccumulator());
      }
   }

   public record Inner_DataUtil(double init, double load) {
      public boolean isZero() {
         return Math.abs(this.init) < 1.0E-7 && Math.abs(this.load) < 1.0E-7;
      }

      public double deltaX() {
         return this.init;
      }
   }

   public enum Inner_StreamUtil {
      GENERIC,
      AIM_ASSIST,
      AUTO_FISH,
      SCAFFOLD_TELLY;
   }
}
