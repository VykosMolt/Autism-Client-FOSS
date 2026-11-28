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
   private static final Map<UUID, Long> init_Map = new LinkedHashMap<>();
   private static WeakReference<Object> init_WeakReference = new WeakReference<>(null);
   private static long load = -9223372036854775808L;

   private RecentCombatTracker() {
   }

   public static boolean isFreshTarget(Entity target) {
      return isFreshTarget(target, System.nanoTime());
   }

   public static boolean isFreshTarget(Entity target, long now) {
      load();
      if (target == null) {
         return true;
      } else {
         Long last = init_Map.get(target.getUUID());
         if (last == null) {
            return true;
         } else if (now - last >= 5000000000L) {
            init_Map.remove(target.getUUID());
            return true;
         } else {
            return false;
         }
      }
   }

   public static void onAttack(Player attacker, Entity target) {
      Minecraft mc = Minecraft.getInstance();
      if (attacker != null && attacker == mc.player && !attacker.isSpectator() && target instanceof LivingEntity) {
         load();
         long now = System.nanoTime();
         init_Map.entrySet().removeIf(entry -> now - entry.getValue() >= 5000000000L);
         init_Map.remove(target.getUUID());
         if (init_Map.size() >= 256) {
            init_Map.remove(init_Map.keySet().iterator().next());
         }

         init_Map.put(target.getUUID(), now);
         load = now;
      }
   }

   public static void markCombat() {
      load = System.nanoTime();
   }

   public static boolean inCombatWithin(long nanos) {
      return load != -9223372036854775808L && System.nanoTime() - load <= nanos;
   }

   private static void load() {
      Object current = Minecraft.getInstance().level;
      if (init_WeakReference.get() != current) {
         init_Map.clear();
         init_WeakReference = new WeakReference<>(current);
      }
   }
}
