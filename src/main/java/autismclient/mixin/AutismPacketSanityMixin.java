package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.ComponentSafetyValidator;
import autismclient.protector.MovementBoundsGuard;
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
      if (MovementBoundsGuard.isBadMotion(packet.movement())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleExplosion", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
      if (MovementBoundsGuard.isBadPosition(packet.center())
         || MovementBoundsGuard.isBadCoord(packet.radius())
         || packet.playerKnockback().isPresent() && MovementBoundsGuard.isBadMotion(packet.playerKnockback().get())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneMovePlayer(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
      if (MovementBoundsGuard.isBadChange(packet.change()) && autism$suppressesCorrections((ClientPacketListener)(Object)this)) {
         ci.cancel();
      }
   }

   @Inject(method = "handleTeleportEntity", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneTeleportEntity(ClientboundTeleportEntityPacket packet, CallbackInfo ci) {
      if (MovementBoundsGuard.isBadChange(packet.change())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleEntityPositionSync", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneEntityPositionSync(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci) {
      if (MovementBoundsGuard.isBadChange(Compat263.entityPosSyncValues(packet))) {
         ci.cancel();
      }
   }

   @Inject(method = "handleAddEntity", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
      if (MovementBoundsGuard.isBadCoord(packet.getX()) || MovementBoundsGuard.isBadCoord(packet.getY()) || MovementBoundsGuard.isBadCoord(packet.getZ()) || MovementBoundsGuard.isBadMotion(packet.getMovement())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
      if (MovementBoundsGuard.isBadCoord(packet.x())
         || MovementBoundsGuard.isBadCoord(packet.y())
         || MovementBoundsGuard.isBadCoord(packet.z())
         || MovementBoundsGuard.isBadCoord(packet.xDist())
         || MovementBoundsGuard.isBadCoord(packet.yDist())
         || MovementBoundsGuard.isBadCoord(packet.zDist())
         || MovementBoundsGuard.isBadCoord(packet.xMaxSpeed())
         || packet.count() > 100000) {
         ci.cancel();
      }
   }

   @Inject(method = "handleMoveVehicle", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneMoveVehicle(ClientboundMoveVehiclePacket packet, CallbackInfo ci) {
      if ((
            MovementBoundsGuard.isBadPosition(Compat263.moveVehiclePos(packet))
               || MovementBoundsGuard.isBadCoord(Compat263.moveVehicleYRot(packet))
               || MovementBoundsGuard.isBadCoord(Compat263.moveVehicleXRot(packet))
         )
         && autism$suppressesCorrections((ClientPacketListener)(Object)this)) {
         ci.cancel();
      }
   }

   @Unique
   private static boolean autism$suppressesCorrections(PacketListener listener) {
      ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(listener);
      return context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED;
   }

   @Inject(method = "handleAddObjective", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneObjective(ClientboundSetObjectivePacket packet, CallbackInfo ci) {
      if (!ComponentSafetyValidator.isSafe(packet.getDisplayName()) || packet.getNumberFormat().isPresent() && !ComponentSafetyValidator.isSafeFormat(packet.getNumberFormat().get())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleSetScore", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
      if (packet.display().isPresent() && !ComponentSafetyValidator.isSafe(packet.display().get())
         || packet.numberFormat().isPresent() && !ComponentSafetyValidator.isSafeFormat(packet.numberFormat().get())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleSetPlayerTeamPacket", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$saneTeam(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
      if (!packet.getParameters().isEmpty()) {
         Parameters parameters = packet.getParameters().get();
         if (!ComponentSafetyValidator.isSafe(parameters.displayName()) || !ComponentSafetyValidator.isSafe(parameters.playerPrefix()) || !ComponentSafetyValidator.isSafe(parameters.playerSuffix())) {
            ci.cancel();
         }
      }
   }
}
