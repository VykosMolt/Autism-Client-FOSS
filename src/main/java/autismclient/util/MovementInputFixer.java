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
   private static final int[] init_IArr = new int[]{7, 0, 1};
   private static final int[] load_IArr = new int[]{3, 4, 5};
   private static final int[] read_IArr = new int[]{2};
   private static final int[] write_IArr = new int[]{6};
   private int write_I = -2147483648;
   private int chosen = -1;
   private int sync_I;
   private float flush_F;
   private float pressed = 0.0F / 0.0F;
   private int clientTick = -2147483648;
   private int close_I = -2147483648;
   private int open_I = -1;
   private int poll_I;
   private float open_F;
   private float poll_F = 0.0F / 0.0F;

   public void reset() {
      this.load();
      this.clientTick = -2147483648;
   }

   public Input init(Input input, float cameraYaw, float silentYaw, int clientTick) {
      return this.init(input, cameraYaw, silentYaw, clientTick, null);
   }

   public Input init(Input input, float cameraYaw, float silentYaw, int clientTick, MovementInputFixer.FootingModel footing) {
      MovementInputFixer copy = new MovementInputFixer();
      copy.write_I = this.write_I;
      copy.chosen = this.chosen;
      copy.sync_I = this.sync_I;
      copy.flush_F = this.flush_F;
      copy.pressed = this.pressed;
      copy.clientTick = this.clientTick;
      copy.close_I = this.close_I;
      copy.open_I = this.open_I;
      copy.poll_I = this.poll_I;
      copy.open_F = this.open_F;
      copy.poll_F = this.poll_F;
      return copy.load(input, cameraYaw, silentYaw, clientTick, footing);
   }

   public Input fix(Input input, float cameraYaw, float silentYaw, int clientTick) {
      return this.load(input, cameraYaw, silentYaw, clientTick, null);
   }

   public Input load(Input input, float cameraYaw, float silentYaw, int clientTick, MovementInputFixer.FootingModel footing) {
      if (input == null) {
         return null;
      }

      if (clientTick == this.clientTick) {
         this.write_I = this.close_I;
         this.chosen = this.open_I;
         this.sync_I = this.poll_I;
         this.flush_F = this.open_F;
         this.pressed = this.poll_F;
      } else {
         if (this.clientTick == -2147483648 || clientTick != this.clientTick + 1) {
            this.load();
         }

         this.close_I = this.write_I;
         this.open_I = this.chosen;
         this.poll_I = this.sync_I;
         this.open_F = this.flush_F;
         this.poll_F = this.pressed;
         this.clientTick = clientTick;
      }

      float forward = inputAxis(input.forward(), input.backward());
      float sideways = inputAxis(input.left(), input.right());
      if (forward == 0.0F && sideways == 0.0F) {
         this.load();
         return input;
      }

      float pressed = (float)Math.toDegrees(Math.atan2(sideways, forward));
      if (Float.isNaN(this.pressed) || pressed != this.pressed) {
         this.flush_F = 0.0F;
      }

      this.pressed = pressed;
      float wanted = (float)Mth.wrapDegrees(pressed - ((double)cameraYaw - silentYaw));
      int next = init_I_I_F(this.write_I, Math.abs(wanted));
      if (next != this.write_I) {
         this.write_I = next;
         this.flush_F = 0.0F;
      }

      int[] keys = this.write_I > 0 ? init_IArr : (this.write_I < 0 ? load_IArr : (wanted >= 0.0F ? read_IArr : write_IArr));
      int chosen = this.init_Z_IArr_F(keys, wanted) ? this.chosen : this.init_I_IArr_F(keys, wanted);
      if (footing != null) {
         chosen = init(chosen, wanted, pressed, cameraYaw, silentYaw, footing);
      }

      this.sync_I = chosen == this.chosen ? this.sync_I + 1 : 1;
      this.chosen = chosen;
      this.flush_F = Mth.clamp(this.flush_F + read(wanted, chosen), -0.75F, 0.75F);
      return init_Input_I_Input(chosen, input);
   }

   static float init_F_F_I(float silentYaw, int k) {
      return silentYaw - k * 45.0F;
   }

   private static int init(int chosen, float wanted, float pressed, float cameraYaw, float silentYaw, MovementInputFixer.FootingModel footing) {
      int chosenTicks = footing.ticks(init_F_F_I(silentYaw, chosen));
      if (chosenTicks >= 3) {
         return chosen;
      }

      if (footing.ticks(cameraYaw - pressed) < 3) {
         return chosen;
      }

      int best = chosen;
      int bestTicks = chosenTicks;
      float bestOffset = Math.abs(load(wanted, chosen));

      for (int k = 0; k < 8; k++) {
         if (k != chosen) {
            float offset = Math.abs(load(wanted, k));
            if (!(offset > 90.001F)) {
               int ticks = footing.ticks(init_F_F_I(silentYaw, k));
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

   static int init_I_I_F(int band, float off) {
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

   private boolean init_Z_IArr_F(int[] keys, float wanted) {
      if (this.chosen >= 0 && init_Z_IArr_I(keys, this.chosen)) {
         if (Math.abs(load(wanted, this.chosen)) > 45.001F) {
            return false;
         }

         float band = this.sync_I < 2 ? 0.75F : 0.5F;
         return Math.abs(this.flush_F + read(wanted, this.chosen)) <= band;
      } else {
         return false;
      }
   }

   private int init_I_IArr_F(int[] keys, float wanted) {
      int best = -1;
      float bestError = 1.0F / 0.0F;
      float bestOffset = 1.0F / 0.0F;

      for (int k : keys) {
         float offset = Math.abs(load(wanted, k));
         if (!(offset > 45.001F)) {
            float after = Math.abs(this.flush_F + read(wanted, k));
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
         float offset = Math.abs(load(wanted, k));
         if (offset < bestOffset) {
            best = k;
            bestOffset = offset;
         }
      }

      return best;
   }

   private static float load(float wanted, int k) {
      return Mth.wrapDegrees(wanted - k * 45.0F);
   }

   private static float read(float wanted, int k) {
      return (float)Math.sin(Math.toRadians(load(wanted, k)));
   }

   private static Input init_Input_I_Input(int k, Input input) {
      boolean forward = k == 7 || k == 0 || k == 1;
      return new Input(forward, k >= 3 && k <= 5, k >= 1 && k <= 3, k >= 5, input.jump(), input.shift(), input.sprint() && forward);
   }

   private static boolean init_Z_IArr_I(int[] keys, int k) {
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

   private void load() {
      this.write_I = -2147483648;
      this.chosen = -1;
      this.sync_I = 0;
      this.flush_F = 0.0F;
      this.pressed = 0.0F / 0.0F;
   }

   public interface FootingModel {
      int ticks(float var1);
   }
}
