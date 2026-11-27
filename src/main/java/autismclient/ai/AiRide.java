package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

final class AiRide {
   private static final int init_I = 24;
   private static final int load = 80;
   private static final double init_D = 3.0;
   private static final int read = 200;

   private AiRide() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiRide.Inner_DataUtil action = init(AiJson.getString(args, "action", "mount"));
         final int entityId = AiJson.getInt(args, "entityId", -1);
         final boolean place = AiJson.getBoolean(args, "place", true);
         final int timeoutSeconds = Math.max(5, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 180)));
         boolean hasTarget = args.has("x") && args.has("z");
         final BlockPos destination = hasTarget
            ? new BlockPos(AiJson.getInt(args, "x", 0), AiJson.getInt(args, "y", 64), AiJson.getInt(args, "z", 0))
            : null;
         if (action == AiRide.Inner_DataUtil.TRAVEL && destination == null) {
            throw new IllegalArgumentException("travel needs x and z (y is optional for a boat)");
         } else {
            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
               AiOperation init_AiOperation;
               Entity init_Entity;
               int init_I;
               int load_I;
               double init_D = 1.7976931348623157E308;
               boolean init_Z;
               boolean load_Z;

               @Override
               public AiOperation.Result tick() {
                  if (!this.load_Z && mc.player != null && mc.level != null) {
                     if (System.nanoTime() > this.init_J) {
                        return this.init(AiOperation.Status.TIMED_OUT, "Ride deadline reached");
                     }

                     if (action == AiRide.Inner_DataUtil.DISMOUNT) {
                        if (mc.player.getVehicle() == null) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Not riding anything");
                        }

                        if (!this.init_Z_20()) {
                           return this.init(
                              AiOperation.Status.FAILED,
                              "No safe dismount spot within reach (lava, a drop or unloaded terrain on every side); move the vehicle over solid ground first"
                           );
                        }

                        mc.player.stopRiding();
                        return mc.player.getVehicle() == null ? this.init(AiOperation.Status.SUCCEEDED, "Dismounted") : this.init("Dismounting");
                     } else {
                        if (action == AiRide.Inner_DataUtil.TRAVEL) {
                           return this.load();
                        }

                        Entity riding = mc.player.getVehicle();
                        return riding != null
                           ? this.init(AiOperation.Status.SUCCEEDED, "Already riding " + BuiltInRegistries.ENTITY_TYPE.getKey(riding.getType()))
                           : this.init_AiOperation_Result_24();
                     }
                  } else {
                     return this.init(AiOperation.Status.CANCELLED, "Ride cancelled");
                  }
               }

               AiOperation.Result init_AiOperation_Result_24() {
                  if (this.init_Entity == null || !this.init_Entity.isAlive()) {
                     this.init_Entity = this.init_Entity_5();
                     this.init_I = 0;
                  }

                  if (this.init_Entity == null) {
                     if (place && !this.init_Z) {
                        Item carried = this.init_Item_3();
                        if (carried == null) {
                           JsonObject missing = new JsonObject();
                           missing.addProperty("minecraft:oak_boat", 1);
                           return this.init(
                              AiOperation.Status.FAILED, "No vehicle nearby and none in the inventory", AiJson.objectOf("missing", missing)
                           );
                        }

                        if (AiItems.equipHotbar(AiItems.resolve(BuiltInRegistries.ITEM.getKey(carried).toString())) < 0) {
                           return this.init(AiOperation.Status.FAILED, "Could not hold the vehicle item");
                        }

                        this.init_DataUtil.aimAt(mc.player.position().add(mc.player.getLookAngle().x * 3.0, -1.2, mc.player.getLookAngle().z * 3.0));
                        this.init_DataUtil.tick();
                        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                        Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                        this.init_Z = true;
                        return this.init("Placing a vehicle");
                     } else {
                        return this.init(
                           AiOperation.Status.FAILED,
                           "No rideable vehicle within 24 blocks" + (place ? " and placing one did not produce a vehicle" : " and place is disabled")
                        );
                     }
                  } else {
                     double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
                     if (mc.player.distanceTo(this.init_Entity) > reach * 0.9) {
                        if (this.init_AiOperation == null) {
                           this.init_AiOperation = AiGoto.travelToCompat(
                              this.init_Entity.blockPosition(), 1.8, false, false, true, Math.min(120, this.init_I_26())
                           );
                        }

                        AiOperation.Result legResult = this.init_AiOperation.tick();
                        if (!legResult.terminal()) {
                           return this.init("Walking to the vehicle");
                        } else {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                           if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                              this.init_Entity = null;
                              return this.init("No route to that vehicle; looking for another");
                           } else {
                              return this.init("Arrived at the vehicle");
                           }
                        }
                     } else {
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                        }

                        this.init_DataUtil.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.init_Entity));
                        this.init_DataUtil.tick();
                        if (++this.init_I > 80) {
                           return this.init(AiOperation.Status.FAILED, "The vehicle did not accept the player");
                        }

                        if (!autismclient.ai.engine.AiAim.isLookingAtEntity(this.init_Entity)) {
                           return this.init("Turning toward the vehicle");
                        }

                        mc.gameMode
                           .interact(
                              mc.player,
                              this.init_Entity,
                              new EntityHitResult(this.init_Entity, autismclient.ai.engine.AiAim.defaultEntityAimPoint(this.init_Entity)),
                              InteractionHand.MAIN_HAND
                           );
                        return mc.player.getVehicle() != null
                           ? this.init(AiOperation.Status.SUCCEEDED, "Mounted " + BuiltInRegistries.ENTITY_TYPE.getKey(mc.player.getVehicle().getType()))
                           : this.init("Mounting");
                     }
                  }
               }

               AiOperation.Result load() {
                  Entity riding = mc.player.getVehicle();
                  if (riding == null) {
                     return this.init(AiOperation.Status.FAILED, "Not riding anything; mount first");
                  }

                  Vec3 target = Vec3.atCenterOf(destination);
                  double distance = riding.position().distanceTo(new Vec3(target.x, riding.getY(), target.z));
                  if (distance <= 3.0) {
                     this.init_V_22();
                     return this.init(AiOperation.Status.SUCCEEDED, "Arrived within " + (int)distance + "m of the destination");
                  }

                  if (distance < this.init_D - 0.5) {
                     this.init_D = distance;
                     this.load_I = 0;
                  } else if (++this.load_I > 200) {
                     this.init_V_22();
                     return this.init(
                        AiOperation.Status.FAILED, "The vehicle stopped making progress " + (int)distance + "m out; it is probably beached or blocked"
                     );
                  }

                  this.init_DataUtil.setProfile(autismclient.ai.engine.AiAim.AimProfile.CALM);
                  this.init_DataUtil.aimAt(new Vec3(target.x, riding.getEyeY(), target.z));
                  this.init_DataUtil.tick();
                  mc.options.keyUp.setDown(true);
                  return this.init("Sailing (" + (int)distance + "m to go)");
               }

               boolean init_Z_20() {
                  BlockPos feet = mc.player.blockPosition();

                  for (BlockPos cell : List.of(feet, feet.north(), feet.south(), feet.east(), feet.west())) {
                     if (AiCombat.isSafeStep(mc, cell)) {
                        return true;
                     }
                  }

                  return false;
               }

               Entity init_Entity_5() {
                  if (entityId < 0) {
                     Entity best = null;
                     double bestDistanceSq = 576.0;

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity.isAlive() && AiRide.init(entity) && entity.getPassengers().isEmpty()) {
                           double distance = entity.distanceToSqr(mc.player);
                           if (distance < bestDistanceSq) {
                              bestDistanceSq = distance;
                              best = entity;
                           }
                        }
                     }

                     return best;
                  } else {
                     Entity explicit = mc.level.getEntity(entityId);
                     return explicit != null && explicit.isAlive() && AiRide.init(explicit) ? explicit : null;
                  }
               }

               Item init_Item_3() {
                  AiItems.ItemSpec boats = AiItems.resolve("boat");

                  for (Item item : boats.items()) {
                     if (AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(item).toString()) > 0) {
                        return item;
                     }
                  }

                  return null;
               }

               void init_V_22() {
                  if (mc.options != null) {
                     mc.options.keyUp.setDown(false);
                  }
               }

               int init_I_26() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject init_JsonObject_41() {
                  JsonObject out = AiJson.objectOf("action", action.name().toLowerCase(Locale.ROOT));
                  Entity riding = mc.player == null ? null : mc.player.getVehicle();
                  if (riding != null) {
                     out.add(
                        "riding",
                        AiJson.objectOf(
                           "entityId",
                           riding.getId(),
                           "type",
                           BuiltInRegistries.ENTITY_TYPE.getKey(riding.getType()).toString(),
                           "x",
                           riding.getBlockX(),
                           "y",
                           riding.getBlockY(),
                           "z",
                           riding.getBlockZ()
                        )
                     );
                  }

                  if (destination != null) {
                     out.addProperty("destination", destination.toShortString());
                  }

                  JsonArray nearby = new JsonArray();
                  if (mc.level != null) {
                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (nearby.size() < 8 && entity.isAlive() && AiRide.init(entity) && !(entity.distanceToSqr(mc.player) > 576.0)) {
                           nearby.add(
                              AiJson.objectOf(
                                 "entityId",
                                 entity.getId(),
                                 "type",
                                 BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(),
                                 "occupied",
                                 !entity.getPassengers().isEmpty(),
                                 "distance",
                                 Math.round(Math.sqrt(entity.distanceToSqr(mc.player)) * 10.0) / 10.0
                              )
                           );
                        }
                     }
                  }

                  out.add("vehiclesNearby", nearby);
                  return out;
               }

               AiOperation.Result init(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_41().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  return this.init(status, message, null);
               }

               AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
                  JsonObject data = this.init_JsonObject_41();
                  if (extra != null) {
                     for (Entry<String, JsonElement> entry : extra.entrySet()) {
                        data.add(entry.getKey(), entry.getValue());
                     }
                  }

                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               @Override
               public void cancel() {
                  if (!this.load_Z) {
                     this.load_Z = true;
                     this.init_V_22();
                     this.init_DataUtil.clear();
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }
                  }
               }
            };
         }
      } else {
         throw new IllegalArgumentException("Riding requires a connected living player");
      }
   }

   static boolean init(Entity entity) {
      if (!(entity instanceof AbstractBoat) && !(entity instanceof AbstractMinecart)) {
         String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
         return List.of(
               "minecraft:horse",
               "minecraft:donkey",
               "minecraft:mule",
               "minecraft:skeleton_horse",
               "minecraft:zombie_horse",
               "minecraft:camel",
               "minecraft:strider",
               "minecraft:pig",
               "minecraft:llama",
               "minecraft:trader_llama"
            )
            .contains(id);
      } else {
         return true;
      }
   }

   private static AiRide.Inner_DataUtil init(String text) {
      try {
         return AiRide.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be mount, travel or dismount");
      }
   }

   enum Inner_DataUtil {
      MOUNT,
      TRAVEL,
      DISMOUNT;
   }
}
