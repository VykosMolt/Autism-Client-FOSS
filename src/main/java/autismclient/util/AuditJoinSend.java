package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;

public final class AuditJoinSend {
   private AuditJoinSend() {
   }

   public static Runnable create(Minecraft var0, ClientPacketListener var1) {
      return () -> var1.send(new ServerboundClientInformationPacket(var0.options.buildPlayerInformation()));
   }
}
