package autismclient.util.macro;

import net.minecraft.nbt.CompoundTag;

public interface PacketOrderAware {
   String init = "packetOrder";

   PacketOrderMode getPacketOrder();

   default boolean h_() {
      return this.getPacketOrder() == PacketOrderMode.GRIM;
   }

   static void writePacketOrder(CompoundTag tag, PacketOrderMode order) {
      tag.putString("packetOrder", (order == null ? PacketOrderMode.INSTANT : order).name());
   }

   static PacketOrderMode readPacketOrder(CompoundTag tag) {
      return tag != null && tag.contains("packetOrder") ? PacketOrderMode.init(tag.getStringOr("packetOrder", "INSTANT"), PacketOrderMode.INSTANT) : PacketOrderMode.INSTANT;
   }
}
