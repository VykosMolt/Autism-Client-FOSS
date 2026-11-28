package autismclient.util;

import java.util.EnumMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class MouseMovementRouter {
   private static final Minecraft CLIENT = Minecraft.getInstance();
   private static final double init_D = 0.05;
   private static final EnumMap<MouseMovementRouter.MovementSource, FractionalCountAccumulator> ACCUMULATORS = new EnumMap<>(MouseMovementRouter.MovementSource.class);
   private static final TimedMouseSmoother SMOOTHER = new TimedMouseSmoother();
   private static volatile MouseMovementRouter.MovementSource exclusiveSource;
   private static boolean init_Z;
   private static long init_J;

   private MouseMovementRouter() {
   }

   public static void init_V_ce_DataUtil_ce_DataUtil(RotationMath.Rotation current, RotationMath.Rotation target) {
      submitRotation(MouseMovementRouter.MovementSource.GENERIC, current, target);
   }

   public static void submitRotation(MouseMovementRouter.MovementSource source, RotationMath.Rotation current, RotationMath.Rotation target) {
      if (current != null && target != null) {
         float yawDelta = RotationMath.angleDelta(target.yaw(), current.yaw());
         float pitchDelta = Mth.clamp(target.pitch(), -90.0F, 90.0F) - Mth.clamp(current.pitch(), -90.0F, 90.0F);
         submitDegrees(source, yawDelta, pitchDelta);
      }
   }

   public static void init_V_F_F(float yawDelta, float pitchDelta) {
      submitDegrees(MouseMovementRouter.MovementSource.GENERIC, yawDelta, pitchDelta);
   }

   public static void submitDegrees(MouseMovementRouter.MovementSource source, float yawDelta, float pitchDelta) {
      if (!read()) {
         release(source);
      } else {
         double degreesPerRawInput = RotationMath.mouseGcd();
         if (!(degreesPerRawInput <= 1.0E-8) && Double.isFinite(degreesPerRawInput)) {
            double rawX = yawDelta / degreesPerRawInput;
            double rawY = pitchDelta / degreesPerRawInput;
            if (CLIENT.options.invertMouseX().get()) {
               rawX = -rawX;
            }

            if (CLIENT.options.invertMouseY().get()) {
               rawY = -rawY;
            }

            init_V_ba_StreamUtil(source, rawX, rawY);
         }
      }
   }

   public static void submitCounts(MouseMovementRouter.MovementSource source, long yawCounts, long pitchCounts) {
      if (!read()) {
         release(source);
      } else {
         double rawX = yawCounts;
         double rawY = pitchCounts;
         if (CLIENT.options.invertMouseX().get()) {
            rawX = -rawX;
         }

         if (CLIENT.options.invertMouseY().get()) {
            rawY = -rawY;
         }

         init_V_ba_StreamUtil(source, rawX, rawY);
      }
   }

   public static void init_V_D_D(double deltaX, double deltaY) {
      init_V_ba_StreamUtil(MouseMovementRouter.MovementSource.GENERIC, deltaX, deltaY);
   }

   public static void init_V_ba_StreamUtil(MouseMovementRouter.MovementSource source, double deltaX, double deltaY) {
      if (!read()) {
         release(source);
      } else if (Double.isFinite(deltaX) && Double.isFinite(deltaY)) {
         if (source == MouseMovementRouter.MovementSource.AIM_ASSIST) {
            SMOOTHER.push(deltaX, deltaY, 0.05);
         } else if (source != MouseMovementRouter.MovementSource.SCAFFOLD_TELLY && source != MouseMovementRouter.MovementSource.AUTO_FISH) {
            if (!(Math.abs(deltaX) < 1.0E-7) || !(Math.abs(deltaY) < 1.0E-7)) {
               accumulatorFor(source).add(deltaX, deltaY);
            }
         } else {
            accumulatorFor(source).set(deltaX, deltaY);
         }
      }
   }

   public static MouseMovementRouter.MouseDelta consumeDelta() {
      if (!read()) {
         reset();
         return new MouseMovementRouter.MouseDelta(0.0, 0.0);
      }

      long now = System.nanoTime();
      double elapsedSeconds = init_J == 0L ? 0.016666666666666666 : Math.clamp((now - init_J) / 1.0E9, 0.001, 0.05);
      init_J = now;
      MouseMovementRouter.MovementSource exclusive = exclusiveSource;
      if (exclusive != null) {
         load(exclusive);
         FractionalCountAccumulator.WholeCounts counts = init_dh_DataUtil_ba_StreamUtil_D(exclusive, elapsedSeconds);
         return new MouseMovementRouter.MouseDelta(counts.x(), counts.y());
      }

      long x = 0L;
      long y = 0L;

      for (Entry<MouseMovementRouter.MovementSource, FractionalCountAccumulator> entry : ACCUMULATORS.entrySet()) {
         if (entry.getKey() != MouseMovementRouter.MovementSource.AIM_ASSIST && entry.getKey() != MouseMovementRouter.MovementSource.SCAFFOLD_TELLY) {
            FractionalCountAccumulator accumulator = entry.getValue();
            FractionalCountAccumulator.WholeCounts counts = accumulator.takeWhole();
            x += counts.x();
            y += counts.y();
         }
      }

      FractionalCountAccumulator.WholeCounts aim = SMOOTHER.drain(elapsedSeconds);
      x += aim.x();
      y += aim.y();
      FractionalCountAccumulator.WholeCounts telly = accumulatorFor(MouseMovementRouter.MovementSource.SCAFFOLD_TELLY).takeWhole();
      x += telly.x();
      y += telly.y();
      return new MouseMovementRouter.MouseDelta(x, y);
   }

   public static void init_V() {
      if (!read()) {
         reset();
      }
   }

   public static void reset() {
      for (FractionalCountAccumulator accumulator : ACCUMULATORS.values()) {
         accumulator.reset();
      }

      SMOOTHER.reset();
      exclusiveSource = null;
      init_Z = false;
      init_J = 0L;
   }

   public static void release(MouseMovementRouter.MovementSource source) {
      if (source == MouseMovementRouter.MovementSource.AUTO_FISH) {
         init_Z = false;
      }

      if (source == MouseMovementRouter.MovementSource.AIM_ASSIST) {
         SMOOTHER.reset();
         accumulatorFor(source).reset();
      } else if (source == MouseMovementRouter.MovementSource.SCAFFOLD_TELLY) {
         accumulatorFor(source).reset();
      } else {
         accumulatorFor(source).reset();
      }
   }

   public static void init(boolean active) {
      init_Z = active;
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static void init_V_ba_StreamUtil_Z(MouseMovementRouter.MovementSource source, boolean exclusive) {
      if (exclusive) {
         if (source != null) {
            exclusiveSource = source;
            load(source);
         }
      } else if (source == null || exclusiveSource == source) {
         exclusiveSource = null;
         if (source != null) {
            release(source);
         }
      }
   }

   public static boolean hasActiveSource() {
      return exclusiveSource != null;
   }

   public static boolean isActive(MouseMovementRouter.MovementSource source) {
      return source != null && exclusiveSource == source;
   }

   public static boolean read() {
      return CLIENT != null
         && CLIENT.player != null
         && CLIENT.level != null
         && CLIENT.options != null
         && CLIENT.getWindow() != null
         && CLIENT.mouseHandler != null
         && CLIENT.mouseHandler.isMouseGrabbed()
         && CLIENT.gui.screen() == null
         && CLIENT.gui.overlay() == null
         && !autismclient.modules.aD.init_Z();
   }

   private static FractionalCountAccumulator accumulatorFor(MouseMovementRouter.MovementSource source) {
      return ACCUMULATORS.get(source == null ? MouseMovementRouter.MovementSource.GENERIC : source);
   }

   private static FractionalCountAccumulator.WholeCounts init_dh_DataUtil_ba_StreamUtil_D(MouseMovementRouter.MovementSource source, double elapsedSeconds) {
      if (source == MouseMovementRouter.MovementSource.AIM_ASSIST) {
         return SMOOTHER.drain(elapsedSeconds);
      } else {
         return source == MouseMovementRouter.MovementSource.SCAFFOLD_TELLY ? accumulatorFor(source).takeWhole() : accumulatorFor(source).takeWhole();
      }
   }

   private static void load(MouseMovementRouter.MovementSource retained) {
      if (retained != MouseMovementRouter.MovementSource.AUTO_FISH) {
         init_Z = false;
      }

      for (Entry<MouseMovementRouter.MovementSource, FractionalCountAccumulator> entry : ACCUMULATORS.entrySet()) {
         if (entry.getKey() != retained) {
            entry.getValue().reset();
         }
      }

      if (retained != MouseMovementRouter.MovementSource.AIM_ASSIST) {
         SMOOTHER.reset();
      }
   }

   static {
      for (MouseMovementRouter.MovementSource source : MouseMovementRouter.MovementSource.values()) {
         ACCUMULATORS.put(source, new FractionalCountAccumulator());
      }
   }

   public record MouseDelta(double init, double y) {
      public boolean isZero() {
         return Math.abs(this.init) < 1.0E-7 && Math.abs(this.y) < 1.0E-7;
      }

      public double deltaX() {
         return this.init;
      }
   }

   public enum MovementSource {
      GENERIC,
      AIM_ASSIST,
      AUTO_FISH,
      SCAFFOLD_TELLY;
   }
}
