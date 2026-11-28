package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.world.level.GameType;

public final class RealGamemodeSender {
   private RealGamemodeSender() {
   }

   public static FakeGamemodeManager.ApplyResult init(GameType mode) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.player.connection == null) {
         return FakeGamemodeManager.ApplyResult.load("Not connected.");
      }

      if (mode == null) {
         return FakeGamemodeManager.ApplyResult.load("Unknown game mode.");
      }

      boolean clearedFake = FakeGamemodeManager.snapshot().fakeActive();
      if (clearedFake) {
         FakeGamemodeManager.clearFake();
      }

      mc.player.connection.send(new ServerboundChangeGameModePacket(mode));
      return FakeGamemodeManager.ApplyResult.init("Requested real gamemode: " + mode.getName() + (clearedFake ? " (fake cleared)" : ""));
   }
}
