package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

final class AiFollow {
   private static final int MAX_LEG_SECONDS = 4;
   private static final double RETARGET_DISTANCE_SQ = 9.0;
   private static final int load = 60;

   private AiFollow() {
   }

   static AiOperation create(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final int entityId = AiJson.getInt(args, "entityId", -1);
         String spec = AiJson.getString(args, "type", "");
         if (entityId < 0 && spec.isBlank()) {
            throw new IllegalArgumentException("entityId or type required");
         }

         final Set<EntityType<?>> types = entityId >= 0 ? Set.of() : AiCombat.init(spec);
         final double distance = Math.max(1.0, Math.min(8.0, AiJson.getDouble(args, "distance", 2.5)));
         final Integer timeout = args.has("timeoutSeconds") ? Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 60))) : null;
         final long deadline = timeout == null ? 9223372036854775807L : System.nanoTime() + timeout.intValue() * 1000000000L;
         AiService service = AiService.getExistingInstance();
         final boolean silentAim = service != null && service.getSettings().aiSilentAim;
         return new AiOperation() {
            final Minecraft mc = Minecraft.getInstance();
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            Entity followed;
            Vec3 lastSeen;
            AiOperation leg;
            BlockPos legGoal;
            int settledTicks;
            int legTicks;
            boolean cancelled;

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.CALM);
               this.aim.setSilent(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && this.mc.player != null && this.mc.level != null) {
                  if (System.nanoTime() > deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Follow deadline reached at " + this.currentDistance() + " blocks from the target");
                  }

                  if (this.followed != null
                     && (!this.followed.isAlive() || this.followed.isRemoved() || this.mc.level.getEntity(this.followed.getId()) == null)) {
                     this.followed = null;
                     this.stopLeg();
                  }

                  if (this.followed == null) {
                     this.followed = this.findTarget();
                     if (this.followed == null) {
                        return this.fail(
                           AiOperation.Status.FAILED,
                           AiFailure.TARGET_GONE,
                           this.lastSeen != null
                              ? "Follow target lost; last observed at " + BlockPos.containing(this.lastSeen).toShortString()
                              : "No matching entity observed"
                        );
                     }
                  }

                  this.lastSeen = this.followed.position();
                  this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.followed));
                  this.aim.tick();
                  if (this.mc.player.distanceTo(this.followed) <= distance) {
                     this.stopLeg();
                     return timeout == null && ++this.settledTicks >= 60
                        ? this.finish(AiOperation.Status.SUCCEEDED, "Settled within follow distance of " + this.targetLabel())
                        : this.running("Holding follow distance of " + this.targetLabel());
                  }

                  this.settledTicks = 0;
                  if (this.leg == null || this.legGoal.distSqr(this.followed.blockPosition()) >= 9.0 || ++this.legTicks > 80) {
                     this.stopLeg();
                     this.legGoal = this.followed.blockPosition();
                     this.legTicks = 0;
                     this.leg = AiGoto.travelToCompat(this.legGoal, distance, false, false, false, this.legSeconds());
                  }

                  AiOperation.Result r = this.leg.tick();
                  if (!r.terminal()) {
                     return this.running("Following " + this.targetLabel());
                  }

                  AiOperation.Status status = r.status();
                  this.stopLeg();
                  return status != AiOperation.Status.SUCCEEDED ? this.finish(status, r.message()) : this.running("Closing on " + this.targetLabel());
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Follow cancelled");
               }
            }

            Entity findTarget() {
               if (entityId >= 0) {
                  Entity explicit = this.mc.level.getEntity(entityId);
                  return explicit != null && explicit.isAlive() ? explicit : null;
               }

               Entity best = null;
               double bestDistance = 1.7976931348623157E308;

               for (Entity entity : this.mc.level.entitiesForRendering()) {
                  if (entity != this.mc.player
                     && entity.isAlive()
                     && (types.isEmpty() ? entity instanceof Enemy : types.contains(entity.getType()))) {
                     double d = entity.distanceToSqr(this.mc.player);
                     if (d < bestDistance) {
                        bestDistance = d;
                        best = entity;
                     }
                  }
               }

               return best;
            }

            int legSeconds() {
               return timeout == null ? 4 : (int)Math.max(1L, Math.min(4L, (deadline - System.nanoTime()) / 1000000000L));
            }

            String targetLabel() {
               return this.followed == null
                  ? "target"
                  : BuiltInRegistries.ENTITY_TYPE.getKey(this.followed.getType()).getPath() + "#" + this.followed.getId();
            }

            double currentDistance() {
               if (this.mc.player == null) {
                  return -1.0;
               }

               double d = this.followed != null
                  ? this.mc.player.distanceTo(this.followed)
                  : (this.lastSeen != null ? this.mc.player.position().distanceTo(this.lastSeen) : -1.0);
               return Math.round(d * 10.0) / 10.0;
            }

            void stopLeg() {
               if (this.leg != null) {
                  this.leg.cancel();
                  this.leg = null;
               }
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf("distance", this.currentDistance(), "followDistance", distance);
               if (this.followed != null) {
                  out.add(
                     "target",
                     AiJson.objectOf(
                        "entityId", this.followed.getId(), "type", BuiltInRegistries.ENTITY_TYPE.getKey(this.followed.getType()).toString()
                     )
                  );
               }

               if (this.lastSeen != null) {
                  BlockPos p = BlockPos.containing(this.lastSeen);
                  out.add("lastKnown", AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ()));
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               JsonObject data = this.progressJson();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            AiOperation.Result fail(AiOperation.Status status, AiFailure code, String message) {
               JsonObject data = this.progressJson();
               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.stopLeg();
                  this.aim.clear();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Following requires a connected living player");
      }
   }
}
