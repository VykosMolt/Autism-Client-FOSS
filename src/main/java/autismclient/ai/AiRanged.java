package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class AiRanged {
   private static final float init_F = -60.0F;
   private static final float load_F = 45.0F;
   private static final float read_F = 2.0F;
   private static final float write = 0.15F;
   private static final int init_I = 120;
   private static final int load_I = 20;
   private static final int read_I = 60;
   private static final double init_D = 48.0;
   private static final double load_D = 0.0025000000000000005;
   private static final double read_D = 2.9;

   private AiRanged() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final String spec = AiJson.getString(args, "target", "hostiles").trim();
         final int explicitId = AiJson.getInt(args, "entityId", -1);
         final int shots = Math.max(1, Math.min(64, AiJson.getInt(args, "count", 1)));
         final double radius = Math.max(4.0, Math.min(48.0, AiJson.getDouble(args, "radius", 32.0)));
         final int timeoutSeconds = Math.max(5, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 120)));
         final Set<EntityType<?>> types = explicitId >= 0 ? Set.of() : AiCombat.init(spec);
         AiService service = AiService.getExistingInstance();
         final boolean silentAim = service != null && service.getSettings().aiSilentAim;
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final int originalSlot = mc.player.getInventory().getSelectedSlot();
            Entity target;
            autismclient.util.RotationMath.Rotation solvedArc;
            int shotsFired;
            int unsolvedTicks;
            int drawTicks;
            int kills;
            boolean drawing;
            boolean cancelled;

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.CALM);
               this.aim.setSilent(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(
                        this.shotsFired > 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                        "Shooting deadline reached after " + this.shotsFired + " shot(s)"
                     );
                  }

                  if (this.shotsFired >= shots) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Fired " + this.shotsFired + " shot(s), " + this.kills + " target(s) down");
                  }

                  if (this.target != null && this.target instanceof LivingEntity living && living.isDeadOrDying()) {
                     this.kills++;
                     this.target = null;
                     this.solvedArc = null;
                     this.releaseUse();
                     return this.running("Target down (" + this.kills + ")");
                  }

                  if (this.target == null
                     || this.target instanceof LivingEntity
                     || explicitId < 0
                     || this.target.isAlive() && !this.target.isRemoved()) {
                     if (this.target == null || this.target.isAlive() && !this.target.isRemoved()) {
                        if (this.target == null) {
                           this.target = this.findTarget();
                           this.solvedArc = null;
                           this.unsolvedTicks = 0;
                           if (this.target == null) {
                              return this.finish(
                                 this.shotsFired > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                 this.shotsFired > 0
                                    ? "No targets left after " + this.shotsFired + " shot(s)"
                                    : "No matching live target within " + (int)radius + " blocks"
                              );
                           }
                        }

                        AiOperation.Result weapon = this.ensureWeapon();
                        if (weapon != null) {
                           return weapon;
                        }

                        autismclient.util.RotationMath.Rotation arc = this.solveArc(this.target);
                        if (arc != null) {
                           this.solvedArc = arc;
                           this.unsolvedTicks = 0;
                           ItemStack held = mc.player.getMainHandItem();
                           if (held.is(Items.BOW)) {
                              return this.tickBow(arc);
                           } else {
                              return held.is(Items.CROSSBOW) ? this.tickCrossbow(arc) : this.tickThrow(arc);
                           }
                        } else if (++this.unsolvedTicks > 60) {
                           Entity dropped = this.target;
                           this.target = null;
                           this.releaseUse();
                           return this.running("No arc reaches " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath() + "; picking another");
                        } else {
                           return this.running("Solving the arc");
                        }
                     } else {
                        this.target = null;
                        this.solvedArc = null;
                        this.releaseUse();
                        return this.running("Target despawned or unloaded (not a kill); picking another");
                     }
                  } else {
                     this.kills++;
                     this.target = null;
                     this.solvedArc = null;
                     this.releaseUse();
                     return this.running("Target destroyed (" + this.kills + ")");
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Shooting cancelled");
               }
            }

            AiOperation.Result tickBow(autismclient.util.RotationMath.Rotation arc) {
               this.aim.setTargetRotation(arc);
               this.aim.tick();
               if (!this.drawing) {
                  this.drawing = true;
                  this.drawTicks = 0;
                  mc.options.keyUse.setDown(true);
                  return this.running("Drawing the bow");
               }

               this.drawTicks++;
               if (this.drawTicks < 20 || !mc.player.isUsingItem() || mc.player.getTicksUsingItem() < 20) {
                  return this.running("Drawing (" + Math.min(this.drawTicks, mc.player.getTicksUsingItem()) + "/20)");
               }

               if (!this.aim.isSettled()) {
                  return this.running("Holding at full pull while the aim settles");
               }

               mc.options.keyUse.setDown(false);
               this.drawing = false;
               this.shotsFired++;
               return this.running("Released shot " + this.shotsFired + "/" + shots);
            }

            AiOperation.Result tickCrossbow(autismclient.util.RotationMath.Rotation arc) {
               this.aim.setTargetRotation(arc);
               this.aim.tick();
               ItemStack held = mc.player.getMainHandItem();
               boolean loaded = held.get(DataComponents.CHARGED_PROJECTILES) != null && !held.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
               if (!loaded) {
                  mc.options.keyUse.setDown(true);
                  this.drawing = true;
                  return this.running("Charging the crossbow");
               }

               if (this.drawing) {
                  mc.options.keyUse.setDown(false);
                  this.drawing = false;
                  return this.running("Crossbow loaded");
               }

               if (!this.aim.isSettled()) {
                  return this.running("Aiming the loaded crossbow");
               }

               mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
               this.shotsFired++;
               return this.running("Fired shot " + this.shotsFired + "/" + shots);
            }

            AiOperation.Result tickThrow(autismclient.util.RotationMath.Rotation arc) {
               this.aim.setTargetRotation(arc);
               this.aim.tick();
               if (!this.aim.isSettled()) {
                  return this.running("Aiming the throw");
               }

               mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
               Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
               this.shotsFired++;
               return this.running("Thrown " + this.shotsFired + "/" + shots);
            }

            AiOperation.Result ensureWeapon() {
               ItemStack held = mc.player.getMainHandItem();
               if (AiRanged.isRangedWeapon(held)) {
                  if ((held.is(Items.BOW) || held.is(Items.CROSSBOW)) && AiGameState.countInventoryItem("minecraft:arrow") <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:arrow", 1);
                     String weapon = held.is(Items.BOW) ? "bow" : "crossbow";
                     return this.finish(AiOperation.Status.FAILED, "Holding a " + weapon + " with no arrows", AiJson.objectOf("missing", missing));
                  } else {
                     return null;
                  }
               } else {
                  for (String candidate : List.of("minecraft:bow", "minecraft:crossbow", "minecraft:trident", "minecraft:snowball", "minecraft:egg")) {
                     if (AiGameState.countInventoryItem(candidate) > 0
                        && (!candidate.equals("minecraft:bow") && !candidate.equals("minecraft:crossbow") || AiGameState.countInventoryItem("minecraft:arrow") > 0)
                        && AiItems.equipHotbar(AiItems.resolveItem(candidate)) >= 0) {
                        return this.running("Holding " + candidate);
                     }
                  }

                  JsonObject missing = new JsonObject();
                  missing.addProperty("minecraft:bow", 1);
                  missing.addProperty("minecraft:arrow", 8);
                  return this.finish(AiOperation.Status.FAILED, "No ranged weapon in inventory", AiJson.objectOf("missing", missing));
               }
            }

            autismclient.util.RotationMath.Rotation solveArc(Entity at) {
               ItemStack held = mc.player.getMainHandItem();
               List<autismclient.modules.ProjectileBallistics.ProjectileShot> possible = autismclient.modules.ProjectileBallistics.init(mc.player, held, true, false);
               if (possible.isEmpty()) {
                  return null;
               }

               autismclient.modules.ProjectileBallistics.ProjectileInfo info = possible.get(0).info();
               Vec3 point = autismclient.ai.engine.AiAim.defaultEntityAimPoint(at);
               Vec3 motion = this.motionOf(at);
               if (motion.lengthSqr() >= 0.0025000000000000005) {
                  double flight = point.distanceTo(mc.player.getEyePosition()) / 2.9;
                  point = point.add(motion.scale(flight));
               }

               float yaw = autismclient.util.RotationMath.lookAt(point, mc.player.getEyePosition()).yaw();
               Vec3 inherited = autismclient.modules.ProjectileBallistics.init(mc.player);
               float best = 0.0F / 0.0F;

               for (float pitch = 45.0F; pitch >= -60.0F; pitch -= 2.0F) {
                  if (this.wouldHit(info, yaw, pitch, at, inherited)) {
                     best = pitch;
                     break;
                  }
               }

               if (Float.isNaN(best)) {
                  return null;
               }

               for (float pitch = best + 2.0F; pitch >= best - 2.0F; pitch -= 0.15F) {
                  if (this.wouldHit(info, yaw, pitch, at, inherited)) {
                     return new autismclient.util.RotationMath.Rotation(yaw, pitch);
                  }
               }

               return new autismclient.util.RotationMath.Rotation(yaw, best);
            }

            boolean wouldHit(autismclient.modules.ProjectileBallistics.ProjectileInfo info, float yaw, float pitch, Entity at, Vec3 inherited) {
               autismclient.modules.ProjectileBallistics.Trajectory result = autismclient.modules.ProjectileBallistics.init(mc.level, mc.player, info, yaw, pitch, 120, inherited);
               HitResult hit = result.hit();
               Vec3 motion = this.motionOf(at);
               if (motion.lengthSqr() < 0.0025000000000000005) {
                  return hit instanceof EntityHitResult entityHit && (entityHit.getEntity() == at || this.isPartOf(entityHit.getEntity(), at));
               }

               List<AABB> boxes = new ArrayList<>();
               if (at instanceof EnderDragon dragon) {
                  for (EnderDragonPart part : dragon.getSubEntities()) {
                     boxes.add(part.getBoundingBox());
                  }
               } else {
                  boxes.add(at.getBoundingBox());
               }

               List<Vec3> points = result.points();
               Vec3 from = new Vec3(mc.player.getX(), mc.player.getEyeY() - 0.1, mc.player.getZ());

               for (int i = 0; i < points.size(); i++) {
                  Vec3 to = points.get(i);
                  Vec3 shift = motion.scale(i + 1);

                  for (AABB box : boxes) {
                     if (box.move(shift).inflate(0.3).clip(from, to).isPresent()) {
                        return true;
                     }
                  }

                  from = to;
               }

               return false;
            }

            boolean isPartOf(Entity hit, Entity at) {
               return hit instanceof EnderDragonPart part && part.parentMob == at;
            }

            Vec3 motionOf(Entity at) {
               return new Vec3(at.getX() - at.xo, at.getY() - at.yo, at.getZ() - at.zo);
            }

            Entity findTarget() {
               if (explicitId >= 0) {
                  Entity explicit = mc.level.getEntity(explicitId);
                  return explicit != null && explicit.isAlive() ? explicit : null;
               }

               Entity best = null;
               double bestDistance = radius * radius;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != mc.player
                     && entity.isAlive()
                     && entity instanceof LivingEntity
                     && (types.isEmpty() ? entity instanceof Enemy : types.contains(entity.getType()))) {
                     double distance = entity.distanceToSqr(mc.player);
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = entity;
                     }
                  }
               }

               return best;
            }

            void releaseUse() {
               if (mc.options != null) {
                  mc.options.keyUse.setDown(false);
               }

               this.drawing = false;
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf("target", spec, "fired", this.shotsFired, "requested", shots, "killed", this.kills);
               ItemStack held = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
               if (!held.isEmpty()) {
                  out.addProperty("weapon", BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
                  if (held.is(Items.BOW)) {
                     out.addProperty("arrows", AiGameState.countInventoryItem("minecraft:arrow"));
                  }
               }

               if (this.target != null) {
                  out.add(
                     "engaging",
                     AiJson.objectOf(
                        "entityId",
                        this.target.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(this.target.getType()).toString(),
                        "distance",
                        Math.round(mc.player.distanceTo(this.target) * 10.0F) / 10.0
                     )
                  );
               }

               if (this.solvedArc != null) {
                  out.add("arc", AiJson.objectOf("yaw", this.solvedArc.yaw(), "pitch", this.solvedArc.pitch()));
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finish(status, message, null);
            }

            AiOperation.Result finish(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.progressJson();
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
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.releaseUse();
                  this.aim.clear();
                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.originalSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Shooting requires a connected living player");
      }
   }

   static boolean isRangedWeapon(ItemStack stack) {
      return stack.is(Items.BOW)
         || stack.is(Items.CROSSBOW)
         || stack.is(Items.TRIDENT)
         || stack.is(Items.SNOWBALL)
         || stack.is(Items.EGG)
         || stack.is(Items.SPLASH_POTION)
         || stack.is(Items.LINGERING_POTION);
   }
}
