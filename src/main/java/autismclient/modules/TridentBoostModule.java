package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.DoubleSetting;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class TridentBoostModule extends Module {
   private final Map<Player, Long> init = new IdentityHashMap<>();

   public TridentBoostModule() {
      super("trident-boost", "TridentBoost", ae.load_ae, "Use a Riptide trident outside water or rain.");
      this.add(new BoolSetting("allow-dry", "Allow Dry Boost", true).build());
      this.add(new DoubleSetting("cooldown", "Dry Boost Cooldown", 10.0, 0.0, 20.0, 0.1).unit("s").visibleWhen(() -> this.bool("allow-dry")).build());
   }

   public static boolean init_Z(Player player) {
      TridentBoostModule module = init_aZ(player);
      if (module != null && !player.isPassenger()) {
         synchronized (module.init) {
            Long last = module.init.get(player);
            return last == null || System.nanoTime() - last >= module.decimal("cooldown") * 1.0E9;
         }
      } else {
         return false;
      }
   }

   public static void init_V(Player player) {
      TridentBoostModule module = init_aZ(player);
      if (module != null) {
         synchronized (module.init) {
            module.init.put(player, System.nanoTime());
         }
      }
   }

   private static TridentBoostModule init_aZ(Player player) {
      LocalPlayer local = MC.player;
      if (local != null && !aD.read_Z()) {
         if (player == local
            || player instanceof ServerPlayer serverPlayer
               && serverPlayer.level().getServer() == MC.getSingleplayerServer()
               && serverPlayer.getUUID().equals(local.getUUID())) {
            return al.init_Module_Str("trident-boost") instanceof TridentBoostModule module && module.isEnabled() && module.bool("allow-dry") ? module : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public void onDisable() {
      this.init();
   }

   @Override
   public void onGameLeft() {
      this.init();
   }

   private void init() {
      synchronized (this.init) {
         this.init.clear();
      }
   }
}
