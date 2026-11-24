package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.ValueRange;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;

public final class bg extends Module {
   private final bf init = new bf(new bf.Inner_DataUtil() {
      @Override
      public int init_I() {
         return bg.this.integer("chance");
      }

      @Override
      public ValueRange init_ValueRange() {
         return ValueRange.parse(bg.this.value("delay-ms"), bf.init_ValueRange);
      }

      @Override
      public ValueRange load_ValueRange() {
         return ValueRange.parse(bg.this.value("release-ms"), bf.load_ValueRange);
      }

      @Override
      public boolean init_Z() {
         return bg.this.bool("players-only");
      }

      @Override
      public boolean load_Z() {
         return bg.this.bool("ground-only");
      }
   });

   public bg() {
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

   static bf init_bf_2() {
      bf built = X.init_bf();
      if (al.init_Module_Str("w-tapping") instanceof bg m && m.isEnabled()) {
         if (built != null) {
            built.load_V();
         }

         return m.init;
      } else {
         return built;
      }
   }

   public static void init(Player attacker, Entity entity) {
      bf tapper = init_bf_2();
      if (tapper != null) {
         tapper.init(attacker, entity);
      }
   }

   public static void init(ClientboundDamageEventPacket packet) {
      bf tapper = init_bf_2();
      if (tapper != null) {
         tapper.init_V_ClientboundDamageE(packet);
      }
   }

   public static Input init(Input original) {
      bf tapper = init_bf_2();
      return tapper == null ? original : tapper.init_Input_Input(original);
   }

   public static boolean init_Z_36() {
      bf tapper = init_bf_2();
      return tapper != null && tapper.init_Z();
   }

   public static boolean load() {
      bf tapper = init_bf_2();
      return tapper != null && tapper.load_Z();
   }

   @Override
   public void preMovementTick() {
      this.init.init_V();
   }

   @Override
   public void onEnable() {
      this.init.read_V();
   }

   @Override
   public void onDisable() {
      this.init.read_V();
   }

   @Override
   public void onGameJoin() {
      this.init.read_V();
   }

   @Override
   public void onGameLeft() {
      this.init.read_V();
   }

   @Override
   public String info() {
      return this.init.init_J() == 0L ? "Ready" : this.init.init_J() + " taps";
   }
}
