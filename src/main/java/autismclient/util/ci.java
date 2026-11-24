package autismclient.util;

import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ci {
   private static volatile boolean init_Z;
   private static volatile boolean load;
   private static volatile boolean read;
   private static volatile long init_J = -9223372036854775808L;
   private static volatile boolean write;
   private static final ArrayDeque<int[]> init_ArrayDeque = new ArrayDeque<>();
   private static volatile boolean update;
   private static volatile boolean sync;
   private static volatile boolean flush;
   private static volatile boolean close;
   private static volatile float init_F;
   private static volatile int init_I;

   private ci() {
   }

   public static void init_V(boolean sprinting) {
      init_Z = sprinting;
      write = sprinting;
      if (sprinting != read) {
         load = sprinting;
         init_J = System.nanoTime();
      }

      read = sprinting;
   }

   public static void load(boolean sprinting) {
      long roundTrip = (Math.max(0, autismclient.util.macro.ab.init_I()) + 100L) * 1000000L;
      if (System.nanoTime() - init_J > roundTrip) {
         load = sprinting;
      }

      synchronized (init_ArrayDeque) {
         if (init_ArrayDeque.size() >= 32) {
            init_ArrayDeque.removeFirst();
         }

         init_ArrayDeque.addLast(new int[]{sprinting ? 1 : 0, -2147483648});
      }
   }

   public static void init(int id) {
      synchronized (init_ArrayDeque) {
         for (int[] flag : init_ArrayDeque) {
            if (flag[1] == -2147483648) {
               flag[1] = id;
            }
         }
      }
   }

   public static void load(int id) {
      synchronized (init_ArrayDeque) {
         Iterator<int[]> it = init_ArrayDeque.iterator();

         while (it.hasNext()) {
            int[] flag = it.next();
            if (flag[1] == id) {
               write = flag[0] == 1;
               it.remove();
            }
         }
      }
   }

   public static boolean init_Z() {
      return write;
   }

   public static boolean load() {
      return load;
   }

   public static boolean init_Z(boolean clientSprinting) {
      return clientSprinting != read ? clientSprinting : load;
   }

   public static boolean read() {
      return write != init_Z;
   }

   public static void init(Player attacker) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && attacker == mc.player) {
         update = init_Z;
         sync = write;
         flush = attacker.isSprinting();
         init_F = attacker.getAttackStrengthScale(0.5F);
         close = false;
      }
   }

   public static void load(Player attacker) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && attacker == mc.player) {
         close = flush && !attacker.isSprinting();
         if (close) {
            init_I++;
         }
      }
   }

   public static boolean write() {
      return update;
   }

   public static boolean update() {
      return sync;
   }

   public static boolean sync() {
      return flush;
   }

   public static float init_F() {
      return init_F;
   }

   public static boolean flush() {
      return close;
   }

   public static int init_I() {
      return init_I;
   }
}
