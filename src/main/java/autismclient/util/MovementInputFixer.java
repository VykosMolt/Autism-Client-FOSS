package autismclient.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;

public final class MovementInputFixer {
   static final float init_F = 67.5F;
   static final float load_F = 112.5F;
   static final float read_F = 7.5F;
   static final float write_F = 0.5F;
   static final int init_I = 2;
   static final float update_F = 0.75F;
   public static final int load_I = 3;
   static final float sync_F = 90.0F;
   private static final int read_I = -2147483648;
   private static final int[] FORWARD_KEYS = new int[]{7, 0, 1};
   private static final int[] BACKWARD_KEYS = new int[]{3, 4, 5};
   private static final int[] POSITIVE_SIDE_KEYS = new int[]{2};
   private static final int[] NEGATIVE_SIDE_KEYS = new int[]{6};
   private int currentBand = -2147483648;
   private int chosen = -1;
   private int chosenStreak;
   private float lateralDrift;
   private float pressed = 0.0F / 0.0F;
   private int clientTick = -2147483648;
   private int prevBand = -2147483648;
   private int prevChosen = -1;
   private int prevChosenStreak;
   private float prevLateralDrift;
   private float prevPressed = 0.0F / 0.0F;

   public void reset() {
      this.clearState();
      this.clientTick = -2147483648;
   }

   public Input init(Input input, float cameraYaw, float silentYaw, int clientTick) {
      return this.preview(input, cameraYaw, silentYaw, clientTick, null);
   }

   public Input preview(Input input, float cameraYaw, float silentYaw, int clientTick, MovementInputFixer.FootingModel footing) {
      MovementInputFixer copy = new MovementInputFixer();
      copy.currentBand = this.currentBand;
      copy.chosen = this.chosen;
      copy.chosenStreak = this.chosenStreak;
      copy.lateralDrift = this.lateralDrift;
      copy.pressed = this.pressed;
      copy.clientTick = this.clientTick;
      copy.prevBand = this.prevBand;
      copy.prevChosen = this.prevChosen;
      copy.prevChosenStreak = this.prevChosenStreak;
      copy.prevLateralDrift = this.prevLateralDrift;
      copy.prevPressed = this.prevPressed;
      return copy.fix(input, cameraYaw, silentYaw, clientTick, footing);
   }

   public Input fix(Input input, float cameraYaw, float silentYaw, int clientTick) {
      return this.fix(input, cameraYaw, silentYaw, clientTick, null);
   }

   public Input fix(Input input, float cameraYaw, float silentYaw, int clientTick, MovementInputFixer.FootingModel footing) {
      if (input == null) {
         return null;
      }

      if (clientTick == this.clientTick) {
         this.currentBand = this.prevBand;
         this.chosen = this.prevChosen;
         this.chosenStreak = this.prevChosenStreak;
         this.lateralDrift = this.prevLateralDrift;
         this.pressed = this.prevPressed;
      } else {
         if (this.clientTick == -2147483648 || clientTick != this.clientTick + 1) {
            this.clearState();
         }

         this.prevBand = this.currentBand;
         this.prevChosen = this.chosen;
         this.prevChosenStreak = this.chosenStreak;
         this.prevLateralDrift = this.lateralDrift;
         this.prevPressed = this.pressed;
         this.clientTick = clientTick;
      }

      float forward = inputAxis(input.forward(), input.backward());
      float sideways = inputAxis(input.left(), input.right());
      if (forward == 0.0F && sideways == 0.0F) {
         this.clearState();
         return input;
      }

      float pressed = (float)Math.toDegrees(Math.atan2(sideways, forward));
      if (Float.isNaN(this.pressed) || pressed != this.pressed) {
         this.lateralDrift = 0.0F;
      }

      this.pressed = pressed;
      float wanted = (float)Mth.wrapDegrees(pressed - ((double)cameraYaw - silentYaw));
      int next = nextBand(this.currentBand, Math.abs(wanted));
      if (next != this.currentBand) {
         this.currentBand = next;
         this.lateralDrift = 0.0F;
      }

      int[] keys = this.currentBand > 0 ? FORWARD_KEYS : (this.currentBand < 0 ? BACKWARD_KEYS : (wanted >= 0.0F ? POSITIVE_SIDE_KEYS : NEGATIVE_SIDE_KEYS));
      int chosen = this.keepChosenKey(keys, wanted) ? this.chosen : this.bestKey(keys, wanted);
      if (footing != null) {
         chosen = stabilizeChoice(chosen, wanted, pressed, cameraYaw, silentYaw, footing);
      }

      this.chosenStreak = chosen == this.chosen ? this.chosenStreak + 1 : 1;
      this.chosen = chosen;
      this.lateralDrift = Mth.clamp(this.lateralDrift + lateralError(wanted, chosen), -0.75F, 0.75F);
      return inputForKey(chosen, input);
   }

   static float keyYaw(float silentYaw, int k) {
      return silentYaw - k * 45.0F;
   }

   private static int stabilizeChoice(int chosen, float wanted, float pressed, float cameraYaw, float silentYaw, MovementInputFixer.FootingModel footing) {
      int chosenTicks = footing.ticks(keyYaw(silentYaw, chosen));
      if (chosenTicks >= 3) {
         return chosen;
      }

      if (footing.ticks(cameraYaw - pressed) < 3) {
         return chosen;
      }

      int best = chosen;
      int bestTicks = chosenTicks;
      float bestOffset = Math.abs(angleError(wanted, chosen));

      for (int k = 0; k < 8; k++) {
         if (k != chosen) {
            float offset = Math.abs(angleError(wanted, k));
            if (!(offset > 90.001F)) {
               int ticks = footing.ticks(keyYaw(silentYaw, k));
               boolean forward = k == 7 || k == 0 || k == 1;
               boolean bestForward = best == 7 || best == 0 || best == 1;
               if (ticks > bestTicks
                  || ticks == bestTicks && (offset < bestOffset - 0.001F || Math.abs(offset - bestOffset) <= 0.001F && forward && !bestForward)) {
                  best = k;
                  bestTicks = ticks;
                  bestOffset = offset;
               }
            }
         }
      }

      return best;
   }

   static int nextBand(int band, float off) {
      if (band == -2147483648) {
         return off < 67.5F ? 1 : (off > 112.5F ? -1 : 0);
      } else if (band > 0) {
         return off <= 75.0F ? 1 : (off > 120.0F ? -1 : 0);
      } else if (band < 0) {
         return off >= 105.0F ? -1 : (off < 60.0F ? 1 : 0);
      } else {
         return off < 60.0F ? 1 : (off > 120.0F ? -1 : 0);
      }
   }

   private boolean keepChosenKey(int[] keys, float wanted) {
      if (this.chosen >= 0 && containsKey(keys, this.chosen)) {
         if (Math.abs(angleError(wanted, this.chosen)) > 45.001F) {
            return false;
         }

         float band = this.chosenStreak < 2 ? 0.75F : 0.5F;
         return Math.abs(this.lateralDrift + lateralError(wanted, this.chosen)) <= band;
      } else {
         return false;
      }
   }

   private int bestKey(int[] keys, float wanted) {
      int best = -1;
      float bestError = 1.0F / 0.0F;
      float bestOffset = 1.0F / 0.0F;

      for (int k : keys) {
         float offset = Math.abs(angleError(wanted, k));
         if (!(offset > 45.001F)) {
            float after = Math.abs(this.lateralDrift + lateralError(wanted, k));
            if (after < bestError - 1.0E-6F || Math.abs(after - bestError) <= 1.0E-6F && offset < bestOffset) {
               best = k;
               bestError = after;
               bestOffset = offset;
            }
         }
      }

      if (best >= 0) {
         return best;
      }

      for (int k : keys) {
         float offset = Math.abs(angleError(wanted, k));
         if (offset < bestOffset) {
            best = k;
            bestOffset = offset;
         }
      }

      return best;
   }

   private static float angleError(float wanted, int k) {
      return Mth.wrapDegrees(wanted - k * 45.0F);
   }

   private static float lateralError(float wanted, int k) {
      return (float)Math.sin(Math.toRadians(angleError(wanted, k)));
   }

   private static Input inputForKey(int k, Input input) {
      boolean forward = k == 7 || k == 0 || k == 1;
      return new Input(forward, k >= 3 && k <= 5, k >= 1 && k <= 3, k >= 5, input.jump(), input.shift(), input.sprint() && forward);
   }

   private static boolean containsKey(int[] keys, int k) {
      for (int key : keys) {
         if (key == k) {
            return true;
         }
      }

      return false;
   }

   private static float inputAxis(boolean positive, boolean negative) {
      return positive == negative ? 0.0F : (positive ? 1.0F : -1.0F);
   }

   private void clearState() {
      this.currentBand = -2147483648;
      this.chosen = -1;
      this.chosenStreak = 0;
      this.lateralDrift = 0.0F;
      this.pressed = 0.0F / 0.0F;
   }

   public interface FootingModel {
      int ticks(float var1);
   }
}
