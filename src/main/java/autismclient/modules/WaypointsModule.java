package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.IntSetting;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismWaypoints;

public final class WaypointsModule extends Module {
   private final BoolSetting init_BoolSetting;
   private final BoolSetting load_BoolSetting;
   private final IntSetting init_IntSetting;
   private final ColorSetting init_ColorSetting;
   private final ColorSetting load_ColorSetting;
   private final IntSetting load_IntSetting;
   private boolean init_Z = true;

   public WaypointsModule() {
      super("waypoints", "Waypoints", ae.update, "Save and render named destinations.");
      this.init_BoolSetting = this.add(
         new BoolSetting("on-death", "Waypoint On Death", true).description("Save a waypoint where you die.").group("Waypoints").build()
      );
      this.load_BoolSetting = this.add(
         new BoolSetting("track-deaths", "Track Deaths", true).description("Save deaths even when off.").group("Waypoints").build()
      );
      this.init_IntSetting = this.add(
         new IntSetting("death-memory", "Death Memory", 10, 1, 50, 1).description("Max deaths to keep.").group("Waypoints").build()
      );
      this.init_ColorSetting = this.add(
         new ColorSetting("death-color", "Death Color", -50373).description("Color for death waypoints.").group("Waypoints").build()
      );
      this.load_ColorSetting = this.add(
         new ColorSetting("default-color", "Default Color", -12855297).description("Color for new waypoints.").group("Waypoints").build()
      );
      this.load_IntSetting = this.add(new IntSetting("dot-size", "Dot Size", 3, 1, 10, 1).description("Waypoint dot size.").group("Waypoints").build());
      this.add(new BoolSetting("show-distance", "Show Distance", true).description("Show your distance below waypoints.").group("Waypoints").build());
      this.add(
         new ActionSetting("manage-waypoints", "Manage Waypoints", this::load)
            .availableOffline()
            .buttonLabel("Open")
            .description("Open the waypoint manager.")
            .group("Waypoints")
            .build()
      );
   }

   public int init_I_23() {
      return this.load_ColorSetting.get();
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public void onGameJoin() {
      if (MC != null) {
         AutismWaypoints.init_AutismWaypoints().init(AuditWaypointScope.current(MC), this.init_IntSetting.get());
      }
   }

   @Override
   public void tick() {
      if (MC != null && MC.player != null) {
         boolean alive = MC.player.getHealth() > 0.0F;
         if (this.init_Z && !alive) {
            this.init_V_33();
         }

         this.init_Z = alive;
      } else {
         this.init_Z = true;
      }
   }

   private void init_V_33() {
      boolean enabled = this.isEnabled();
      if (enabled ? this.init_BoolSetting.get() : this.load_BoolSetting.get()) {
         int x = (int)Math.floor(MC.player.getX());
         int y = (int)Math.floor(MC.player.getY());
         int z = (int)Math.floor(MC.player.getZ());
         AutismWaypoints.Waypoint added = AutismWaypoints.init_AutismWaypoints()
            .init(AuditWaypointScope.current(MC), x, y, z, this.init_ColorSetting.get(), System.currentTimeMillis(), this.init_IntSetting.get());
         if (added != null && enabled) {
            AutismClientMessaging.sendPrefixed("§aDeath waypoint saved: §f" + added.name() + " §7(" + x + " " + y + " " + z + ")");
         }
      }
   }

   private void load() {
      if (MC != null && MC.gui != null) {
         MC.gui.setScreen(new autismclient.gui.d.WaypointsScreen(MC.gui.screen()));
      }
   }
}
