package autismclient.util;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class RecentCombatTracker {
   private static final long init_J = 5000000000L;
   private static final int init_I = 256;
   private static final Map<UUID, Long> lastHitNanos = new LinkedHashMap<>();
   private static WeakReference<Object> trackedLevel = new WeakReference<>(null);
   private static long lastCombatNanos = -9223372036854775808L;

   private RecentCombatTracker() {
   }

   public static boolean isFreshTarget(Entity target) {
      return isFreshTarget(target, System.nanoTime());
   }

   public static boolean isFreshTarget(Entity target, long now) {
      resetOnLevelChange();
      if (target == null) {
         return true;
      } else {
         Long last = lastHitNanos.get(target.getUUID());
         if (last == null) {
            return true;
         } else if (now - last >= 5000000000L) {
            lastHitNanos.remove(target.getUUID());
            return true;
         } else {
            return false;
         }
      }
   }

   public static void onAttack(Player attacker, Entity target) {
      Minecraft mc = Minecraft.getInstance();
      if (attacker != null && attacker == mc.player && !attacker.isSpectator() && target instanceof LivingEntity) {
         resetOnLevelChange();
         long now = System.nanoTime();
         lastHitNanos.entrySet().removeIf(entry -> now - entry.getValue() >= 5000000000L);
         lastHitNanos.remove(target.getUUID());
         if (lastHitNanos.size() >= 256) {
            lastHitNanos.remove(lastHitNanos.keySet().iterator().next());
         }

         lastHitNanos.put(target.getUUID(), now);
         lastCombatNanos = now;
      }
   }

   public static void markCombat() {
      lastCombatNanos = System.nanoTime();
   }

   public static boolean inCombatWithin(long nanos) {
      return lastCombatNanos != -9223372036854775808L && System.nanoTime() - lastCombatNanos <= nanos;
   }

   private static void resetOnLevelChange() {
      Object current = Minecraft.getInstance().level;
      if (trackedLevel.get() != current) {
         lastHitNanos.clear();
         trackedLevel = new WeakReference<>(current);
      }
   }
}
