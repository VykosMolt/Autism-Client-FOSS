package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.g.BufferHelper;
import autismclient.g.CachePool;
import autismclient.g.SyncManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Parameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class AutismPacketSanityMixin {
   @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneSetEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
      if (SyncManager.load(packet.movement())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleExplosion", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
      if (SyncManager.init(packet.center())
         || SyncManager.init(packet.radius())
         || packet.playerKnockback().isPresent() && SyncManager.load(packet.playerKnockback().get())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneMovePlayer(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
      if (SyncManager.init(packet.change()) && autism$suppressesCorrections((ClientPacketListener)(Object)this)) {
         ci.cancel();
      }
   }

   @Inject(method = "handleTeleportEntity", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneTeleportEntity(ClientboundTeleportEntityPacket packet, CallbackInfo ci) {
      if (SyncManager.init(packet.change())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleEntityPositionSync", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneEntityPositionSync(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci) {
      if (SyncManager.init(Compat263.entityPosSyncValues(packet))) {
         ci.cancel();
      }
   }

   @Inject(method = "handleAddEntity", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
      if (SyncManager.init(packet.getX()) || SyncManager.init(packet.getY()) || SyncManager.init(packet.getZ()) || SyncManager.load(packet.getMovement())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
      if (SyncManager.init(packet.x())
         || SyncManager.init(packet.y())
         || SyncManager.init(packet.z())
         || SyncManager.init(packet.xDist())
         || SyncManager.init(packet.yDist())
         || SyncManager.init(packet.zDist())
         || SyncManager.init(packet.xMaxSpeed())
         || packet.count() > 100000) {
         ci.cancel();
      }
   }

   @Inject(method = "handleMoveVehicle", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneMoveVehicle(ClientboundMoveVehiclePacket packet, CallbackInfo ci) {
      if ((
            SyncManager.init(Compat263.moveVehiclePos(packet))
               || SyncManager.init(Compat263.moveVehicleYRot(packet))
               || SyncManager.init(Compat263.moveVehicleXRot(packet))
         )
         && autism$suppressesCorrections((ClientPacketListener)(Object)this)) {
         ci.cancel();
      }
   }

   @Unique
   private static boolean autism$suppressesCorrections(PacketListener listener) {
      BufferHelper.Inner_StreamUtil context = BufferHelper.init(listener);
      return context != null && context.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.load;
   }

   @Inject(method = "handleAddObjective", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneObjective(ClientboundSetObjectivePacket packet, CallbackInfo ci) {
      if (!CachePool.init(packet.getDisplayName()) || packet.getNumberFormat().isPresent() && !CachePool.init(packet.getNumberFormat().get())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleSetScore", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
      if (packet.display().isPresent() && !CachePool.init(packet.display().get())
         || packet.numberFormat().isPresent() && !CachePool.init(packet.numberFormat().get())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleSetPlayerTeamPacket", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneTeam(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
      if (!packet.getParameters().isEmpty()) {
         Parameters parameters = packet.getParameters().get();
         if (!CachePool.init(parameters.displayName()) || !CachePool.init(parameters.playerPrefix()) || !CachePool.init(parameters.playerSuffix())) {
            ci.cancel();
         }
      }
   }
}
