package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class p {
   static final double init_D = 1.2;
   static final double load_D = 5.0;
   static final int init_I = 12;
   static final int load_I = 3;
   static final int read = 5;

   private p() {
   }

   static boolean init(Minecraft mc, boolean moving) {
      if (mc.player == null) {
         return false;
      }

      for (by.Inner_StreamUtil t : by.init(mc)) {
         if (by.init(t)) {
            Entity e = mc.level.getEntity(t.entityId());
            return true;
         }
      }

      return false;
   }

   static boolean init(by.Inner_StreamUtil t, double distance) {
      return t.secondsToClosest() >= 0.0 && t.secondsToClosest() <= 1.2 && distance <= 5.0;
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final boolean deflect = ag.init_Z_JsonObject(args, "deflect", true);
         final boolean shieldOk = ag.init_Z_JsonObject(args, "shield", false);
         final long deadline = System.nanoTime() + Math.min(10, Math.max(1, ag.init_I_JsonObject(args, "timeoutSeconds", 4))) * 1000000000L;
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final Object init_Obj = this.init_Minecraft.level;
            final Object load_Obj = this.init_Minecraft.getConnection();
            final Object read_Obj = this.init_Minecraft.player;
            V init_V = new V(this.init_Minecraft);
            final List<V> init_List = new ArrayList<>(List.of(this.init_V));
            final Set<Integer> init_Set = new HashSet<>();
            final Set<Integer> load_Set = new HashSet<>();
            final Map<Integer, UUID> init_Map = new HashMap<>();
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            int init_I;
            int load_I;
            int read_I;
            final long init_J = System.nanoTime();
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            int write;
            int update;
            int sync;
            int flush;
            Vec3 init_Vec3;
            Vec3 load_Vec3;
            int close;
            final Set<Integer> read_Set;
            final float init_F;
            float load_F;
            float read_F;

            {
               this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.a.DataUtil.Inner_DataUtil.write);
               this.update = -1;
               this.close = -100;
               this.read_Set = new HashSet<>();
               this.init_F = this.init_Minecraft.player.getHealth();
               this.load_F = this.init_F;
            }

            @Override
            public AiOperation.Result tick() {
               this.read_I++;
               if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (this.init_Minecraft.level == this.init_Obj
                     && this.init_Minecraft.getConnection() == this.load_Obj
                     && this.init_Minecraft.player == this.read_Obj
                     && this.init_Minecraft.player.isAlive()) {
                     List<by.Inner_StreamUtil> threats = new ArrayList<>();

                     for (by.Inner_StreamUtil t : by.init(this.init_Minecraft)) {
                        if (by.init(t)) {
                           threats.add(t);
                        }
                     }

                     for (by.Inner_StreamUtil t : threats) {
                        Entity observed = this.init_Minecraft.level.getEntity(t.entityId());
                        if (observed != null) {
                           UUID previous = this.init_Map.put(t.entityId(), observed.getUUID());
                           if (previous != null && !previous.equals(observed.getUUID())) {
                              this.load_Set.remove(t.entityId());
                              this.read_Set.remove(t.entityId());
                              if (this.update == t.entityId()) {
                                 this.update = -1;
                              }
                           }

                           this.init_Set.add(t.entityId());
                        }
                     }

                     this.read_F = this.read_F + Math.max(0.0F, this.load_F - this.init_Minecraft.player.getHealth());
                     this.load_F = this.init_Minecraft.player.getHealth();
                     this.init();
                     if (this.load_Z) {
                        this.read();
                     }

                     this.flush = 0;

                     for (int id : this.init_Set) {
                        boolean stillThreat = false;

                        for (by.Inner_StreamUtil t : threats) {
                           if (t.entityId() == id) {
                              stillThreat = true;
                              break;
                           }
                        }

                        if (!stillThreat && !this.load_Set.contains(id) && this.init_List.stream().noneMatch(r -> r.init_Z_I(id)) && this.read_F == 0.0F) {
                           this.flush++;
                        }
                     }

                     if (System.nanoTime() > deadline) {
                        return this.init(AiOperation.Status.TIMED_OUT, "Still under fire at the deadline", threats);
                     }

                     if (deflect) {
                        if (this.init_V.load_Z()
                           && threats.stream()
                              .anyMatch(
                                 tx -> this.init_Minecraft.level.getEntity(tx.entityId()) instanceof LargeFireball && !this.init_V.init_Z_I(tx.entityId())
                              )) {
                           this.init_V = new V(this.init_Minecraft);
                           this.init_List.add(this.init_V);
                        }

                        String reflection = this.init_V.init_Str_List(threats);
                        threats.removeIf(tx -> this.init_List.stream().anyMatch(r -> r.init_Z_I(tx.entityId())));
                        if (this.read_Z && !this.init_Minecraft.options.keyUse.isDown()) {
                           this.read_Z = false;
                        }

                        if (reflection != null) {
                           if (this.read_Z) {
                              this.init_Minecraft.options.keyUse.setDown(false);
                              this.init_Minecraft.gameMode.releaseUsingItem(this.init_Minecraft.player);
                           }

                           this.read_Z = false;
                           this.init_Vec3 = null;
                           this.write = 0;
                           return this.init(reflection, threats);
                        }
                     }

                     if (threats.isEmpty() && !this.init_V.init_Z()) {
                        return this.init(
                           AiOperation.Status.SUCCEEDED,
                           this.init_Set.isEmpty() && this.load_Set.isEmpty()
                              ? "No incoming projectiles"
                              : (this.read_F > 0.0F ? "Incoming threats resolved; damage was received" : "Incoming threats resolved without observed damage"),
                           List.of()
                        );
                     }

                     if (threats.isEmpty()) {
                        return this.init("Awaiting server fireball redirection", threats);
                     }

                     if (deflect) {
                        Entity fireball = this.init_Entity_2(threats);
                        if (fireball != null) {
                           if (this.update != fireball.getId()) {
                              this.update = fireball.getId();
                              this.load_Vec3 = fireball.getDeltaMovement();
                              this.sync = 0;
                           }

                           Entity owner = fireball instanceof Projectile projectile ? projectile.getOwner() : null;
                           Vec3 eye = this.init_Minecraft.player.getEyePosition();
                           Vec3 back = owner != null && owner.isAlive() ? owner.getBoundingBox().getCenter() : null;
                           boolean onLine = back != null
                              && fireball.getBoundingBox().inflate(0.2).clip(eye, eye.add(back.subtract(eye).normalize().scale(6.0))).isPresent();
                           R.init(onLine ? back : fireball.getBoundingBox().getCenter());
                           double reach = k.init_D(this.init_Minecraft);
                           BlockHitResult blockRay = this.init_Minecraft
                              .level
                              .clip(new ClipContext(eye, fireball.getBoundingBox().getCenter(), Block.COLLIDER, Fluid.NONE, this.init_Minecraft.player));
                           if (this.init_Minecraft.player.tickCount - this.close >= 4
                              && blockRay.getType() == Type.MISS
                              && fireball.getBoundingBox().distanceToSqr(eye) <= reach * reach
                              && k.load(this.init_Minecraft, fireball)) {
                              this.init_Minecraft.gameMode.attack(this.init_Minecraft.player, fireball);
                              this.read_Set.add(fireball.getId());
                              this.close = this.init_Minecraft.player.tickCount;
                              Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                              return this.init(onLine ? "Sending the fireball back" : "Batting the fireball away", threats);
                           }
                        }
                     }

                     by.Inner_StreamUtil dominant = threats.get(0);

                     for (by.Inner_StreamUtil t : threats) {
                        if (t.secondsToClosest() < dominant.secondsToClosest()) {
                           dominant = t;
                        }
                     }

                     if (shieldOk
                        && k.init(this.init_Minecraft, this.init_Minecraft.level.getEntity(dominant.entityId()))
                        && k.init(this.init_Minecraft, dominant.secondsToClosest() * 20.0)
                        && this.init(dominant, threats)) {
                        this.load();
                        this.init_DataUtil.init_V_Vec3(new Vec3(dominant.x(), dominant.y(), dominant.z()));
                        this.init_DataUtil.load_V();
                        if (!this.read_Z) {
                           this.read_Z = true;
                           this.init_Minecraft.options.keyUse.setDown(true);
                        }

                        this.load_I++;
                        return this.init("Blocking " + dominant.type(), threats);
                     } else {
                        if (this.read_Z) {
                           this.init_Minecraft.options.keyUse.setDown(false);
                           this.read_Z = false;
                        }

                        if (this.init_Vec3 == null || this.write % 3 == 0) {
                           this.init_Vec3 = this.init(dominant);
                        }

                        this.write++;
                        if (this.write > 12) {
                           this.load();
                           this.write = 0;
                           this.init_Vec3 = null;
                           return this.init("Re-assessing the dodge", threats);
                        } else if (this.init_Vec3 == null) {
                           this.load();
                           return this.init("Holding ground; no safe escape direction", threats);
                        } else {
                           this.load();
                           this.init_Minecraft.player.setYRot((float)(Math.toDegrees(Math.atan2(this.init_Vec3.z, this.init_Vec3.x)) - 90.0));
                           this.init_Minecraft.options.keyUp.setDown(true);
                           return this.init("Dodging " + dominant.type(), threats);
                        }
                     }
                  } else {
                     return this.init(AiOperation.Status.CANCELLED, "Dodge world or player changed", List.of());
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Dodge cancelled", List.of());
               }
            }

            void init() {
               if (this.update >= 0) {
                  Entity e = this.init_Minecraft.level.getEntity(this.update);
                  if (!this.read_Set.contains(this.update)
                     || e == null
                     || !e.getUUID().equals(this.init_Map.get(this.update))
                     || !e.isAlive()
                     || !(e instanceof Projectile projectile && projectile.getOwner() == this.init_Minecraft.player)
                        && (this.load_Vec3 == null || !(e.getDeltaMovement().dot(this.load_Vec3) < 0.0))) {
                     if (++this.sync > 20) {
                        this.update = -1;
                        this.load_Vec3 = null;
                     }
                  } else {
                     this.load_Set.add(this.update);
                     this.update = -1;
                     this.load_Vec3 = null;
                  }
               }
            }

            Vec3 init(by.Inner_StreamUtil dominant) {
               Vec3 dir = by.init_Vec3(
                  new Vec3(dominant.x(), dominant.y(), dominant.z()),
                  new Vec3(dominant.vx(), dominant.vy(), dominant.vz()),
                  dominant.gravity(),
                  this.init_Minecraft.player.getEyePosition(),
                  dominant.secondsToClosest()
               );
               Vec3 best = null;
               double score = -1.7976931348623157E308;
               if (dir != null) {
                  for (Vec3 candidate : List.of(
                     dir,
                     dir.scale(-1.0),
                     this.init_Minecraft.player.position().subtract(new Vec3(dominant.x(), this.init_Minecraft.player.getY(), dominant.z())).normalize()
                  )) {
                     if (this.init(candidate)) {
                        double yaw = Math.toRadians(this.init_Minecraft.player.getYRot());
                        double forward = -candidate.x * Math.sin(yaw) + candidate.z * Math.cos(yaw);
                        double strafe = candidate.x * Math.cos(yaw) + candidate.z * Math.sin(yaw);
                        int travelTicks = Math.max(1, Math.min(12, (int)Math.ceil(dominant.secondsToClosest() * 20.0)));
                        AABB landing = autismclient.ai.a.SyncManager.init(this.init_Minecraft, forward, strafe, travelTicks);
                        if (landing != null) {
                           Vec3 displacement = landing.getCenter().subtract(this.init_Minecraft.player.getBoundingBox().getCenter());
                           boolean contact = false;

                           for (Entity mob : this.init_Minecraft.level.entitiesForRendering()) {
                              if (mob instanceof LivingEntity && cv.init_Z(this.init_Minecraft, mob) && landing.intersects(mob.getBoundingBox().inflate(1.2))) {
                                 contact = true;
                                 break;
                              }
                           }

                           if (!contact) {
                              double clearance = 1.7976931348623157E308;

                              for (by.Inner_StreamUtil shot : by.init(this.init_Minecraft)) {
                                 if (by.init(shot)) {
                                    Entity projectile = this.init_Minecraft.level.getEntity(shot.entityId());
                                    if (projectile != null) {
                                       by.Inner_DataUtil predicted = by.init(this.init_Minecraft, projectile, landing, travelTicks);
                                       clearance = Math.min(clearance, predicted == null ? 10.0 : predicted.closestApproach());
                                    }
                                 }
                              }

                              if (clearance > score) {
                                 score = clearance;
                                 best = candidate;
                              }
                           }
                        }
                     }
                  }
               }

               return best;
            }

            boolean init(by.Inner_StreamUtil main, List<by.Inner_StreamUtil> threats) {
               Vec3 facing = new Vec3(main.x() - this.init_Minecraft.player.getX(), 0.0, main.z() - this.init_Minecraft.player.getZ()).normalize();

               for (by.Inner_StreamUtil shot : threats) {
                  if (!k.init(this.init_Minecraft, this.init_Minecraft.level.getEntity(shot.entityId()))) {
                     return false;
                  }

                  Vec3 direction = new Vec3(shot.x() - this.init_Minecraft.player.getX(), 0.0, shot.z() - this.init_Minecraft.player.getZ()).normalize();
                  if (facing.dot(direction) < 0.5) {
                     return false;
                  }
               }

               return true;
            }

            boolean init(Vec3 dir) {
               BlockPos feet = this.init_Minecraft.player.blockPosition();

               for (int ahead = 1; ahead <= 3; ahead++) {
                  BlockPos cell = feet.offset((int)Math.round(dir.x * ahead), 0, (int)Math.round(dir.z * ahead));
                  if (!ValueSink.init_Z_Minecraft_BlockPos(this.init_Minecraft, cell)) {
                     return false;
                  }

                  if (this.init_Minecraft.level.getBlockState(cell.below()).getCollisionShape(this.init_Minecraft.level, cell.below()).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }

            Entity init_Entity_2(List<by.Inner_StreamUtil> threats) {
               Entity best = null;
               double bestTime = 1.7976931348623157E308;

               for (by.Inner_StreamUtil t : threats) {
                  Entity e = this.init_Minecraft.level.getEntity(t.entityId());
                  if (e instanceof Projectile
                     && e.isAlive()
                     && e.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
                     && !(e instanceof LargeFireball)
                     && p.init(t, Math.sqrt(e.distanceToSqr(this.init_Minecraft.player)))
                     && t.secondsToClosest() < bestTime) {
                     best = e;
                     bestTime = t.secondsToClosest();
                  }
               }

               return best;
            }

            JsonObject init_JsonObject(List<by.Inner_StreamUtil> threats) {
               JsonArray remaining = new JsonArray();

               for (by.Inner_StreamUtil t : threats) {
                  remaining.add(
                     ag.init_JsonObject_ObjArr(
                        "id", t.entityId(), "type", t.type(), "closestApproach", t.closestApproach(), "secondsToClosest", t.secondsToClosest()
                     )
                  );
               }

               JsonObject reflection = this.init_V.init_JsonObject();
               JsonArray shots = new JsonArray();

               for (V controller : this.init_List) {
                  shots.add(controller.init_JsonObject());
               }

               for (String counter : List.of("serverConfirmedReflections", "ghastDeathsObserved", "missedAttempts", "defensiveBats", "movementTicks")) {
                  reflection.addProperty(counter, this.init_List.stream().mapToInt(r -> ag.init_I_JsonObject(r.init_JsonObject(), counter, 0)).sum());
               }

               reflection.add("shots", shots);
               return ag.init_JsonObject_ObjArr(
                  "defenseTicks",
                  this.read_I,
                  "defenseMs",
                  (System.nanoTime() - this.init_J) / 1000000L,
                  "dodged",
                  this.flush,
                  "deflected",
                  this.load_Set.size() + this.init_List.stream().mapToInt(V::init_I).sum(),
                  "blocked",
                  this.init_I,
                  "shieldTicks",
                  this.load_I,
                  "projectilesObserved",
                  this.init_Set.size(),
                  "ghastReflection",
                  reflection,
                  "healthLost",
                  this.read_F,
                  "outcomeEvidence",
                  "client health and live projectile motion; shield block events unavailable",
                  "threats",
                  remaining
               );
            }

            AiOperation.Result init(String message, List<by.Inner_StreamUtil> threats) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject(threats));
            }

            AiOperation.Result init(AiOperation.Status status, String message, List<by.Inner_StreamUtil> threats) {
               this.cancel();
               return R.init_AiOperation_Result(status, message, this.init_JsonObject(threats));
            }

            void load() {
               if (this.init_Minecraft.options != null) {
                  for (KeyMapping key : List.of(
                     this.init_Minecraft.options.keyUp,
                     this.init_Minecraft.options.keyDown,
                     this.init_Minecraft.options.keyLeft,
                     this.init_Minecraft.options.keyRight,
                     this.init_Minecraft.options.keySprint
                  )) {
                     key.setDown(autismclient.util.KeyMappingControl.init(key).autism$physicalForBody());
                  }
               }
            }

            void read() {
               if (this.init_Minecraft.options != null && this.load_Z) {
                  this.load_Z = false;
                  this.init_Minecraft.options.keyAttack.setDown(autismclient.util.KeyMappingControl.init(this.init_Minecraft.options.keyAttack).autism$physicalForBody());
               }
            }

            @Override
            public void cancel() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  this.load();
                  this.read();
                  this.init_V.load_V();
                  if (this.read_Z && this.init_Minecraft.options != null) {
                     this.init_Minecraft.options.keyUse.setDown(false);
                  }

                  this.read_Z = false;
                  this.init_DataUtil.init_V();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Dodging requires a connected living player");
      }
   }
}
