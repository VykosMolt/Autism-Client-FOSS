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
   private static final int init_I = 4;
   private static final double init_D = 9.0;
   private static final int load = 60;

   private AiFollow() {
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final int entityId = AiJson.init_I_JsonObject(args, "entityId", -1);
         String spec = AiJson.init_Str_JsonObject(args, "type", "");
         if (entityId < 0 && spec.isBlank()) {
            throw new IllegalArgumentException("entityId or type required");
         }

         final Set<EntityType<?>> types = entityId >= 0 ? Set.of() : AiCombat.init(spec);
         final double distance = Math.max(1.0, Math.min(8.0, AiJson.init_D_JsonObject(args, "distance", 2.5)));
         final Integer timeout = args.has("timeoutSeconds") ? Math.max(1, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 60))) : null;
         final long deadline = timeout == null ? 9223372036854775807L : System.nanoTime() + timeout.intValue() * 1000000000L;
         AiService service = AiService.load_cc();
         final boolean silentAim = service != null && service.init_AiSettings().aiSilentAim;
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            Entity init_Entity;
            Vec3 init_Vec3;
            AiOperation init_AiOperation;
            BlockPos init_BlockPos;
            int init_I;
            int load;
            boolean init_Z;

            {
               this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.engine.AiAim.AimProfile.CALM);
               this.init_DataUtil.init_V_Z(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (System.nanoTime() > deadline) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Follow deadline reached at " + this.init_D() + " blocks from the target");
                  }

                  if (this.init_Entity != null
                     && (!this.init_Entity.isAlive() || this.init_Entity.isRemoved() || this.init_Minecraft.level.getEntity(this.init_Entity.getId()) == null)) {
                     this.init_Entity = null;
                     this.init_V_6();
                  }

                  if (this.init_Entity == null) {
                     this.init_Entity = this.init_Entity();
                     if (this.init_Entity == null) {
                        return this.init(
                           AiOperation.Status.FAILED,
                           AiFailure.TARGET_GONE,
                           this.init_Vec3 != null
                              ? "Follow target lost; last observed at " + BlockPos.containing(this.init_Vec3).toShortString()
                              : "No matching entity observed"
                        );
                     }
                  }

                  this.init_Vec3 = this.init_Entity.position();
                  this.init_DataUtil.init_V_Vec3(autismclient.ai.engine.AiAim.load_Vec3_Entity(this.init_Entity));
                  this.init_DataUtil.load_V();
                  if (this.init_Minecraft.player.distanceTo(this.init_Entity) <= distance) {
                     this.init_V_6();
                     return timeout == null && ++this.init_I >= 60
                        ? this.init(AiOperation.Status.SUCCEEDED, "Settled within follow distance of " + this.init_Str_5())
                        : this.init("Holding follow distance of " + this.init_Str_5());
                  }

                  this.init_I = 0;
                  if (this.init_AiOperation == null || this.init_BlockPos.distSqr(this.init_Entity.blockPosition()) >= 9.0 || ++this.load > 80) {
                     this.init_V_6();
                     this.init_BlockPos = this.init_Entity.blockPosition();
                     this.load = 0;
                     this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(this.init_BlockPos, distance, false, false, false, this.init_I_7());
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init("Following " + this.init_Str_5());
                  }

                  AiOperation.Status status = r.status();
                  this.init_V_6();
                  return status != AiOperation.Status.SUCCEEDED ? this.init(status, r.message()) : this.init("Closing on " + this.init_Str_5());
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Follow cancelled");
               }
            }

            Entity init_Entity() {
               if (entityId >= 0) {
                  Entity explicit = this.init_Minecraft.level.getEntity(entityId);
                  return explicit != null && explicit.isAlive() ? explicit : null;
               }

               Entity best = null;
               double bestDistance = 1.7976931348623157E308;

               for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
                  if (entity != this.init_Minecraft.player
                     && entity.isAlive()
                     && (types.isEmpty() ? entity instanceof Enemy : types.contains(entity.getType()))) {
                     double d = entity.distanceToSqr(this.init_Minecraft.player);
                     if (d < bestDistance) {
                        bestDistance = d;
                        best = entity;
                     }
                  }
               }

               return best;
            }

            int init_I_7() {
               return timeout == null ? 4 : (int)Math.max(1L, Math.min(4L, (deadline - System.nanoTime()) / 1000000000L));
            }

            String init_Str_5() {
               return this.init_Entity == null
                  ? "target"
                  : BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).getPath() + "#" + this.init_Entity.getId();
            }

            double init_D() {
               if (this.init_Minecraft.player == null) {
                  return -1.0;
               }

               double d = this.init_Entity != null
                  ? this.init_Minecraft.player.distanceTo(this.init_Entity)
                  : (this.init_Vec3 != null ? this.init_Minecraft.player.position().distanceTo(this.init_Vec3) : -1.0);
               return Math.round(d * 10.0) / 10.0;
            }

            void init_V_6() {
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }

            JsonObject init_JsonObject_13() {
               JsonObject out = AiJson.init_JsonObject_ObjArr("distance", this.init_D(), "followDistance", distance);
               if (this.init_Entity != null) {
                  out.add(
                     "target",
                     AiJson.init_JsonObject_ObjArr(
                        "entityId", this.init_Entity.getId(), "type", BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).toString()
                     )
                  );
               }

               if (this.init_Vec3 != null) {
                  BlockPos p = BlockPos.containing(this.init_Vec3);
                  out.add("lastKnown", AiJson.init_JsonObject_ObjArr("x", p.getX(), "y", p.getY(), "z", p.getZ()));
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_13().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               JsonObject data = this.init_JsonObject_13();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, AiFailure code, String message) {
               JsonObject data = this.init_JsonObject_13();
               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            @Override
            public void cancel() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  this.init_V_6();
                  this.init_DataUtil.init_V();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Following requires a connected living player");
      }
   }
}
