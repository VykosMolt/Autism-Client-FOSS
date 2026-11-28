package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.ValueRange;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;

public final class WTappingModule extends Module {
   private final WTapController init = new WTapController(new WTapController.Inner_DataUtil() {
      @Override
      public int chance() {
         return WTappingModule.this.integer("chance");
      }

      @Override
      public ValueRange delayRange() {
         return ValueRange.parse(WTappingModule.this.value("delay-ms"), WTapController.DEFAULT_DELAY_MS);
      }

      @Override
      public ValueRange releaseRange() {
         return ValueRange.parse(WTappingModule.this.value("release-ms"), WTapController.DEFAULT_RELEASE_MS);
      }

      @Override
      public boolean playersOnly() {
         return WTappingModule.this.bool("players-only");
      }

      @Override
      public boolean groundOnly() {
         return WTappingModule.this.bool("ground-only");
      }
   });

   public WTappingModule() {
      super("w-tapping", "W-tapping", ae.init_ae, "Briefly release forward after a landed sprint hit to reset sprint.");
      this.add(new IntSetting("chance", "Chance", 90, 0, 100, 1).unit("%").description("Chance per confirmed sprint hit."));
      this.add(
         new RangeSetting("delay-ms", "Reaction", 20.0, 60.0, 0.0, 200.0, 1.0)
            .unit("ms")
            .description("Milliseconds after hit confirmation before releasing forward.")
      );
      this.add(
         new RangeSetting("release-ms", "Release Time", 45.0, 85.0, 1.0, 200.0, 1.0)
            .unit("ms")
            .description("Milliseconds to release forward; uses whole native movement ticks.")
      );
      this.add(new BoolSetting("players-only", "Players Only", true));
      this.add(new BoolSetting("ground-only", "Ground Only", false).description("Only tap while standing on the ground."));
   }

   static WTapController wTapController() {
      WTapController built = KBDisplacementModule.wTapController();
      if (al.init_Module_Str("w-tapping") instanceof WTappingModule m && m.isEnabled()) {
         if (built != null) {
            built.cancelTap();
         }

         return m.init;
      } else {
         return built;
      }
   }

   public static void onAttack(Player attacker, Entity entity) {
      WTapController tapper = wTapController();
      if (tapper != null) {
         tapper.onAttack(attacker, entity);
      }
   }

   public static void onDamageEvent(ClientboundDamageEventPacket packet) {
      WTapController tapper = wTapController();
      if (tapper != null) {
         tapper.onDamageEvent(packet);
      }
   }

   public static Input filterInput(Input original) {
      WTapController tapper = wTapController();
      return tapper == null ? original : tapper.filterInput(original);
   }

   public static boolean isTapping() {
      WTapController tapper = wTapController();
      return tapper != null && tapper.isTapping();
   }

   public static boolean tapActive() {
      WTapController tapper = wTapController();
      return tapper != null && tapper.pollActive();
   }

   @Override
   public void preMovementTick() {
      this.init.tick();
   }

   @Override
   public void onEnable() {
      this.init.resetAll();
   }

   @Override
   public void onDisable() {
      this.init.resetAll();
   }

   @Override
   public void onGameJoin() {
      this.init.resetAll();
   }

   @Override
   public void onGameLeft() {
      this.init.resetAll();
   }

   @Override
   public String info() {
      return this.init.tapCount() == 0L ? "Ready" : this.init.tapCount() + " taps";
   }
}
