package autismclient.ai;

import autismclient.AutismClientAddon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiThreats {
   static final double init_D = Math.sqrt(2.0399999618530273) - 0.6000000238418579;
   static final int init_I = 160;
   static final int load_I = 60;
   static final int read_I = 12;
   private static final Map<Integer, Long> init_Map = new HashMap<>();
   private static final Map<Integer, Long> load_Map = new HashMap<>();
   private static final Map<Integer, Long> read_Map = new HashMap<>();
   private static final Map<Integer, Double> write_Map = new HashMap<>();
   private static long init_J = -9223372036854775808L;
   private static int write_I;
   private static Object init_Obj;
   private static Object load_Obj;
   private static final Map<Integer, UUID> update = new HashMap<>();
   private static final Map<Integer, Long> sync = new HashMap<>();
   private static final Set<Integer> init_Set = new HashSet<>();
   private static final Map<Integer, Long> flush = new HashMap<>();
   private static final Map<Integer, String> close = new HashMap<>();
   private static final Set<Integer> load_Set = new HashSet<>();
   private static long load_J = -9223372036854775808L;

   private AiThreats() {
   }

   static boolean init_Z() {
      return Boolean.getBoolean("autism.ai.combat.legacy");
   }

   static AiThreats.Inner_DataUtil init_cv_DataUtil(Entity e) {
      String type = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
      if (!type.equals("guardian") && !type.equals("breeze") && !type.equals("vindicator")) {
         if (e instanceof LivingEntity rider) {
            AttackRange range = rider.getMainHandItem().get(DataComponents.ATTACK_RANGE);
            if (range != null && range.effectiveMaxRange(rider) > 3.5) {
               return AiThreats.Inner_DataUtil.stop;
            }
         }

         if (e instanceof Warden || e instanceof Evoker || e instanceof Ravager || e instanceof ElderGuardian) {
            return AiThreats.Inner_DataUtil.stop;
         } else if (e instanceof ZombifiedPiglin) {
            return AiThreats.Inner_DataUtil.tick;
         } else if (e instanceof Zombie z) {
            return z.isBaby() ? AiThreats.Inner_DataUtil.load : AiThreats.Inner_DataUtil.init;
         } else if (e instanceof WitherSkeleton) {
            return AiThreats.Inner_DataUtil.poll;
         } else if (e instanceof AbstractSkeleton s) {
            return s.getMainHandItem().is(Items.BOW) ? AiThreats.Inner_DataUtil.read : AiThreats.Inner_DataUtil.init;
         } else if (e instanceof Creeper) {
            return AiThreats.Inner_DataUtil.update;
         } else if (e instanceof Spider) {
            return AiThreats.Inner_DataUtil.sync;
         } else if (e instanceof Enderman) {
            return AiThreats.Inner_DataUtil.flush;
         } else if (e instanceof Blaze) {
            return AiThreats.Inner_DataUtil.close;
         } else if (e instanceof Ghast) {
            return AiThreats.Inner_DataUtil.open;
         } else if (e instanceof PiglinBrute) {
            return AiThreats.Inner_DataUtil.pop;
         } else if (e instanceof Piglin p) {
            return p.getMainHandItem().is(Items.CROSSBOW) ? AiThreats.Inner_DataUtil.write : AiThreats.Inner_DataUtil.push;
         } else if (e instanceof Hoglin || e instanceof Zoglin) {
            return AiThreats.Inner_DataUtil.run;
         } else if (e instanceof AbstractCubeMob) {
            return e instanceof Enemy ? AiThreats.Inner_DataUtil.get : AiThreats.Inner_DataUtil.reset;
         } else if (e instanceof Silverfish || e instanceof Endermite) {
            return AiThreats.Inner_DataUtil.set;
         } else if (e instanceof Witch) {
            return AiThreats.Inner_DataUtil.put;
         } else if (e instanceof Phantom) {
            return AiThreats.Inner_DataUtil.add;
         } else if (e instanceof Pillager) {
            return AiThreats.Inner_DataUtil.write;
         } else if (e instanceof Enemy) {
            return AiThreats.Inner_DataUtil.clear;
         } else {
            return e instanceof NeutralMob ? AiThreats.Inner_DataUtil.reset : AiThreats.Inner_DataUtil.start;
         }
      } else {
         return AiThreats.Inner_DataUtil.stop;
      }
   }

   static boolean init(AiThreats.Inner_DataUtil p) {
      return p == AiThreats.Inner_DataUtil.flush
         || p == AiThreats.Inner_DataUtil.tick
         || p == AiThreats.Inner_DataUtil.push
         || p == AiThreats.Inner_DataUtil.reset
         || p == AiThreats.Inner_DataUtil.start;
   }

   static boolean load(AiThreats.Inner_DataUtil p) {
      boolean var10000;
      switch (p) {
         case init:
         case load:
         case sync:
         case flush:
         case poll:
         case push:
         case pop:
         case tick:
         case run:
         case get:
         case set:
            var10000 = true;
            break;
         case read:
         case write:
         case update:
         case close:
         case open:
         default:
            var10000 = false;
      }

      return var10000;
   }

   static double init_D(Entity e) {
      if (e instanceof LivingEntity living) {
         double speed = living.getAttributeValue(Attributes.MOVEMENT_SPEED);
         if (e.getVehicle() instanceof LivingEntity mount) {
            speed = Math.max(speed, mount.getAttributeValue(Attributes.MOVEMENT_SPEED));
         }

         return speed * speed / 0.454;
      } else {
         return 0.0;
      }
   }

   static double init(Mob mob) {
      AttackRange range = mob.getActiveItem().get(DataComponents.ATTACK_RANGE);
      return range == null ? init_D : range.effectiveMaxRange(mob);
   }

   static double init(Mob mob, AABB player) {
      AABB attack = mob.getBoundingBox().inflate(init(mob), 0.0, init(mob));
      if (!(attack.maxY < player.minY) && !(attack.minY > player.maxY)) {
         double gx = Math.max(attack.minX - player.maxX, player.minX - attack.maxX);
         double gz = Math.max(attack.minZ - player.maxZ, player.minZ - attack.maxZ);
         return Math.max(gx, gz);
      } else {
         return 10.0;
      }
   }

   static long init_J() {
      return autismclient.util.AutismSessionState.init_ck().state_I();
   }

   static void init_V(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         if (init_Obj != mc.level || load_Obj != mc.player) {
            init_Obj = mc.level;
            load_Obj = mc.player;
            init_Map.clear();
            load_Map.clear();
            read_Map.clear();
            write_Map.clear();
            update.clear();
            init_Set.clear();
            flush.clear();
            close.clear();
            write_I = 0;
            init_J = -9223372036854775808L;
            sync.clear();
            load_Set.clear();
            load_J = -9223372036854775808L;
         }

         long tick = init_J();
         if (tick != init_J) {
            init_J = tick;

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Mob mob && mob.isAlive()) {
                  UUID previousIdentity = update.put(mob.getId(), mob.getUUID());
                  if (previousIdentity != null && !previousIdentity.equals(mob.getUUID())) {
                     init_Map.remove(mob.getId());
                     load_Map.remove(mob.getId());
                     read_Map.remove(mob.getId());
                     write_Map.remove(mob.getId());
                     init_Set.remove(mob.getId());
                     flush.remove(mob.getId());
                     close.remove(mob.getId());
                     sync.remove(mob.getId());
                     load_Set.remove(mob.getId());
                  }

                  double d = mob.distanceTo(mc.player);
                  if (!(d > 48.0)) {
                     if (mob.isAggressive()) {
                        load_Map.put(mob.getId(), tick);
                     }

                     if (mob instanceof Enderman enderman) {
                        Vec3 eye0 = mc.player.getEyePosition();
                        Vec3 to0 = new Vec3(enderman.getX() - mc.player.getX(), enderman.getEyeY() - eye0.y, enderman.getZ() - mc.player.getZ());
                        double len0 = to0.length();
                        if (len0 > 0.001 && len0 < 64.0 && mc.player.getViewVector(1.0F).normalize().dot(to0.scale(1.0 / len0)) > 1.0 - 0.025 / len0) {
                           flush.put(mob.getId(), tick);
                           close.put(mob.getId(), autismclient.ai.engine.AiMotionAudit.init_Str() + "; " + autismclient.ai.engine.AiAim.init_Str());
                        }

                        if (enderman.isCreepy() && init_Set.add(mob.getId())) {
                           Long stared = flush.get(mob.getId());
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] enderman angered: last stare {} ticks ago, last hurt by the player {}, during '{}'",
                                 stared == null ? "never" : String.valueOf(tick - stared),
                                 mc.player.getLastHurtMob() == mob ? mc.player.tickCount - mc.player.getLastHurtMobTimestamp() + " ticks ago" : "no",
                                 autismclient.ai.engine.AiMotionAudit.init_Str()
                                    + "; "
                                    + autismclient.ai.engine.AiAim.init_Str()
                                    + (close.containsKey(mob.getId()) ? "; stared during: " + close.get(mob.getId()) : "")
                              );
                           to0 = mc.player.getEyePosition();
                           Vec3 to = new Vec3(enderman.getX() - mc.player.getX(), enderman.getEyeY() - to0.y, enderman.getZ() - mc.player.getZ());
                           double dot = mc.player.getViewVector(1.0F).normalize().dot(to.normalize());
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] enderman angered at {} ({} blocks): look yaw {} pitch {}, stare cone {} (dot {} vs {}), using {}",
                                 enderman.blockPosition().toShortString(),
                                 String.format(Locale.ROOT, "%.1f", d),
                                 String.format(Locale.ROOT, "%.0f", mc.player.getYRot()),
                                 String.format(Locale.ROOT, "%.0f", mc.player.getXRot()),
                                 dot > 1.0 - 0.025 / Math.max(0.1, to.length()),
                                 String.format(Locale.ROOT, "%.4f", dot),
                                 String.format(Locale.ROOT, "%.4f", 1.0 - 0.025 / Math.max(0.1, to.length())),
                                 mc.player.isUsingItem() ? mc.player.getUseItem().getItem().toString() : "nothing"
                              );
                           IntegratedServer server = mc.getSingleplayerServer();
                           if (server != null) {
                              ResourceKey<Level> dimension = mc.level.dimension();
                              UUID identity = enderman.getUUID();
                              server.execute(
                                 () -> {
                                    ServerLevel serverLevel = server.getLevel(dimension);
                                    if ((serverLevel == null ? null : serverLevel.getEntity(identity)) instanceof Enderman e) {
                                       DamageSource src = e.getLastDamageSource();
                                       AutismClientAddon.LOG
                                          .info(
                                             "[AI] enderman angered (server): target {}, last hurt by {}, last damage {}",
                                             e.getTarget() == null ? "none" : e.getTarget().getName().getString(),
                                             e.getLastHurtByMob() == null ? "none" : e.getLastHurtByMob().getName().getString(),
                                             src == null
                                                ? "none"
                                                : src.getMsgId() + (src.getEntity() == null ? "" : " by " + src.getEntity().getName().getString())
                                          );
                                    }
                                 }
                              );
                           }
                        } else if (!enderman.isCreepy()) {
                           init_Set.remove(mob.getId());
                        }
                     }

                     Double before = write_Map.put(mob.getId(), d);
                     if (before != null && before - d > 0.05) {
                        read_Map.merge(mob.getId(), tick, (old, t) -> (Long)old);
                     } else if (before != null && before - d < 0.01) {
                        read_Map.remove(mob.getId());
                     }
                  }
               }
            }

            int hurt = mc.player.hurtTime;
            if (hurt > write_I && hurt >= mc.player.hurtDuration - 1) {
               DamageSource source = mc.player.getLastDamageSource();
               if (source != null && source.getEntity() != null) {
                  init_Map.put(source.getEntity().getId(), tick);
               }
            }

            write_I = hurt;
            if (tick % 200L == 0L) {
               update.keySet().removeIf(id -> !(mc.level.getEntity(id) instanceof Mob m && m.isAlive()));
               init_Set.retainAll(update.keySet());
               flush.keySet().retainAll(update.keySet());
               close.keySet().retainAll(update.keySet());
               sync.keySet().retainAll(update.keySet());
               load_Set.retainAll(update.keySet());
               init_Map.values().removeIf(t -> tick - t > 160L);
               load_Map.values().removeIf(t -> tick - t > 60L);
               write_Map.keySet().removeIf(id -> mc.level.getEntity(id) == null);
               read_Map.keySet().removeIf(id -> mc.level.getEntity(id) == null);
            }
         }
      }
   }

   static boolean init_Z(Minecraft mc) {
      if (mc.player == null) {
         return false;
      }

      for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         ItemStack stack = mc.player.getItemBySlot(slot);
         if (stack.is(Items.GOLDEN_HELMET) || stack.is(Items.GOLDEN_CHESTPLATE) || stack.is(Items.GOLDEN_LEGGINGS) || stack.is(Items.GOLDEN_BOOTS)) {
            return true;
         }
      }

      return false;
   }

   static boolean init_Z(Entity e) {
      Long t = init_Map.get(e.getId());
      return t != null && init_J() - t <= 160L;
   }

   static boolean load(Entity e) {
      Long t = flush.get(e.getId());
      return t != null && init_J() - t < 40L;
   }

   static boolean read(Entity e) {
      Long t = load_Map.get(e.getId());
      return t != null && init_J() - t <= 60L;
   }

   static boolean init(Entity e, int ticks) {
      Long since = read_Map.get(e.getId());
      return since != null && init_J() - since >= ticks;
   }

   static boolean init_Z(Minecraft mc, Entity e) {
      init_V(mc);
      if (!(e instanceof LivingEntity living && living.isAlive() && e != mc.player)) {
         return false;
      } else if (init_Z(e)) {
         return true;
      } else if (e instanceof Mob mob && mob.getTarget() != null && mob.getTarget() != mc.player) {
         return false;
      } else {
         AiThreats.Inner_DataUtil p = init_cv_DataUtil(e);
         double d = e.distanceTo(mc.player);
         boolean sight = d < 48.0 && mc.player.hasLineOfSight(e);
         boolean var10000;
         switch (p) {
            case init:
            case load:
            case sync:
            case poll:
               var10000 = d < 3.0 && sight || sight && d < 20.0 && (read(e) || init(e, 6));
               break;
            case read:
            case write:
               var10000 = read(e) && sight && d < 24.0;
               break;
            case update:
               if (sight && d < 16.0 || e instanceof Creeper c && c.getSwellDir() > 0) {
                  var10000 = true;
                  break;
               }

               var10000 = false;
               break;
            case flush:
               if (init_Z(e) || e instanceof Enderman m && m.isCreepy() && (init(e, 4) || load(e) || e.distanceToSqr(mc.player) < 6.25)) {
                  var10000 = true;
                  break;
               }

               var10000 = false;
               break;
            case close:
               var10000 = sight && (d < 12.0 || e.isOnFire());
               break;
            case open:
               if (e instanceof Ghast g && g.isCharging() || sight && d < 20.0 && Math.abs(e.getY() - mc.player.getY()) <= 5.0) {
                  var10000 = true;
                  break;
               }

               var10000 = false;
               break;
            case push:
               var10000 = init_Z(mc) ? init_Z(e) : read(e);
               break;
            case pop:
            case run:
               var10000 = read(e) || p == AiThreats.Inner_DataUtil.pop && (d < 8.0 || sight && d < 12.0);
               break;
            case tick:
               var10000 = init_Z(e);
               break;
            case get:
               var10000 = d < 8.0;
               break;
            case set:
               var10000 = d < 6.0;
               break;
            case put:
               var10000 = sight && d < 10.0;
               break;
            case add:
               var10000 = d < 24.0;
               break;
            case clear:
               var10000 = d < 4.0 && sight || read(e) && sight && d < 16.0;
               break;
            case reset:
            case start:
               var10000 = false;
               break;
            case stop:
               var10000 = sight && d < 20.0 || read(e);
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }

   static int init_I(Minecraft mc, Entity e) {
      init_V(mc);
      if (e instanceof AbstractSkeleton s && s.isUsingItem() && s.getUseItem().is(Items.BOW)) {
         return Math.max(0, 20 - s.getTicksUsingItem());
      } else if (e instanceof Mob m && m.getMainHandItem().is(Items.CROSSBOW) && CrossbowItem.isCharged(m.getMainHandItem()) && read(e)) {
         return 4;
      } else if (e instanceof Ghast g && g.isCharging()) {
         return 8;
      } else if (e instanceof Blaze) {
         long tick = init_J();
         if (tick != load_J) {
            load_J = tick;
            sync.keySet().removeIf(id -> !(mc.level.getEntity(id) instanceof Blaze b && b.isOnFire()));
         }

         if (!e.isOnFire()) {
            load_Set.add(e.getId());
            return -1;
         } else {
            long since = sync.computeIfAbsent(e.getId(), id -> load_Set.contains(id) ? tick : tick - 60L);
            return (int)Math.max(0L, 60L - (tick - since));
         }
      } else {
         return -1;
      }
   }

   static Entity init(Minecraft mc, int within) {
      if (mc.player != null && mc.level != null) {
         init_V(mc);
         Entity best = null;
         int bestIn = 2147483647;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Mob && entity.isAlive()) {
               double d2 = entity.distanceToSqr(mc.player);
               if (!(d2 > 1600.0)) {
                  int in = init_I(mc, entity);
                  if (in >= 0 && in <= within && (!(entity instanceof Blaze) || !(d2 < 4.840000000000001)) && mc.player.hasLineOfSight(entity) && in < bestIn) {
                     bestIn = in;
                     best = entity;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   static Vec3 init(Vec3 v) {
      double l = Math.hypot(v.x, v.z);
      return l < 1.0E-6 ? Vec3.ZERO : new Vec3(v.x / l, 0.0, v.z / l);
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push,
      pop,
      tick,
      run,
      get,
      set,
      put,
      add,
      clear,
      reset,
      start,
      stop;
   }
}
