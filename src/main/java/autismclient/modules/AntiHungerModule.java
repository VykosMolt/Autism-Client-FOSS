package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.mixin.accessor.AutismMovePlayerPacketAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class AntiHungerModule extends Module {
   private static final Minecraft CLIENT = Minecraft.getInstance();

   public AntiHungerModule() {
      super("anti-hunger", "AntiHunger", ae.write, "Prevents hunger from decreasing. Will flag anticheats.");
      this.add(new BoolSetting("no-sprint", "NoSprint", true).description("Hide sprint from server."));
      this.add(new BoolSetting("sprint-while-swimming", "While Swimming", false).description("Keep sprinting while swimming."));
      this.add(new BoolSetting("keep-floating", "KeepFloating", true).description("Spoof onGround as false."));
   }

   boolean suppressSprint() {
      return this.bool("no-sprint") && (!CLIENT.player.isSwimming() || this.bool("sprint-while-swimming"));
   }

   public static boolean suppressesSprint() {
      return al.init_Module_Str("anti-hunger") instanceof AntiHungerModule antiHunger && antiHunger.isEnabled() && CLIENT.player != null && antiHunger.suppressSprint()
         ? true
         : al.init_Module_Str("flight") instanceof f.Inner_FileCache flightModule && flightModule.isEnabled() && flightModule.init_Z_42();
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (CLIENT.player != null && CLIENT.getConnection() != null) {
         if (packet instanceof ServerboundMovePlayerPacket move) {
            if (!this.bool("keep-floating") || CLIENT.player.isPassenger() || isDestroying()) {
               return false;
            }

            if (CLIENT.player.isInWater() || CLIENT.player.isSwimming() || CLIENT.player.isUnderWater()) {
               return false;
            }

            if (move.isOnGround() && CLIENT.player.fallDistance <= 0.0) {
               ((AutismMovePlayerPacketAccessor)move).autism$setOnGround(false);
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean isDestroying() {
      return CLIENT.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor && accessor.autism$isDestroying();
   }
}
