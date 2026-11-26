package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.world.level.GameType;

public final class RealGamemodeSender {
   private RealGamemodeSender() {
   }

   public static FakeGamemodeManager.Inner_DataUtil init(GameType mode) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.player.connection == null) {
         return FakeGamemodeManager.Inner_DataUtil.load("Not connected.");
      }

      if (mode == null) {
         return FakeGamemodeManager.Inner_DataUtil.load("Unknown game mode.");
      }

      boolean clearedFake = FakeGamemodeManager.init_W_StreamUtil().init_Z();
      if (clearedFake) {
         FakeGamemodeManager.init_W_DataUtil();
      }

      mc.player.connection.send(new ServerboundChangeGameModePacket(mode));
      return FakeGamemodeManager.Inner_DataUtil.init("Requested real gamemode: " + mode.getName() + (clearedFake ? " (fake cleared)" : ""));
   }
}
