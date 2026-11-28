package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property.Value;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiRaycast {
   private AiRaycast() {
   }

   static JsonObject raycast(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         double maxDistance = Math.max(1.0, Math.min(128.0, AiJson.getDouble(args, "maxDistance", 32.0)));
         boolean fluids = AiJson.getBoolean(args, "fluids", false);
         Vec3 eye = mc.player.getEyePosition();
         Vec3 direction;
         if (args.has("x") && args.has("y") && args.has("z")) {
            direction = new Vec3(
               AiJson.getDouble(args, "x", 0.0) - eye.x, AiJson.getDouble(args, "y", 0.0) - eye.y, AiJson.getDouble(args, "z", 0.0) - eye.z
            );
            if (direction.lengthSqr() < 1.0E-8) {
               throw new IllegalArgumentException("Target point is at the eye position");
            }

            direction = direction.normalize();
         } else if (!args.has("yaw") && !args.has("pitch")) {
            direction = mc.player.getLookAngle();
         } else {
            direction = Vec3.directionFromRotation(
               (float)AiJson.getDouble(args, "pitch", mc.player.getXRot()), (float)AiJson.getDouble(args, "yaw", mc.player.getYRot())
            );
         }

         Vec3 end = eye.add(direction.scale(maxDistance));
         BlockHitResult block = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, fluids ? Fluid.ANY : Fluid.NONE, mc.player));
         double blockDistanceSqr = block.getType() == Type.BLOCK ? eye.distanceToSqr(block.getLocation()) : 1.7976931348623157E308;
         Entity entityHit = null;
         Vec3 entityHitPos = null;
         double entityDistanceSqr = blockDistanceSqr;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != mc.player && !entity.isRemoved()) {
               Optional<Vec3> hit = entity.getBoundingBox().inflate(entity.getPickRadius()).clip(eye, end);
               if (!hit.isEmpty()) {
                  double distanceSqr = eye.distanceToSqr(hit.get());
                  if (distanceSqr < entityDistanceSqr) {
                     entityDistanceSqr = distanceSqr;
                     entityHit = entity;
                     entityHitPos = hit.get();
                  }
               }
            }
         }

         if (entityHit != null) {
            return init(
               entityHitPos.x,
               entityHitPos.y,
               entityHitPos.z,
               Math.sqrt(entityDistanceSqr),
               entityHit.getId(),
               BuiltInRegistries.ENTITY_TYPE.getKey(entityHit.getType()).toString()
            );
         } else if (block.getType() == Type.BLOCK) {
            BlockPos pos = block.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            return init(
               block.getLocation().x,
               block.getLocation().y,
               block.getLocation().z,
               Math.sqrt(blockDistanceSqr),
               BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),
               init(state),
               mc.level.getBrightness(LightLayer.SKY, pos),
               mc.level.getBrightness(LightLayer.BLOCK, pos)
            );
         } else {
            return init(end.x, end.y, end.z, maxDistance);
         }
      } else {
         throw new IllegalArgumentException("Raycast requires a connected living player");
      }
   }

   static JsonObject init(double x, double y, double z, double distance, String blockId, Map<String, String> properties, int skyLight, int blockLight) {
      JsonObject out = init("block", x, y, z, distance);
      JsonObject block = new JsonObject();
      block.addProperty("id", blockId);
      JsonObject props = new JsonObject();
      properties.forEach(props::addProperty);
      block.add("properties", props);
      block.addProperty("skyLight", skyLight);
      block.addProperty("blockLight", blockLight);
      out.add("block", block);
      return out;
   }

   static JsonObject init(double x, double y, double z, double distance, int id, String typeId) {
      JsonObject out = init("entity", x, y, z, distance);
      out.add("entity", AiJson.objectOf("id", id, "type", typeId));
      return out;
   }

   static JsonObject init(double x, double y, double z, double distance) {
      return init("miss", x, y, z, distance);
   }

   private static JsonObject init(String hit, double x, double y, double z, double distance) {
      JsonObject out = new JsonObject();
      out.addProperty("hit", hit);
      out.add("pos", AiJson.objectOf("x", init(x), "y", init(y), "z", init(z)));
      out.addProperty("distance", init(distance));
      return out;
   }

   private static Map<String, String> init(BlockState state) {
      Map<String, String> out = new LinkedHashMap<>();
      state.getValues().forEach(raw -> {
         Value<? extends Comparable<?>> value = (Value<? extends Comparable<?>>)raw;
         out.put(value.property().getName(), String.valueOf(value.value()));
      });
      return out;
   }

   private static double init(double value) {
      return Math.round(value * 100.0) / 100.0;
   }
}
