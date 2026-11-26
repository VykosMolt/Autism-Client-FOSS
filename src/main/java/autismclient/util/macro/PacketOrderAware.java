package autismclient.util.macro;

import net.minecraft.nbt.CompoundTag;

public interface PacketOrderAware {
   String init = "packetOrder";

   PacketOrderMode init_D();

   default boolean h_() {
      return this.init_D() == PacketOrderMode.GRIM;
   }

   static void init_V_CompoundTag_D(CompoundTag tag, PacketOrderMode order) {
      tag.putString("packetOrder", (order == null ? PacketOrderMode.INSTANT : order).name());
   }

   static PacketOrderMode init_D_CompoundTag(CompoundTag tag) {
      return tag != null && tag.contains("packetOrder") ? PacketOrderMode.init(tag.getStringOr("packetOrder", "INSTANT"), PacketOrderMode.INSTANT) : PacketOrderMode.INSTANT;
   }
}
