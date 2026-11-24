package autismclient.util.a;

import autismclient.modules.aD;
import autismclient.util.aB;
import autismclient.util.ci;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public final class StreamUtil implements DataUtil.Inner_NetHelper {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final List<StreamUtil> init_List = new ArrayList<>();
   private static long init_J;
   private final boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private long load_J = -9223372036854775808L;
   private static final long read_J = 50000000L;
   private long write_J = -9223372036854775808L;
   private long update = -9223372036854775808L;
   private long sync = -9223372036854775808L;
   private int init_I;
   private int load_I;
   private boolean write_Z;

   private StreamUtil(boolean useKey) {
      this.init_Z = useKey;
      synchronized (init_List) {
         init_List.add(this);
      }
   }

   public static StreamUtil init_StreamUtil() {
      return new StreamUtil(false);
   }

   public static StreamUtil load_StreamUtil() {
      return new StreamUtil(true);
   }

   public static boolean init_Z_Z(boolean useKey) {
      synchronized (init_List) {
         for (StreamUtil output : init_List) {
            if (output.init_Z == useKey && output.load_Z) {
               return true;
            }
         }

         return false;
      }
   }

   public void init_V() {
      this.sync = init_J;
   }

   public void init_V_Z(boolean owns) {
      this.read_Z = owns;
   }

   public boolean init_Z() {
      return this.load_Z;
   }

   public boolean load_Z() {
      KeyMapping mapping = this.init_KeyMapping();
      return mapping != null && !mapping.isUnbound() && aB.init(mapping).autism$isActuallyDown();
   }

   @Override
   public boolean init_Z_J(long now) {
      if (!this.load_Z && read_Z()) {
         KeyMapping mapping = this.init_KeyMapping();
         if (mapping != null && !mapping.isUnbound()) {
            aB.init(mapping).autism$simulatePress(true);
            this.update = System.nanoTime();
            this.load_Z = true;
            if (!this.init_Z) {
               this.init_I++;
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
   public long init_J_J(long now) {
      return Math.max(now, this.update);
   }

   @Override
   public void init_V_J(long now) {
      if (this.load_Z) {
         this.load_Z = false;
         this.load_J = -9223372036854775808L;
         KeyMapping mapping = this.init_KeyMapping();
         if (mapping != null) {
            aB bridge = aB.init(mapping);
            if (!read_Z()) {
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
   public void load_V(long now) {
      this.load_Z = true;
      this.load_J = -9223372036854775808L;
   }

   public boolean init(long now, long holdNanos) {
      if (this.write_J != -9223372036854775808L && now - this.write_J < 50000000L) {
         return false;
      }

      if (!this.init_Z_J(now)) {
         return false;
      }

      this.write_J = this.init_J_J(now);
      this.load_J = this.write_J + Math.max(1000000L, holdNanos);
      return true;
   }

   public void read(long now) {
      if (this.load_Z && this.load_J != -9223372036854775808L && now >= this.load_J) {
         this.init_V_J(now);
      }
   }

   public void load_V() {
      this.read_Z = false;
      if (this.load_Z) {
         this.init_V_J(System.nanoTime());
      }
   }

   public static void read_V() {
      synchronized (init_List) {
         for (StreamUtil output : init_List) {
            output.flush();
         }
      }
   }

   public static void write() {
      synchronized (init_List) {
         for (StreamUtil output : init_List) {
            output.close();
         }
      }
   }

   private void flush() {
      this.load_I = 0;
      KeyMapping mapping = this.init_KeyMapping();
      if (this.init_Z || this.init_I <= 0 || mapping == null || init_Minecraft.player == null) {
         this.write_Z = false;
      } else if (this.write_Z) {
         this.write_Z = false;
         if (!(init_Minecraft.hitResult instanceof EntityHitResult)) {
            while (this.init_I > 0 && mapping.consumeClick()) {
               this.init_I--;
            }

            this.init_I = 0;
         }
      } else if (init_Minecraft.hitResult instanceof EntityHitResult hit
         && hit.getEntity() instanceof Player
         && ci.read()
         && !(init_Minecraft.player.getAttackStrengthScale(0.5F) <= 0.9F)) {
         while (this.load_I < this.init_I && mapping.consumeClick()) {
            this.load_I++;
         }
      }
   }

   private void close() {
      KeyMapping mapping = this.init_KeyMapping();
      if (mapping != null) {
         for (int i = 0; i < this.load_I; i++) {
            aB.init(mapping).autism$countClick();
         }
      }

      this.init_I = this.load_I;
      this.write_Z = this.load_I > 0;
      this.load_I = 0;
   }

   public static void update() {
      init_J++;
   }

   public static void sync() {
      synchronized (init_List) {
         for (StreamUtil output : init_List) {
            if (output.load_Z && (!read_Z() || output.sync != init_J)) {
               output.load_V();
            }
         }
      }
   }

   private KeyMapping init_KeyMapping() {
      if (init_Minecraft != null && init_Minecraft.options != null) {
         return this.init_Z ? init_Minecraft.options.keyUse : init_Minecraft.options.keyAttack;
      } else {
         return null;
      }
   }

   private static boolean read_Z() {
      return init_Minecraft != null
         && init_Minecraft.player != null
         && init_Minecraft.level != null
         && init_Minecraft.options != null
         && init_Minecraft.getWindow() != null
         && init_Minecraft.gui.screen() == null
         && init_Minecraft.gui.overlay() == null
         && !aD.init_Z();
   }
}
