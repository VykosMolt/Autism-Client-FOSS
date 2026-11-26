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
         final String spec = AiJson.init_Str_JsonObject(args, "target", "hostiles").trim();
         final int explicitId = AiJson.init_I_JsonObject(args, "entityId", -1);
         final int shots = Math.max(1, Math.min(64, AiJson.init_I_JsonObject(args, "count", 1)));
         final double radius = Math.max(4.0, Math.min(48.0, AiJson.init_D_JsonObject(args, "radius", 32.0)));
         final int timeoutSeconds = Math.max(5, Math.min(900, AiJson.init_I_JsonObject(args, "timeoutSeconds", 120)));
         final Set<EntityType<?>> types = explicitId >= 0 ? Set.of() : AiCombat.init(spec);
         AiService service = AiService.load_cc();
         final boolean silentAim = service != null && service.init_AiSettings().aiSilentAim;
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final int init_I = mc.player.getInventory().getSelectedSlot();
            Entity init_Entity;
            autismclient.util.RotationMath.Inner_DataUtil init_ce_DataUtil;
            int load_I;
            int read;
            int write;
            int update;
            boolean init_Z;
            boolean load_Z;

            {
               this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.engine.AiAim.Inner_DataUtil.init_DataUtil_DataUtil);
               this.init_DataUtil.init_V_Z(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.load_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        this.load_I > 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                        "Shooting deadline reached after " + this.load_I + " shot(s)"
                     );
                  }

                  if (this.load_I >= shots) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Fired " + this.load_I + " shot(s), " + this.update + " target(s) down");
                  }

                  if (this.init_Entity != null && this.init_Entity instanceof LivingEntity living && living.isDeadOrDying()) {
                     this.update++;
                     this.init_Entity = null;
                     this.init_ce_DataUtil = null;
                     this.init_V_21();
                     return this.init("Target down (" + this.update + ")");
                  }

                  if (this.init_Entity == null
                     || this.init_Entity instanceof LivingEntity
                     || explicitId < 0
                     || this.init_Entity.isAlive() && !this.init_Entity.isRemoved()) {
                     if (this.init_Entity == null || this.init_Entity.isAlive() && !this.init_Entity.isRemoved()) {
                        if (this.init_Entity == null) {
                           this.init_Entity = this.init_Entity_4();
                           this.init_ce_DataUtil = null;
                           this.read = 0;
                           if (this.init_Entity == null) {
                              return this.init(
                                 this.load_I > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                 this.load_I > 0
                                    ? "No targets left after " + this.load_I + " shot(s)"
                                    : "No matching live target within " + (int)radius + " blocks"
                              );
                           }
                        }

                        AiOperation.Result weapon = this.init_AiOperation_Result_22();
                        if (weapon != null) {
                           return weapon;
                        }

                        autismclient.util.RotationMath.Inner_DataUtil arc = this.init_ce_DataUtil(this.init_Entity);
                        if (arc != null) {
                           this.init_ce_DataUtil = arc;
                           this.read = 0;
                           ItemStack held = mc.player.getMainHandItem();
                           if (held.is(Items.BOW)) {
                              return this.init(arc);
                           } else {
                              return held.is(Items.CROSSBOW) ? this.load(arc) : this.read(arc);
                           }
                        } else if (++this.read > 60) {
                           Entity dropped = this.init_Entity;
                           this.init_Entity = null;
                           this.init_V_21();
                           return this.init("No arc reaches " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath() + "; picking another");
                        } else {
                           return this.init("Solving the arc");
                        }
                     } else {
                        this.init_Entity = null;
                        this.init_ce_DataUtil = null;
                        this.init_V_21();
                        return this.init("Target despawned or unloaded (not a kill); picking another");
                     }
                  } else {
                     this.update++;
                     this.init_Entity = null;
                     this.init_ce_DataUtil = null;
                     this.init_V_21();
                     return this.init("Target destroyed (" + this.update + ")");
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Shooting cancelled");
               }
            }

            AiOperation.Result init(autismclient.util.RotationMath.Inner_DataUtil arc) {
               this.init_DataUtil.init_V_ce_DataUtil(arc);
               this.init_DataUtil.load_V();
               if (!this.init_Z) {
                  this.init_Z = true;
                  this.write = 0;
                  mc.options.keyUse.setDown(true);
                  return this.init("Drawing the bow");
               }

               this.write++;
               if (this.write < 20 || !mc.player.isUsingItem() || mc.player.getTicksUsingItem() < 20) {
                  return this.init("Drawing (" + Math.min(this.write, mc.player.getTicksUsingItem()) + "/20)");
               }

               if (!this.init_DataUtil.load_Z()) {
                  return this.init("Holding at full pull while the aim settles");
               }

               mc.options.keyUse.setDown(false);
               this.init_Z = false;
               this.load_I++;
               return this.init("Released shot " + this.load_I + "/" + shots);
            }

            AiOperation.Result load(autismclient.util.RotationMath.Inner_DataUtil arc) {
               this.init_DataUtil.init_V_ce_DataUtil(arc);
               this.init_DataUtil.load_V();
               ItemStack held = mc.player.getMainHandItem();
               boolean loaded = held.get(DataComponents.CHARGED_PROJECTILES) != null && !held.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
               if (!loaded) {
                  mc.options.keyUse.setDown(true);
                  this.init_Z = true;
                  return this.init("Charging the crossbow");
               }

               if (this.init_Z) {
                  mc.options.keyUse.setDown(false);
                  this.init_Z = false;
                  return this.init("Crossbow loaded");
               }

               if (!this.init_DataUtil.load_Z()) {
                  return this.init("Aiming the loaded crossbow");
               }

               mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
               this.load_I++;
               return this.init("Fired shot " + this.load_I + "/" + shots);
            }

            AiOperation.Result read(autismclient.util.RotationMath.Inner_DataUtil arc) {
               this.init_DataUtil.init_V_ce_DataUtil(arc);
               this.init_DataUtil.load_V();
               if (!this.init_DataUtil.load_Z()) {
                  return this.init("Aiming the throw");
               }

               mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
               Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
               this.load_I++;
               return this.init("Thrown " + this.load_I + "/" + shots);
            }

            AiOperation.Result init_AiOperation_Result_22() {
               ItemStack held = mc.player.getMainHandItem();
               if (AiRanged.init(held)) {
                  if ((held.is(Items.BOW) || held.is(Items.CROSSBOW)) && AiGameState.init_I_Str("minecraft:arrow") <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:arrow", 1);
                     String weapon = held.is(Items.BOW) ? "bow" : "crossbow";
                     return this.init(AiOperation.Status.FAILED, "Holding a " + weapon + " with no arrows", AiJson.init_JsonObject_ObjArr("missing", missing));
                  } else {
                     return null;
                  }
               } else {
                  for (String candidate : List.of("minecraft:bow", "minecraft:crossbow", "minecraft:trident", "minecraft:snowball", "minecraft:egg")) {
                     if (AiGameState.init_I_Str(candidate) > 0
                        && (!candidate.equals("minecraft:bow") && !candidate.equals("minecraft:crossbow") || AiGameState.init_I_Str("minecraft:arrow") > 0)
                        && AiItems.read(AiItems.load_af_DataUtil(candidate)) >= 0) {
                        return this.init("Holding " + candidate);
                     }
                  }

                  JsonObject missing = new JsonObject();
                  missing.addProperty("minecraft:bow", 1);
                  missing.addProperty("minecraft:arrow", 8);
                  return this.init(AiOperation.Status.FAILED, "No ranged weapon in inventory", AiJson.init_JsonObject_ObjArr("missing", missing));
               }
            }

            autismclient.util.RotationMath.Inner_DataUtil init_ce_DataUtil(Entity at) {
               ItemStack held = mc.player.getMainHandItem();
               List<autismclient.modules.ProjectileBallistics.Inner_BufferHelper> possible = autismclient.modules.ProjectileBallistics.init(mc.player, held, true, false);
               if (possible.isEmpty()) {
                  return null;
               }

               autismclient.modules.ProjectileBallistics.Inner_DataUtil info = possible.get(0).init_aX_DataUtil();
               Vec3 point = autismclient.ai.engine.AiAim.init_Vec3_Entity(at);
               Vec3 motion = this.init_Vec3(at);
               if (motion.lengthSqr() >= 0.0025000000000000005) {
                  double flight = point.distanceTo(mc.player.getEyePosition()) / 2.9;
                  point = point.add(motion.scale(flight));
               }

               float yaw = autismclient.util.RotationMath.init(point, mc.player.getEyePosition()).init();
               Vec3 inherited = autismclient.modules.ProjectileBallistics.init(mc.player);
               float best = 0.0F / 0.0F;

               for (float pitch = 45.0F; pitch >= -60.0F; pitch -= 2.0F) {
                  if (this.init(info, yaw, pitch, at, inherited)) {
                     best = pitch;
                     break;
                  }
               }

               if (Float.isNaN(best)) {
                  return null;
               }

               for (float pitch = best + 2.0F; pitch >= best - 2.0F; pitch -= 0.15F) {
                  if (this.init(info, yaw, pitch, at, inherited)) {
                     return new autismclient.util.RotationMath.Inner_DataUtil(yaw, pitch);
                  }
               }

               return new autismclient.util.RotationMath.Inner_DataUtil(yaw, best);
            }

            boolean init(autismclient.modules.ProjectileBallistics.Inner_DataUtil info, float yaw, float pitch, Entity at, Vec3 inherited) {
               autismclient.modules.ProjectileBallistics.Inner_CachePool result = autismclient.modules.ProjectileBallistics.init(mc.level, mc.player, info, yaw, pitch, 120, inherited);
               HitResult hit = result.init_HitResult();
               Vec3 motion = this.init_Vec3(at);
               if (motion.lengthSqr() < 0.0025000000000000005) {
                  return hit instanceof EntityHitResult entityHit && (entityHit.getEntity() == at || this.init(entityHit.getEntity(), at));
               }

               List<AABB> boxes = new ArrayList<>();
               if (at instanceof EnderDragon dragon) {
                  for (EnderDragonPart part : dragon.getSubEntities()) {
                     boxes.add(part.getBoundingBox());
                  }
               } else {
                  boxes.add(at.getBoundingBox());
               }

               List<Vec3> points = result.init_List();
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

            boolean init(Entity hit, Entity at) {
               return hit instanceof EnderDragonPart part && part.parentMob == at;
            }

            Vec3 init_Vec3(Entity at) {
               return new Vec3(at.getX() - at.xo, at.getY() - at.yo, at.getZ() - at.zo);
            }

            Entity init_Entity_4() {
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

            void init_V_21() {
               if (mc.options != null) {
                  mc.options.keyUse.setDown(false);
               }

               this.init_Z = false;
            }

            JsonObject init_JsonObject_38() {
               JsonObject out = AiJson.init_JsonObject_ObjArr("target", spec, "fired", this.load_I, "requested", shots, "killed", this.update);
               ItemStack held = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
               if (!held.isEmpty()) {
                  out.addProperty("weapon", BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
                  if (held.is(Items.BOW)) {
                     out.addProperty("arrows", AiGameState.init_I_Str("minecraft:arrow"));
                  }
               }

               if (this.init_Entity != null) {
                  out.add(
                     "engaging",
                     AiJson.init_JsonObject_ObjArr(
                        "entityId",
                        this.init_Entity.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).toString(),
                        "distance",
                        Math.round(mc.player.distanceTo(this.init_Entity) * 10.0F) / 10.0
                     )
                  );
               }

               if (this.init_ce_DataUtil != null) {
                  out.add("arc", AiJson.init_JsonObject_ObjArr("yaw", this.init_ce_DataUtil.init(), "pitch", this.init_ce_DataUtil.load()));
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_38().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_38();
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
                  this.init_V_21();
                  this.init_DataUtil.init_V();
                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.init_I) {
                     autismclient.util.InventorySlotHelper.init_V(mc, this.init_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Shooting requires a connected living player");
      }
   }

   static boolean init(ItemStack stack) {
      return stack.is(Items.BOW)
         || stack.is(Items.CROSSBOW)
         || stack.is(Items.TRIDENT)
         || stack.is(Items.SNOWBALL)
         || stack.is(Items.EGG)
         || stack.is(Items.SPLASH_POTION)
         || stack.is(Items.LINGERING_POTION);
   }
}
