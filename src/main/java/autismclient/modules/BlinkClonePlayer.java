package autismclient.modules;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;

public final class BlinkClonePlayer extends RemotePlayer {
   private static final int init_I = -4344907;
   private final PlayerInfo init_PlayerInfo;

   public BlinkClonePlayer(ClientLevel level, LocalPlayer source) {
      super(level, source.getGameProfile());
      this.setId(-4344907);
      this.setUUID(UUID.randomUUID());
      this.getInventory().replaceWith(source.getInventory());

      try {
         this.getAttributes().assignAllValues(source.getAttributes());
      } catch (Throwable var6) {
      }

      this.setPose(source.getPose());
      PlayerInfo resolved = null;

      try {
         if (Minecraft.getInstance().getConnection() != null) {
            resolved = Minecraft.getInstance().getConnection().getPlayerInfo(source.getGameProfile().id());
         }
      } catch (Throwable var5) {
      }

      this.init_PlayerInfo = resolved;
   }

   public void init(float headYaw, float bodyYaw) {
      this.yHeadRot = headYaw;
      this.yHeadRotO = headYaw;
      this.yBodyRot = bodyYaw;
      this.yBodyRotO = bodyYaw;
   }

   @Override
   protected PlayerInfo getPlayerInfo() {
      return this.init_PlayerInfo != null ? this.init_PlayerInfo : super.getPlayerInfo();
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Override
   public void tick() {
   }

   @Override
   public boolean isPickable() {
      return false;
   }
}
