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

public final class KeyClickOutput implements ClickPatternEngine.Inner_NetHelper {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final List<KeyClickOutput> OUTPUTS = new ArrayList<>();
   private static long generation;
   private final boolean useKey;
   private boolean load_Z;
   private boolean read_Z;
   private long releaseAtNanos = -9223372036854775808L;
   private static final long read_J = 50000000L;
   private long pressNanos = -9223372036854775808L;
   private long update = -9223372036854775808L;
   private long sync = -9223372036854775808L;
   private int pendingClicks;
   private int consumedClicks;
   private boolean write_Z;

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
            if (output.useKey == useKey && output.load_Z) {
               return true;
            }
         }

         return false;
      }
   }

   public void init_V() {
      this.sync = generation;
   }

   public void setOwnsKey(boolean owns) {
      this.read_Z = owns;
   }

   public boolean isPressed() {
      return this.load_Z;
   }

   public boolean isPhysicallyDown() {
      KeyMapping mapping = this.mapping();
      return mapping != null && !mapping.isUnbound() && KeyMappingControl.of(mapping).autism$isActuallyDown();
   }

   @Override
   public boolean init_Z_J(long now) {
      if (!this.load_Z && canClick()) {
         KeyMapping mapping = this.mapping();
         if (mapping != null && !mapping.isUnbound()) {
            KeyMappingControl.of(mapping).autism$simulatePress(true);
            this.update = System.nanoTime();
            this.load_Z = true;
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
      return Math.max(now, this.update);
   }

   @Override
   public void init_V_J(long now) {
      if (this.load_Z) {
         this.load_Z = false;
         this.releaseAtNanos = -9223372036854775808L;
         KeyMapping mapping = this.mapping();
         if (mapping != null) {
            KeyMappingControl bridge = KeyMappingControl.of(mapping);
            if (!canClick()) {
               bridge.autism$resetPressedState();
            } else if (!this.read_Z && bridge.autism$isActuallyDown()) {
               bridge.autism$resetPressedState();
            } else {
               bridge.autism$simulatePress(false);
            }
         }
      }
   }

   @Override
   public void onClick(long now) {
      this.load_Z = true;
      this.releaseAtNanos = -9223372036854775808L;
   }

   public boolean init(long now, long holdNanos) {
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

   public void read(long now) {
      if (this.load_Z && this.releaseAtNanos != -9223372036854775808L && now >= this.releaseAtNanos) {
         this.init_V_J(now);
      }
   }

   public void release() {
      this.read_Z = false;
      if (this.load_Z) {
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

   public static void write() {
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
         this.write_Z = false;
      } else if (this.write_Z) {
         this.write_Z = false;
         if (!(MINECRAFT.hitResult instanceof EntityHitResult)) {
            while (this.pendingClicks > 0 && mapping.consumeClick()) {
               this.pendingClicks--;
            }

            this.pendingClicks = 0;
         }
      } else if (MINECRAFT.hitResult instanceof EntityHitResult hit
         && hit.getEntity() instanceof Player
         && SprintStateTracker.read()
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
      this.write_Z = this.consumedClicks > 0;
      this.consumedClicks = 0;
   }

   public static void update() {
      generation++;
   }

   public static void sync() {
      synchronized (OUTPUTS) {
         for (KeyClickOutput output : OUTPUTS) {
            if (output.load_Z && (!canClick() || output.sync != generation)) {
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
