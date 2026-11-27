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
   private Object init_Obj;
   private Object load;
   private long init_J = -9223372036854775808L;
   private ThrownPearlTracker.Inner_DataUtil remembered;
   private double init_D;
   private final List<ThrownEnderpearl> init_List = new ArrayList<>();
   private final Map<UUID, ThrownPearlTracker.Inner_DataUtil> init_Map = new HashMap<>();
   private final Set<UUID> init_Set = new HashSet<>();
   private static final int init_I = 32;

   ThrownPearlTracker.Inner_DataUtil remembered() {
      return this.remembered;
   }

   double init_D() {
      return this.init_D;
   }

   void reset() {
      this.init_Obj = this.load = null;
      this.init_J = -9223372036854775808L;
      this.remembered = null;
      this.init_D = 0.0;
      this.init_List.clear();
      this.init_Map.clear();
      this.init_Set.clear();
   }

   void tick(Minecraft mc, boolean enabled) {
      if (enabled && mc != null && mc.level != null && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()) {
         if (this.init_Obj != mc.level || this.load != mc.player) {
            this.reset();
         }

         this.init_Obj = mc.level;
         this.load = mc.player;
         long now = mc.level.getGameTime();
         if (now != this.init_J) {
            long started = System.nanoTime();
            this.init_J = now;
            this.init_List.clear();
            this.init_Set.clear();

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof ThrownEnderpearl pearl && !pearl.isRemoved() && pearl.isAlive() && pearl.getOwner() == mc.player) {
                  this.init_List.add(pearl);
                  this.init_Set.add(pearl.getUUID());
               }
            }

            this.init_Map.keySet().retainAll(this.init_Set);
            this.init_List.sort(Comparator.comparingInt(Entity::getId).reversed());

            for (int i = 0; i < Math.min(32, this.init_List.size()); i++) {
               ThrownEnderpearl pearl = this.init_List.get(i);
               IncomingProjectileScanner.Inner_DataUtil flight = IncomingProjectileScanner.init(mc.level, pearl, IncomingProjectileScanner.Inner_StreamUtil.PEARL);
               if (flight.load() != null) {
                  this.init_Map.put(pearl.getUUID(), new ThrownPearlTracker.Inner_DataUtil(pearl.getUUID(), pearl.getId(), flight.load(), now + flight.init_D()));
               }
            }

            ThrownPearlTracker.Inner_DataUtil latest = null;

            for (ThrownPearlTracker.Inner_DataUtil forecast : this.init_Map.values()) {
               latest = init_aV_DataUtil_aV_DataUtil_aV_DataUtil(latest, forecast);
            }

            this.remembered = latest;
            this.init_D = (System.nanoTime() - started) / 1000000.0;
         }
      } else {
         this.reset();
      }
   }

   static ThrownPearlTracker.Inner_DataUtil init_aV_DataUtil_aV_DataUtil_aV_DataUtil(ThrownPearlTracker.Inner_DataUtil a, ThrownPearlTracker.Inner_DataUtil b) {
      if (b != null
         && b.position() != null
         && Double.isFinite(b.init_D())
         && Double.isFinite(b.position().x)
         && Double.isFinite(b.position().y)
         && Double.isFinite(b.position().z)) {
         return a == null || b.init_D() > a.init_D() || b.init_D() == a.init_D() && b.init_I() > a.init_I() ? b : a;
      } else {
         return a;
      }
   }

   record Inner_DataUtil(UUID init_UUID, int init_I, Vec3 position, double init_D) {
   }
}
