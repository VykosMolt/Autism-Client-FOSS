package autismclient.util.macro;

import net.minecraft.nbt.CompoundTag;

public interface E {
   String init = "packetOrder";

   D init_D();

   default boolean h_() {
      return this.init_D() == D.GRIM;
   }

   static void init_V_CompoundTag_D(CompoundTag tag, D order) {
      tag.putString("packetOrder", (order == null ? D.INSTANT : order).name());
   }

   static D init_D_CompoundTag(CompoundTag tag) {
      return tag != null && tag.contains("packetOrder") ? D.init(tag.getStringOr("packetOrder", "INSTANT"), D.INSTANT) : D.INSTANT;
   }
}
