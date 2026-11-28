package autismclient.util;

import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class SprintStateTracker {
   private static volatile boolean localSprinting;
   private static volatile boolean predictedServerSprinting;
   private static volatile boolean previousSprinting;
   private static volatile long lastChangeNanos = -9223372036854775808L;
   private static volatile boolean serverSprinting;
   private static final ArrayDeque<int[]> pendingSprintFlags = new ArrayDeque<>();
   private static volatile boolean attackLocalSprinting;
   private static volatile boolean attackServerSprinting;
   private static volatile boolean attackStartSprinting;
   private static volatile boolean attackResetSprint;
   private static volatile float attackStrength;
   private static volatile int sprintResetCount;

   private SprintStateTracker() {
   }

   public static void update(boolean sprinting) {
      localSprinting = sprinting;
      serverSprinting = sprinting;
      if (sprinting != previousSprinting) {
         predictedServerSprinting = sprinting;
         lastChangeNanos = System.nanoTime();
      }

      previousSprinting = sprinting;
   }

   public static void load(boolean sprinting) {
      long roundTrip = (Math.max(0, autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs()) + 100L) * 1000000L;
      if (System.nanoTime() - lastChangeNanos > roundTrip) {
         predictedServerSprinting = sprinting;
      }

      synchronized (pendingSprintFlags) {
         if (pendingSprintFlags.size() >= 32) {
            pendingSprintFlags.removeFirst();
         }

         pendingSprintFlags.addLast(new int[]{sprinting ? 1 : 0, -2147483648});
      }
   }

   public static void init(int id) {
      synchronized (pendingSprintFlags) {
         for (int[] flag : pendingSprintFlags) {
            if (flag[1] == -2147483648) {
               flag[1] = id;
            }
         }
      }
   }

   public static void load(int id) {
      synchronized (pendingSprintFlags) {
         Iterator<int[]> it = pendingSprintFlags.iterator();

         while (it.hasNext()) {
            int[] flag = it.next();
            if (flag[1] == id) {
               serverSprinting = flag[0] == 1;
               it.remove();
            }
         }
      }
   }

   public static boolean isServerSprinting() {
      return serverSprinting;
   }

   public static boolean predictedServerSprinting() {
      return predictedServerSprinting;
   }

   public static boolean effectiveSprinting(boolean clientSprinting) {
      return clientSprinting != previousSprinting ? clientSprinting : predictedServerSprinting;
   }

   public static boolean isDesynced() {
      return serverSprinting != localSprinting;
   }

   public static void onAttackStart(Player attacker) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && attacker == mc.player) {
         attackLocalSprinting = localSprinting;
         attackServerSprinting = serverSprinting;
         attackStartSprinting = attacker.isSprinting();
         attackStrength = attacker.getAttackStrengthScale(0.5F);
         attackResetSprint = false;
      }
   }

   public static void load(Player attacker) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && attacker == mc.player) {
         attackResetSprint = attackStartSprinting && !attacker.isSprinting();
         if (attackResetSprint) {
            sprintResetCount++;
         }
      }
   }

   public static boolean wasLocalSprintingOnAttack() {
      return attackLocalSprinting;
   }

   public static boolean wasServerSprintingOnAttack() {
      return attackServerSprinting;
   }

   public static boolean attackStartSprinting() {
      return attackStartSprinting;
   }

   public static float lastAttackStrength() {
      return attackStrength;
   }

   public static boolean didAttackResetSprint() {
      return attackResetSprint;
   }

   public static int sprintResets() {
      return sprintResetCount;
   }
}
