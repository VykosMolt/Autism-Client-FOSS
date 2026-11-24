package autismclient.compat263;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.world.InteractionHand;

public final class CompatSwingPacket implements Packet<ServerGamePacketListener> {
   private final InteractionHand hand;

   public CompatSwingPacket(InteractionHand var1) {
      this.hand = var1 != null ? var1 : InteractionHand.MAIN_HAND;
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   @Override
   @SuppressWarnings("unchecked")
   public PacketType<CompatSwingPacket> type() {
      return (PacketType)GamePacketTypes.SERVERBOUND_PUNCH;
   }

   public void handle(ServerGamePacketListener var1) {
      if (var1 != null) {
         var1.handlePunch(ServerboundPunchPacket.INSTANCE);
      }
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof ServerboundPunchPacket || o instanceof CompatSwingPacket other && this.hand == other.hand;
   }

   @Override
   public int hashCode() {
      return ServerboundPunchPacket.INSTANCE.hashCode();
   }
}
