package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;

public class DirectPacketSender {
   public static void send(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.getConnection() != null) {
            autismclient.protector.ProtectorFeatureFlags.markDirect(packet);
            mc.getConnection().send(packet);
         }
      }
   }

   public static void load(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.getConnection() != null) {
            autismclient.protector.ProtectorFeatureFlags.markDirect(packet);
            mc.getConnection().send(packet);
         }
      }
   }
}
