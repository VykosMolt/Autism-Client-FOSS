package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.util.AuditClientFixes;
import autismclient.util.AutismLiteVariant;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class NoClipModule extends Module {
   private static NoClipModule INSTANCE;
   private boolean init_Z;
   private Entity noPhysicsVehicle;
   private volatile boolean setbackSeen;

   public NoClipModule() {
      super("no-clip", "NoClip", ae.load_ae, "Move through blocks.");
      INSTANCE = this;
      this.add(new DoubleSetting("speed", "Speed", 0.32, 0.1, 0.4, 0.01).description("Movement speed.").build());
      this.add(new BoolSetting("only-in-vehicle", "Only In Vehicle", false).description("Only while riding.").build());
      this.add(new BoolSetting("disable-on-setback", "Disable On Setback", false).description("Disable on server setback.").build());
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         if (this.setbackSeen) {
            this.setbackSeen = false;
            this.disableWithToggleMessage("NoClip disabled: server set you back.");
         } else {
            LocalPlayer player = MC.player;
            if (player != null) {
               if (this.vehicleOnlyWithoutVehicle()) {
                  if (this.init_Z) {
                     this.restorePhysics();
                  }
               } else {
                  this.init_Z = true;
                  player.noPhysics = true;
                  player.fallDistance = 0.0;
                  player.setOnGround(false);
                  double speed = this.decimal("speed");
                  Entity vehicle = player.getControlledVehicle();
                  this.releaseVehicleExcept(vehicle);
                  if (vehicle != null) {
                     vehicle.noPhysics = true;
                     this.noPhysicsVehicle = vehicle;
                     if (!al.init_Z_Str("boat-fly")) {
                        applyFlight(vehicle, player, speed);
                     }
                  } else {
                     applyFlight(player, player, speed);
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundPlayerPositionPacket && this.bool("disable-on-setback") && !this.vehicleOnlyWithoutVehicle()) {
         this.setbackSeen = true;
      }

      return false;
   }

   @Override
   public void onEnable() {
      this.init_Z = false;
      this.setbackSeen = false;
   }

   @Override
   public void onDisable() {
      this.restorePhysics();
   }

   @Override
   public void onGameLeft() {
      this.restorePhysics();
   }

   public static boolean noClipActive() {
      NoClipModule module = INSTANCE;
      return module != null && module.isEnabled() && !aD.read_Z() && !module.vehicleOnlyWithoutVehicle();
   }

   private boolean vehicleOnlyWithoutVehicle() {
      LocalPlayer player = MC.player;
      return this.bool("only-in-vehicle") && (player == null || player.getControlledVehicle() == null);
   }

   private void restorePhysics() {
      this.init_Z = false;
      if (MC.player != null) {
         AuditClientFixes.clearNoPhysicsIfUnused();
         Entity vehicle = MC.player.getControlledVehicle();
         if (vehicle != null) {
            vehicle.noPhysics = false;
         }
      }

      this.releaseVehicleExcept(null);
   }

   private void releaseVehicleExcept(Entity keep) {
      if (this.noPhysicsVehicle != null && this.noPhysicsVehicle != keep) {
         this.noPhysicsVehicle.noPhysics = false;
         this.noPhysicsVehicle = null;
      }
   }

   private static void applyFlight(Entity target, LocalPlayer player, double speed) {
      double y = MC.options.keyJump.isDown() ? speed : (MC.options.keyShift.isDown() ? -speed : 0.0);
      target.setDeltaMovement(MovementModuleHooks.steerVelocity(player, new Vec3(0.0, y, 0.0), speed));
   }
}
