package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.IntSetting;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismWaypoints;

public final class WaypointsModule extends Module {
   private final BoolSetting onDeath;
   private final BoolSetting trackDeaths;
   private final IntSetting deathMemory;
   private final ColorSetting deathColor;
   private final ColorSetting defaultColor;
   private final IntSetting dotSize;
   private boolean alive = true;

   public WaypointsModule() {
      super("waypoints", "Waypoints", ae.update, "Save and render named destinations.");
      this.onDeath = this.add(
         new BoolSetting("on-death", "Waypoint On Death", true).description("Save a waypoint where you die.").group("Waypoints").build()
      );
      this.trackDeaths = this.add(
         new BoolSetting("track-deaths", "Track Deaths", true).description("Save deaths even when off.").group("Waypoints").build()
      );
      this.deathMemory = this.add(
         new IntSetting("death-memory", "Death Memory", 10, 1, 50, 1).description("Max deaths to keep.").group("Waypoints").build()
      );
      this.deathColor = this.add(
         new ColorSetting("death-color", "Death Color", -50373).description("Color for death waypoints.").group("Waypoints").build()
      );
      this.defaultColor = this.add(
         new ColorSetting("default-color", "Default Color", -12855297).description("Color for new waypoints.").group("Waypoints").build()
      );
      this.dotSize = this.add(new IntSetting("dot-size", "Dot Size", 3, 1, 10, 1).description("Waypoint dot size.").group("Waypoints").build());
      this.add(new BoolSetting("show-distance", "Show Distance", true).description("Show your distance below waypoints.").group("Waypoints").build());
      this.add(
         new ActionSetting("manage-waypoints", "Manage Waypoints", this::openScreen)
            .availableOffline()
            .buttonLabel("Open")
            .description("Open the waypoint manager.")
            .group("Waypoints")
            .build()
      );
   }

   public int defaultColor() {
      return this.defaultColor.get();
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public void onGameJoin() {
      if (MC != null) {
         AutismWaypoints.getInstance().init(AuditWaypointScope.current(MC), this.deathMemory.get());
      }
   }

   @Override
   public void tick() {
      if (MC != null && MC.player != null) {
         boolean alive = MC.player.getHealth() > 0.0F;
         if (this.alive && !alive) {
            this.recordDeath();
         }

         this.alive = alive;
      } else {
         this.alive = true;
      }
   }

   private void recordDeath() {
      boolean enabled = this.isEnabled();
      if (enabled ? this.onDeath.get() : this.trackDeaths.get()) {
         int x = (int)Math.floor(MC.player.getX());
         int y = (int)Math.floor(MC.player.getY());
         int z = (int)Math.floor(MC.player.getZ());
         AutismWaypoints.Waypoint added = AutismWaypoints.getInstance()
            .init(AuditWaypointScope.current(MC), x, y, z, this.deathColor.get(), System.currentTimeMillis(), this.deathMemory.get());
         if (added != null && enabled) {
            AutismClientMessaging.sendPrefixed("§aDeath waypoint saved: §f" + added.name() + " §7(" + x + " " + y + " " + z + ")");
         }
      }
   }

   private void openScreen() {
      if (MC != null && MC.gui != null) {
         MC.gui.setScreen(new autismclient.gui.screens.WaypointsScreen(MC.gui.screen()));
      }
   }
}
