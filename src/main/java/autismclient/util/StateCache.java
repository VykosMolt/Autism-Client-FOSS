package autismclient.util;

import autismclient.api.module.DoubleSetting;
import net.minecraft.client.player.LocalPlayer;

public final class StateCache {
   public static final String init_Str = "rotation-speed-multiplier";
   private static final int init_I = 2;
   private final q.Inner_CachePool init_q_CachePool = new q.Inner_CachePool();
   private Object init_Obj;
   private int load;

   public static DoubleSetting init_DoubleSetting() {
      return new DoubleSetting("rotation-speed-multiplier", "Rotation Speed", 1.0, 0.5, 2.5, 0.1)
         .group("Aim")
         .description("Relative to module default")
         .unit("×");
   }

   public static double init_D(double multiplier) {
      return Math.clamp(Double.isFinite(multiplier) ? multiplier : 1.0, 0.5, 2.5);
   }

   public static float init_F(double scale) {
      return (float)Math.min(180.0, 72.0 * scale);
   }

   public void init_V() {
      this.init_Obj = null;
      this.init_q_CachePool.init();
   }

   void init(Object enemy, int tick, int ticks) {
      if (enemy != null) {
         if (this.init_Obj != enemy) {
            this.init_Obj = enemy;
            this.load = tick;
            this.init_q_CachePool.init();
         }

         if (this.load - tick < ticks) {
            this.load = tick + ticks;
         }
      }
   }

   boolean init(Object enemy, int tick) {
      return enemy != null && this.init_Obj == enemy && this.load - tick >= 0;
   }

   public void init(Object from, Object to) {
      if (from != null && this.init_Obj == from) {
         this.init_Obj = to;
      }
   }

   public void load(Object enemy, int tick, int latencyMs) {
      this.init(enemy, tick, FileCache.init_I_I(latencyMs) + 2);
   }

   public void init(String owner, int priority, LocalPlayer player, Object enemy, double speed, ce.Inner_DataUtil goal, boolean pin) {
      if (!owner.equals(aD.init_Str()) || aD.init_Z()) {
         this.init_V();
      }

      aD.init_V_Str(owner, priority, goal);
      if (owner.equals(aD.init_Str())) {
         this.init(enemy, ck.init_ck().state_I(), 2);
         if (pin) {
            this.init_q_CachePool.init();
            aD.init_V_Str_5(owner, player, true);
         } else {
            double scale = 2.0 * init_D(speed);
            aD.init_V_Str_aD_DataUtil(owner, (at, yaw, pitch) -> this.init_q_CachePool.init(at, goal, yaw, pitch, scale));
            float cap = init_F(scale);
            aD.init(owner, player, cap, cap);
         }
      }
   }

   public boolean init(String owner, int priority, LocalPlayer player, Object enemy, ce.Inner_DataUtil wire) {
      if (wire != null && this.init(enemy, ck.init_ck().state_I()) && owner.equals(aD.init_Str()) && !aD.init_Z()) {
         ce.Inner_DataUtil current = aD.init_ce_DataUtil();
         if (current == null || !aD.load_Z() || player.getYRot() == current.init() && player.getXRot() == current.load()) {
            aD.init_V_Str(owner, priority, current == null ? wire : current);
            aD.init_V_Str_5(owner, player, true);
            this.init_q_CachePool.init();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
