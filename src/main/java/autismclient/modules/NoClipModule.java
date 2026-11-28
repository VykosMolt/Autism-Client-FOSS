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
   private static NoClipModule init_av;
   private boolean init_Z;
   private Entity init_Entity;
   private volatile boolean load;

   public NoClipModule() {
      super("no-clip", "NoClip", ae.load_ae, "Move through blocks.");
      init_av = this;
      this.add(new DoubleSetting("speed", "Speed", 0.32, 0.1, 0.4, 0.01).description("Movement speed.").build());
      this.add(new BoolSetting("only-in-vehicle", "Only In Vehicle", false).description("Only while riding.").build());
      this.add(new BoolSetting("disable-on-setback", "Disable On Setback", false).description("Disable on server setback.").build());
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         if (this.load) {
            this.load = false;
            this.disableWithToggleMessage("NoClip disabled: server set you back.");
         } else {
            LocalPlayer player = MC.player;
            if (player != null) {
               if (this.load()) {
                  if (this.init_Z) {
                     this.init_V_30();
                  }
               } else {
                  this.init_Z = true;
                  player.noPhysics = true;
                  player.fallDistance = 0.0;
                  player.setOnGround(false);
                  double speed = this.decimal("speed");
                  Entity vehicle = player.getControlledVehicle();
                  this.init(vehicle);
                  if (vehicle != null) {
                     vehicle.noPhysics = true;
                     this.init_Entity = vehicle;
                     if (!al.init_Z_Str("boat-fly")) {
                        init(vehicle, player, speed);
                     }
                  } else {
                     init(player, player, speed);
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundPlayerPositionPacket && this.bool("disable-on-setback") && !this.load()) {
         this.load = true;
      }

      return false;
   }

   @Override
   public void onEnable() {
      this.init_Z = false;
      this.load = false;
   }

   @Override
   public void onDisable() {
      this.init_V_30();
   }

   @Override
   public void onGameLeft() {
      this.init_V_30();
   }

   public static boolean noClipActive() {
      NoClipModule module = init_av;
      return module != null && module.isEnabled() && !aD.read_Z() && !module.load();
   }

   private boolean load() {
      LocalPlayer player = MC.player;
      return this.bool("only-in-vehicle") && (player == null || player.getControlledVehicle() == null);
   }

   private void init_V_30() {
      this.init_Z = false;
      if (MC.player != null) {
         AuditClientFixes.clearNoPhysicsIfUnused();
         Entity vehicle = MC.player.getControlledVehicle();
         if (vehicle != null) {
            vehicle.noPhysics = false;
         }
      }

      this.init(null);
   }

   private void init(Entity keep) {
      if (this.init_Entity != null && this.init_Entity != keep) {
         this.init_Entity.noPhysics = false;
         this.init_Entity = null;
      }
   }

   private static void init(Entity target, LocalPlayer player, double speed) {
      double y = MC.options.keyJump.isDown() ? speed : (MC.options.keyShift.isDown() ? -speed : 0.0);
      target.setDeltaMovement(MovementModuleHooks.steerVelocity(player, new Vec3(0.0, y, 0.0), speed));
   }
}
