package autismclient.util.clicker;

import autismclient.modules.aD;
import autismclient.util.KeyMappingControl;
import autismclient.util.SprintStateTracker;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public final class KeyClickOutput implements ClickPatternEngine.ClickSink {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final List<KeyClickOutput> OUTPUTS = new ArrayList<>();
   private static long generation;
   private final boolean useKey;
   private boolean pressed;
   private boolean ownsKey;
   private long releaseAtNanos = -9223372036854775808L;
   private static final long REPRESS_GUARD_NANOS = 50000000L;
   private long pressNanos = -9223372036854775808L;
   private long lastFireNanos = -9223372036854775808L;
   private long activeGeneration = -9223372036854775808L;
   private int pendingClicks;
   private int consumedClicks;
   private boolean returnedClicks;

   private KeyClickOutput(boolean useKey) {
      this.useKey = useKey;
      synchronized (OUTPUTS) {
         OUTPUTS.add(this);
      }
   }

   public static KeyClickOutput attack() {
      return new KeyClickOutput(false);
   }

   public static KeyClickOutput use() {
      return new KeyClickOutput(true);
   }

   public static boolean isAnyPressed(boolean useKey) {
      synchronized (OUTPUTS) {
         for (KeyClickOutput output : OUTPUTS) {
            if (output.useKey == useKey && output.pressed) {
               return true;
            }
         }

         return false;
      }
   }

   public void activate() {
      this.activeGeneration = generation;
   }

   public void setOwnsKey(boolean owns) {
      this.ownsKey = owns;
   }

   public boolean isPressed() {
      return this.pressed;
   }

   public boolean isPhysicallyDown() {
      KeyMapping mapping = this.mapping();
      return mapping != null && !mapping.isUnbound() && KeyMappingControl.of(mapping).autism$isActuallyDown();
   }

   @Override
   public boolean init_Z_J(long now) {
      if (!this.pressed && canClick()) {
         KeyMapping mapping = this.mapping();
         if (mapping != null && !mapping.isUnbound()) {
            KeyMappingControl.of(mapping).autism$simulatePress(true);
            this.lastFireNanos = System.nanoTime();
            this.pressed = true;
            if (!this.useKey) {
               this.pendingClicks++;
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public long earliestClickNanos(long now) {
      return Math.max(now, this.lastFireNanos);
   }

   @Override
   public void init_V_J(long now) {
      if (this.pressed) {
         this.pressed = false;
         this.releaseAtNanos = -9223372036854775808L;
         KeyMapping mapping = this.mapping();
         if (mapping != null) {
            KeyMappingControl bridge = KeyMappingControl.of(mapping);
            if (!canClick()) {
               bridge.autism$resetPressedState();
            } else if (!this.ownsKey && bridge.autism$isActuallyDown()) {
               bridge.autism$resetPressedState();
            } else {
               bridge.autism$simulatePress(false);
            }
         }
      }
   }

   @Override
   public void onClick(long now) {
      this.pressed = true;
      this.releaseAtNanos = -9223372036854775808L;
   }

   public boolean press(long now, long holdNanos) {
      if (this.pressNanos != -9223372036854775808L && now - this.pressNanos < 50000000L) {
         return false;
      }

      if (!this.init_Z_J(now)) {
         return false;
      }

      this.pressNanos = this.earliestClickNanos(now);
      this.releaseAtNanos = this.pressNanos + Math.max(1000000L, holdNanos);
      return true;
   }

   public void releaseIfDue(long now) {
      if (this.pressed && this.releaseAtNanos != -9223372036854775808L && now >= this.releaseAtNanos) {
         this.init_V_J(now);
      }
   }

   public void release() {
      this.ownsKey = false;
      if (this.pressed) {
         this.init_V_J(System.nanoTime());
      }
   }

   public static void flushAll() {
      synchronized (OUTPUTS) {
         for (KeyClickOutput output : OUTPUTS) {
            output.flush();
         }
      }
   }

   public static void closeAll() {
      synchronized (OUTPUTS) {
         for (KeyClickOutput output : OUTPUTS) {
            output.close();
         }
      }
   }

   private void flush() {
      this.consumedClicks = 0;
      KeyMapping mapping = this.mapping();
      if (this.useKey || this.pendingClicks <= 0 || mapping == null || MINECRAFT.player == null) {
         this.returnedClicks = false;
      } else if (this.returnedClicks) {
         this.returnedClicks = false;
         if (!(MINECRAFT.hitResult instanceof EntityHitResult)) {
            while (this.pendingClicks > 0 && mapping.consumeClick()) {
               this.pendingClicks--;
            }

            this.pendingClicks = 0;
         }
      } else if (MINECRAFT.hitResult instanceof EntityHitResult hit
         && hit.getEntity() instanceof Player
         && SprintStateTracker.isDesynced()
         && !(MINECRAFT.player.getAttackStrengthScale(0.5F) <= 0.9F)) {
         while (this.consumedClicks < this.pendingClicks && mapping.consumeClick()) {
            this.consumedClicks++;
         }
      }
   }

   private void close() {
      KeyMapping mapping = this.mapping();
      if (mapping != null) {
         for (int i = 0; i < this.consumedClicks; i++) {
            KeyMappingControl.of(mapping).autism$countClick();
         }
      }

      this.pendingClicks = this.consumedClicks;
      this.returnedClicks = this.consumedClicks > 0;
      this.consumedClicks = 0;
   }

   public static void nextGeneration() {
      generation++;
   }

   public static void releaseStale() {
      synchronized (OUTPUTS) {
         for (KeyClickOutput output : OUTPUTS) {
            if (output.pressed && (!canClick() || output.activeGeneration != generation)) {
               output.release();
            }
         }
      }
   }

   private KeyMapping mapping() {
      if (MINECRAFT != null && MINECRAFT.options != null) {
         return this.useKey ? MINECRAFT.options.keyUse : MINECRAFT.options.keyAttack;
      } else {
         return null;
      }
   }

   private static boolean canClick() {
      return MINECRAFT != null
         && MINECRAFT.player != null
         && MINECRAFT.level != null
         && MINECRAFT.options != null
         && MINECRAFT.getWindow() != null
         && MINECRAFT.gui.screen() == null
         && MINECRAFT.gui.overlay() == null
         && !aD.init_Z();
   }
}
