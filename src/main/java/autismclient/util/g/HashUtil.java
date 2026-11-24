package autismclient.util.g;

import autismclient.compat263.CompatSwingPacket;
import autismclient.util.bw;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;

public final class HashUtil {
   private static final Set<Class<?>> init = Set.of(
      ServerboundChatCommandPacket.class,
      ServerboundClientCommandPacket.class,
      ServerboundCustomPayloadPacket.class,
      Pos.class,
      PosRot.class,
      Rot.class,
      StatusOnly.class,
      ServerboundPlayerInputPacket.class,
      ServerboundSetCarriedItemPacket.class,
      CompatSwingPacket.class,
      ServerboundPunchPacket.class
   );

   private HashUtil() {
   }

   public static boolean init(Class<? extends Packet<?>> packetClass) {
      return packetClass != null && init.contains(packetClass);
   }

   public static Set<Class<? extends Packet<?>>> init() {
      Set<Class<? extends Packet<?>>> out = new LinkedHashSet<>();

      for (Class<? extends Packet<?>> packetClass : bw.load()) {
         if (!init(packetClass)) {
            out.add(packetClass);
         }
      }

      return out;
   }
}
