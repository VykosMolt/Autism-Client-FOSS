package autismclient.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public final class CombatAttackDispatcher {
   private static final CombatAttackDispatcher.Inner_DataUtil init = new CombatAttackDispatcher.Inner_DataUtil();

   private CombatAttackDispatcher() {
   }

   public static boolean init(String owner, EntityHitResult hit, Runnable dispatched) {
      Objects.requireNonNull(dispatched);
      if (!ClientInteractionOverride.init(owner, hit)) {
         return false;
      }

      init.load(dispatched);
      return true;
   }

   public static void init(Player player, Entity target) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && player == mc.player && !init.init_Z(target) && ClientInteractionOverride.close_Z()) {
         init.load(target);
      }
   }

   public static void init(Entity target, Runnable attack, Runnable dispatched) {
      init.init(target, attack, dispatched);
   }

   static void init() {
      init.init(() -> {});
   }

   public static Runnable init_Runnable(Entity target) {
      return init.init_Runnable(target);
   }

   static void init_V(Entity target) {
      init.init_V(target);
   }

   static void load() {
      init.load();
   }

   static void read() {
      init.init();
   }

   static void write() {
      init.read();
   }

   static final class Inner_DataUtil {
      private final Deque<Runnable> init_Deque = new ArrayDeque<>();
      private final Deque<Runnable> load = new ArrayDeque<>();
      private Object init_Obj;
      private CombatAttackDispatcher.Inner_DataUtil.Inner_DataUtil_2 init_r_DataUtil_DataUti;

      void init(Object target, Runnable attack, Runnable dispatched) {
         CombatAttackDispatcher.Inner_DataUtil.Inner_DataUtil_2 previous = this.init_r_DataUtil_DataUti;
         this.init_r_DataUtil_DataUti = new CombatAttackDispatcher.Inner_DataUtil.Inner_DataUtil_2(target, Objects.requireNonNull(dispatched));

         try {
            attack.run();
         } finally {
            this.init_r_DataUtil_DataUti = previous;
         }
      }

      boolean init_Z(Object hit) {
         if (this.init_r_DataUtil_DataUti != null && this.init_r_DataUtil_DataUti.init_Obj == hit) {
            Runnable receipt = this.init_r_DataUtil_DataUti.init();
            if (receipt != null) {
               receipt.run();
            }

            return true;
         } else {
            return false;
         }
      }

      void init(Runnable receipt) {
         this.init_Deque.addLast(Objects.requireNonNull(receipt));
      }

      void load(Runnable receipt) {
         this.init_Deque.removeLast();
         this.init(receipt);
      }

      void init_V(Object target) {
         this.load();
         this.load.addAll(this.init_Deque);
         this.init_Deque.clear();
         this.init_Obj = target;
      }

      void load(Object hit) {
         Runnable receipt = this.init_Runnable(hit);
         if (receipt != null) {
            receipt.run();
         }
      }

      Runnable init_Runnable(Object hit) {
         if (this.init_r_DataUtil_DataUti != null && this.init_r_DataUtil_DataUti.init_Obj == hit) {
            return this.init_r_DataUtil_DataUti.init();
         } else {
            return this.init_Obj != null && this.init_Obj == hit ? this.load.pollFirst() : null;
         }
      }

      void init() {
         this.init_Deque.clear();
      }

      void load() {
         this.init_Obj = null;
         this.load.clear();
      }

      void read() {
         this.init();
         this.load();
      }

      private static final class Inner_DataUtil_2 {
         final Object init_Obj;
         Runnable init_Runnable;

         Inner_DataUtil_2(Object target, Runnable receipt) {
            this.init_Obj = target;
            this.init_Runnable = receipt;
         }

         Runnable init() {
            Runnable result = this.init_Runnable;
            this.init_Runnable = null;
            return result;
         }
      }
   }
}
