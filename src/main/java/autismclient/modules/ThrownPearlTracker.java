package autismclient.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

final class ThrownPearlTracker {
   private Object trackedLevel;
   private Object trackedPlayer;
   private long lastGameTime = -9223372036854775808L;
   private ThrownPearlTracker.Inner_DataUtil remembered;
   private double scanMillis;
   private final List<ThrownEnderpearl> ownPearls = new ArrayList<>();
   private final Map<UUID, ThrownPearlTracker.Inner_DataUtil> forecasts = new HashMap<>();
   private final Set<UUID> liveIds = new HashSet<>();
   private static final int init_I = 32;

   ThrownPearlTracker.Inner_DataUtil remembered() {
      return this.remembered;
   }

   double init_D() {
      return this.scanMillis;
   }

   void reset() {
      this.trackedLevel = this.trackedPlayer = null;
      this.lastGameTime = -9223372036854775808L;
      this.remembered = null;
      this.scanMillis = 0.0;
      this.ownPearls.clear();
      this.forecasts.clear();
      this.liveIds.clear();
   }

   void tick(Minecraft mc, boolean enabled) {
      if (enabled && mc != null && mc.level != null && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()) {
         if (this.trackedLevel != mc.level || this.trackedPlayer != mc.player) {
            this.reset();
         }

         this.trackedLevel = mc.level;
         this.trackedPlayer = mc.player;
         long now = mc.level.getGameTime();
         if (now != this.lastGameTime) {
            long started = System.nanoTime();
            this.lastGameTime = now;
            this.ownPearls.clear();
            this.liveIds.clear();

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof ThrownEnderpearl pearl && !pearl.isRemoved() && pearl.isAlive() && pearl.getOwner() == mc.player) {
                  this.ownPearls.add(pearl);
                  this.liveIds.add(pearl.getUUID());
               }
            }

            this.forecasts.keySet().retainAll(this.liveIds);
            this.ownPearls.sort(Comparator.comparingInt(Entity::getId).reversed());

            for (int i = 0; i < Math.min(32, this.ownPearls.size()); i++) {
               ThrownEnderpearl pearl = this.ownPearls.get(i);
               IncomingProjectileScanner.ProjectilePath flight = IncomingProjectileScanner.traceProjectile(mc.level, pearl, IncomingProjectileScanner.ProjectileKind.PEARL);
               if (flight.load() != null) {
                  this.forecasts.put(pearl.getUUID(), new ThrownPearlTracker.Inner_DataUtil(pearl.getUUID(), pearl.getId(), flight.load(), now + flight.init_D()));
               }
            }

            ThrownPearlTracker.Inner_DataUtil latest = null;

            for (ThrownPearlTracker.Inner_DataUtil forecast : this.forecasts.values()) {
               latest = laterForecast(latest, forecast);
            }

            this.remembered = latest;
            this.scanMillis = (System.nanoTime() - started) / 1000000.0;
         }
      } else {
         this.reset();
      }
   }

   static ThrownPearlTracker.Inner_DataUtil laterForecast(ThrownPearlTracker.Inner_DataUtil a, ThrownPearlTracker.Inner_DataUtil b) {
      if (b != null
         && b.position() != null
         && Double.isFinite(b.landingTime())
         && Double.isFinite(b.position().x)
         && Double.isFinite(b.position().y)
         && Double.isFinite(b.position().z)) {
         return a == null || b.landingTime() > a.landingTime() || b.landingTime() == a.landingTime() && b.init_I() > a.init_I() ? b : a;
      } else {
         return a;
      }
   }

   record Inner_DataUtil(UUID pearlId, int init_I, Vec3 position, double landingTime) {
   }
}
