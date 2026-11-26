package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.mixin.accessor.AutismMovePlayerPacketAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class AntiHungerModule extends Module {
   private static final Minecraft init = Minecraft.getInstance();

   public AntiHungerModule() {
      super("anti-hunger", "AntiHunger", ae.write, "Prevents hunger from decreasing. Will flag anticheats.");
      this.add(new BoolSetting("no-sprint", "NoSprint", true).description("Hide sprint from server."));
      this.add(new BoolSetting("sprint-while-swimming", "While Swimming", false).description("Keep sprinting while swimming."));
      this.add(new BoolSetting("keep-floating", "KeepFloating", true).description("Spoof onGround as false."));
   }

   boolean init() {
      return this.bool("no-sprint") && (!init.player.isSwimming() || this.bool("sprint-while-swimming"));
   }

   public static boolean load() {
      return al.init_Module_Str("anti-hunger") instanceof AntiHungerModule antiHunger && antiHunger.isEnabled() && init.player != null && antiHunger.init()
         ? true
         : al.init_Module_Str("flight") instanceof f.Inner_FileCache flightModule && flightModule.isEnabled() && flightModule.init_Z_42();
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (init.player != null && init.getConnection() != null) {
         if (packet instanceof ServerboundMovePlayerPacket move) {
            if (!this.bool("keep-floating") || init.player.isPassenger() || read()) {
               return false;
            }

            if (init.player.isInWater() || init.player.isSwimming() || init.player.isUnderWater()) {
               return false;
            }

            if (move.isOnGround() && init.player.fallDistance <= 0.0) {
               ((AutismMovePlayerPacketAccessor)move).autism$setOnGround(false);
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean read() {
      return init.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor && accessor.autism$isDestroying();
   }
}
