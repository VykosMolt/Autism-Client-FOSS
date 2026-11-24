package autismclient.mixin;

import autismclient.util.g.PacketUtil;
import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class AutismBotPlayerInfoMixin {
   @Shadow
   @Final
   private Map<UUID, PlayerInfo> playerInfoMap;

   @Inject(method = "handleAddEntity", at = @At("HEAD"))
   private void autism$mintBotPlayerInfo(ClientboundAddEntityPacket packet, CallbackInfo ci) {
      if (Minecraft.getInstance().isSameThread()) {
         try {
            if (packet.getType() != EntityTypes.PLAYER) {
               return;
            }

            UUID uuid = packet.getUUID();
            if (uuid == null || this.playerInfoMap.containsKey(uuid)) {
               return;
            }

            GameProfile profile = PacketUtil.init_GameProfile_UUID(uuid);
            if (profile != null) {
               this.playerInfoMap.put(uuid, new PlayerInfo(profile, false));
            }
         } catch (Throwable var5) {
         }
      }
   }
}
