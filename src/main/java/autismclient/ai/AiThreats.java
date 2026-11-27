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
   static final double DEFAULT_MELEE_REACH = Math.sqrt(2.0399999618530273) - 0.6000000238418579;
   static final int RECENT_ATTACKER_TICKS = 160;
   static final int RECENT_AGGRESSIVE_TICKS = 60;
   static final int APPROACH_WINDOW_TICKS = 12;
   private static final Map<Integer, Long> lastDamagedPlayerTick = new HashMap<>();
   private static final Map<Integer, Long> lastAggressiveTick = new HashMap<>();
   private static final Map<Integer, Long> approachingSinceTick = new HashMap<>();
   private static final Map<Integer, Double> lastDistanceByEntity = new HashMap<>();
   private static long lastObservedTick = -9223372036854775808L;
   private static int lastPlayerHurtTime;
   private static Object trackedLevel;
   private static Object trackedPlayer;
   private static final Map<Integer, UUID> entityUuidById = new HashMap<>();
   private static final Map<Integer, Long> blazeFireStartTick = new HashMap<>();
   private static final Set<Integer> loggedAngryEndermen = new HashSet<>();
   private static final Map<Integer, Long> lastEndermanStareTick = new HashMap<>();
   private static final Map<Integer, String> lastEndermanStareContext = new HashMap<>();
   private static final Set<Integer> seenUnlitBlazes = new HashSet<>();
   private static long lastBlazePruneTick = -9223372036854775808L;

   private AiThreats() {
   }

   static boolean isLegacyCombatEnabled() {
      return Boolean.getBoolean("autism.ai.combat.legacy");
   }

   static AiThreats.Inner_DataUtil classify(Entity e) {
      String type = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
      if (!type.equals("guardian") && !type.equals("breeze") && !type.equals("vindicator")) {
         if (e instanceof LivingEntity rider) {
            AttackRange range = rider.getMainHandItem().get(DataComponents.ATTACK_RANGE);
            if (range != null && range.effectiveMaxRange(rider) > 3.5) {
               return AiThreats.Inner_DataUtil.AVOID;
            }
         }

         if (e instanceof Warden || e instanceof Evoker || e instanceof Ravager || e instanceof ElderGuardian) {
            return AiThreats.Inner_DataUtil.AVOID;
         } else if (e instanceof ZombifiedPiglin) {
            return AiThreats.Inner_DataUtil.ZOMBIFIED_PIGLIN;
         } else if (e instanceof Zombie z) {
            return z.isBaby() ? AiThreats.Inner_DataUtil.BABY_ZOMBIE : AiThreats.Inner_DataUtil.ZOMBIE;
         } else if (e instanceof WitherSkeleton) {
            return AiThreats.Inner_DataUtil.WITHER_SKELETON;
         } else if (e instanceof AbstractSkeleton s) {
            return s.getMainHandItem().is(Items.BOW) ? AiThreats.Inner_DataUtil.SKELETON : AiThreats.Inner_DataUtil.ZOMBIE;
         } else if (e instanceof Creeper) {
            return AiThreats.Inner_DataUtil.CREEPER;
         } else if (e instanceof Spider) {
            return AiThreats.Inner_DataUtil.SPIDER;
         } else if (e instanceof Enderman) {
            return AiThreats.Inner_DataUtil.ENDERMAN;
         } else if (e instanceof Blaze) {
            return AiThreats.Inner_DataUtil.BLAZE;
         } else if (e instanceof Ghast) {
            return AiThreats.Inner_DataUtil.GHAST;
         } else if (e instanceof PiglinBrute) {
            return AiThreats.Inner_DataUtil.BRUTE;
         } else if (e instanceof Piglin p) {
            return p.getMainHandItem().is(Items.CROSSBOW) ? AiThreats.Inner_DataUtil.CROSSBOW : AiThreats.Inner_DataUtil.PIGLIN;
         } else if (e instanceof Hoglin || e instanceof Zoglin) {
            return AiThreats.Inner_DataUtil.HOGLIN;
         } else if (e instanceof AbstractCubeMob) {
            return e instanceof Enemy ? AiThreats.Inner_DataUtil.CUBE : AiThreats.Inner_DataUtil.NEUTRAL;
         } else if (e instanceof Silverfish || e instanceof Endermite) {
            return AiThreats.Inner_DataUtil.SMALL;
         } else if (e instanceof Witch) {
            return AiThreats.Inner_DataUtil.WITCH;
         } else if (e instanceof Phantom) {
            return AiThreats.Inner_DataUtil.PHANTOM;
         } else if (e instanceof Pillager) {
            return AiThreats.Inner_DataUtil.CROSSBOW;
         } else if (e instanceof Enemy) {
            return AiThreats.Inner_DataUtil.OTHER_HOSTILE;
         } else {
            return e instanceof NeutralMob ? AiThreats.Inner_DataUtil.NEUTRAL : AiThreats.Inner_DataUtil.PASSIVE;
         }
      } else {
         return AiThreats.Inner_DataUtil.AVOID;
      }
   }

   static boolean isNormallyPassiveOrNeutral(AiThreats.Inner_DataUtil p) {
      return p == AiThreats.Inner_DataUtil.ENDERMAN
         || p == AiThreats.Inner_DataUtil.ZOMBIFIED_PIGLIN
         || p == AiThreats.Inner_DataUtil.PIGLIN
         || p == AiThreats.Inner_DataUtil.NEUTRAL
         || p == AiThreats.Inner_DataUtil.PASSIVE;
   }

   static boolean isMeleeThreat(AiThreats.Inner_DataUtil p) {
      boolean var10000;
      switch (p) {
         case ZOMBIE:
         case BABY_ZOMBIE:
         case SPIDER:
         case ENDERMAN:
         case WITHER_SKELETON:
         case PIGLIN:
         case BRUTE:
         case ZOMBIFIED_PIGLIN:
         case HOGLIN:
         case CUBE:
         case SMALL:
            var10000 = true;
            break;
         case SKELETON:
         case CROSSBOW:
         case CREEPER:
         case BLAZE:
         case GHAST:
         default:
            var10000 = false;
      }

      return var10000;
   }

   static double estimateThreatSpeedScore(Entity e) {
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

   static double meleeReach(Mob mob) {
      AttackRange range = mob.getActiveItem().get(DataComponents.ATTACK_RANGE);
      return range == null ? DEFAULT_MELEE_REACH : range.effectiveMaxRange(mob);
   }

   static double horizontalGapToAttackBox(Mob mob, AABB player) {
      AABB attack = mob.getBoundingBox().inflate(meleeReach(mob), 0.0, meleeReach(mob));
      if (!(attack.maxY < player.minY) && !(attack.minY > player.maxY)) {
         double gx = Math.max(attack.minX - player.maxX, player.minX - attack.maxX);
         double gz = Math.max(attack.minZ - player.maxZ, player.minZ - attack.maxZ);
         return Math.max(gx, gz);
      } else {
         return 10.0;
      }
   }

   static long currentClientTick() {
      return autismclient.util.AutismSessionState.getInstance().getClientTick();
   }

   static void observeTick(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         if (trackedLevel != mc.level || trackedPlayer != mc.player) {
            trackedLevel = mc.level;
            trackedPlayer = mc.player;
            lastDamagedPlayerTick.clear();
            lastAggressiveTick.clear();
            approachingSinceTick.clear();
            lastDistanceByEntity.clear();
            entityUuidById.clear();
            loggedAngryEndermen.clear();
            lastEndermanStareTick.clear();
            lastEndermanStareContext.clear();
            lastPlayerHurtTime = 0;
            lastObservedTick = -9223372036854775808L;
            blazeFireStartTick.clear();
            seenUnlitBlazes.clear();
            lastBlazePruneTick = -9223372036854775808L;
         }

         long tick = currentClientTick();
         if (tick != lastObservedTick) {
            lastObservedTick = tick;

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Mob mob && mob.isAlive()) {
                  UUID previousIdentity = entityUuidById.put(mob.getId(), mob.getUUID());
                  if (previousIdentity != null && !previousIdentity.equals(mob.getUUID())) {
                     lastDamagedPlayerTick.remove(mob.getId());
                     lastAggressiveTick.remove(mob.getId());
                     approachingSinceTick.remove(mob.getId());
                     lastDistanceByEntity.remove(mob.getId());
                     loggedAngryEndermen.remove(mob.getId());
                     lastEndermanStareTick.remove(mob.getId());
                     lastEndermanStareContext.remove(mob.getId());
                     blazeFireStartTick.remove(mob.getId());
                     seenUnlitBlazes.remove(mob.getId());
                  }

                  double d = mob.distanceTo(mc.player);
                  if (!(d > 48.0)) {
                     if (mob.isAggressive()) {
                        lastAggressiveTick.put(mob.getId(), tick);
                     }

                     if (mob instanceof Enderman enderman) {
                        Vec3 eye0 = mc.player.getEyePosition();
                        Vec3 to0 = new Vec3(enderman.getX() - mc.player.getX(), enderman.getEyeY() - eye0.y, enderman.getZ() - mc.player.getZ());
                        double len0 = to0.length();
                        if (len0 > 0.001 && len0 < 64.0 && mc.player.getViewVector(1.0F).normalize().dot(to0.scale(1.0 / len0)) > 1.0 - 0.025 / len0) {
                           lastEndermanStareTick.put(mob.getId(), tick);
                           lastEndermanStareContext.put(mob.getId(), autismclient.ai.engine.AiMotionAudit.lastContext() + "; " + autismclient.ai.engine.AiAim.lastDebugState());
                        }

                        if (enderman.isCreepy() && loggedAngryEndermen.add(mob.getId())) {
                           Long stared = lastEndermanStareTick.get(mob.getId());
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] enderman angered: last stare {} ticks ago, last hurt by the player {}, during '{}'",
                                 stared == null ? "never" : String.valueOf(tick - stared),
                                 mc.player.getLastHurtMob() == mob ? mc.player.tickCount - mc.player.getLastHurtMobTimestamp() + " ticks ago" : "no",
                                 autismclient.ai.engine.AiMotionAudit.lastContext()
                                    + "; "
                                    + autismclient.ai.engine.AiAim.lastDebugState()
                                    + (lastEndermanStareContext.containsKey(mob.getId()) ? "; stared during: " + lastEndermanStareContext.get(mob.getId()) : "")
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
                           loggedAngryEndermen.remove(mob.getId());
                        }
                     }

                     Double before = lastDistanceByEntity.put(mob.getId(), d);
                     if (before != null && before - d > 0.05) {
                        approachingSinceTick.merge(mob.getId(), tick, (old, t) -> (Long)old);
                     } else if (before != null && before - d < 0.01) {
                        approachingSinceTick.remove(mob.getId());
                     }
                  }
               }
            }

            int hurt = mc.player.hurtTime;
            if (hurt > lastPlayerHurtTime && hurt >= mc.player.hurtDuration - 1) {
               DamageSource source = mc.player.getLastDamageSource();
               if (source != null && source.getEntity() != null) {
                  lastDamagedPlayerTick.put(source.getEntity().getId(), tick);
               }
            }

            lastPlayerHurtTime = hurt;
            if (tick % 200L == 0L) {
               entityUuidById.keySet().removeIf(id -> !(mc.level.getEntity(id) instanceof Mob m && m.isAlive()));
               loggedAngryEndermen.retainAll(entityUuidById.keySet());
               lastEndermanStareTick.keySet().retainAll(entityUuidById.keySet());
               lastEndermanStareContext.keySet().retainAll(entityUuidById.keySet());
               blazeFireStartTick.keySet().retainAll(entityUuidById.keySet());
               seenUnlitBlazes.retainAll(entityUuidById.keySet());
               lastDamagedPlayerTick.values().removeIf(t -> tick - t > 160L);
               lastAggressiveTick.values().removeIf(t -> tick - t > 60L);
               lastDistanceByEntity.keySet().removeIf(id -> mc.level.getEntity(id) == null);
               approachingSinceTick.keySet().removeIf(id -> mc.level.getEntity(id) == null);
            }
         }
      }
   }

   static boolean isWearingGoldArmor(Minecraft mc) {
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

   static boolean recentlyDamagedPlayer(Entity e) {
      Long t = lastDamagedPlayerTick.get(e.getId());
      return t != null && currentClientTick() - t <= 160L;
   }

   static boolean recentlyStaredAtEnderman(Entity e) {
      Long t = lastEndermanStareTick.get(e.getId());
      return t != null && currentClientTick() - t < 40L;
   }

   static boolean recentlyAggressive(Entity e) {
      Long t = lastAggressiveTick.get(e.getId());
      return t != null && currentClientTick() - t <= 60L;
   }

   static boolean hasBeenApproachingFor(Entity e, int ticks) {
      Long since = approachingSinceTick.get(e.getId());
      return since != null && currentClientTick() - since >= ticks;
   }

   static boolean isActiveThreat(Minecraft mc, Entity e) {
      observeTick(mc);
      if (!(e instanceof LivingEntity living && living.isAlive() && e != mc.player)) {
         return false;
      } else if (recentlyDamagedPlayer(e)) {
         return true;
      } else if (e instanceof Mob mob && mob.getTarget() != null && mob.getTarget() != mc.player) {
         return false;
      } else {
         AiThreats.Inner_DataUtil p = classify(e);
         double d = e.distanceTo(mc.player);
         boolean sight = d < 48.0 && mc.player.hasLineOfSight(e);
         boolean var10000;
         switch (p) {
            case ZOMBIE:
            case BABY_ZOMBIE:
            case SPIDER:
            case WITHER_SKELETON:
               var10000 = d < 3.0 && sight || sight && d < 20.0 && (recentlyAggressive(e) || hasBeenApproachingFor(e, 6));
               break;
            case SKELETON:
            case CROSSBOW:
               var10000 = recentlyAggressive(e) && sight && d < 24.0;
               break;
            case CREEPER:
               if (sight && d < 16.0 || e instanceof Creeper c && c.getSwellDir() > 0) {
                  var10000 = true;
                  break;
               }

               var10000 = false;
               break;
            case ENDERMAN:
               if (recentlyDamagedPlayer(e) || e instanceof Enderman m && m.isCreepy() && (hasBeenApproachingFor(e, 4) || recentlyStaredAtEnderman(e) || e.distanceToSqr(mc.player) < 6.25)) {
                  var10000 = true;
                  break;
               }

               var10000 = false;
               break;
            case BLAZE:
               var10000 = sight && (d < 12.0 || e.isOnFire());
               break;
            case GHAST:
               if (e instanceof Ghast g && g.isCharging() || sight && d < 20.0 && Math.abs(e.getY() - mc.player.getY()) <= 5.0) {
                  var10000 = true;
                  break;
               }

               var10000 = false;
               break;
            case PIGLIN:
               var10000 = isWearingGoldArmor(mc) ? recentlyDamagedPlayer(e) : recentlyAggressive(e);
               break;
            case BRUTE:
            case HOGLIN:
               var10000 = recentlyAggressive(e) || p == AiThreats.Inner_DataUtil.BRUTE && (d < 8.0 || sight && d < 12.0);
               break;
            case ZOMBIFIED_PIGLIN:
               var10000 = recentlyDamagedPlayer(e);
               break;
            case CUBE:
               var10000 = d < 8.0;
               break;
            case SMALL:
               var10000 = d < 6.0;
               break;
            case WITCH:
               var10000 = sight && d < 10.0;
               break;
            case PHANTOM:
               var10000 = d < 24.0;
               break;
            case OTHER_HOSTILE:
               var10000 = d < 4.0 && sight || recentlyAggressive(e) && sight && d < 16.0;
               break;
            case NEUTRAL:
            case PASSIVE:
               var10000 = false;
               break;
            case AVOID:
               var10000 = sight && d < 20.0 || recentlyAggressive(e);
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }

   static int ticksUntilRangedShot(Minecraft mc, Entity e) {
      observeTick(mc);
      if (e instanceof AbstractSkeleton s && s.isUsingItem() && s.getUseItem().is(Items.BOW)) {
         return Math.max(0, 20 - s.getTicksUsingItem());
      } else if (e instanceof Mob m && m.getMainHandItem().is(Items.CROSSBOW) && CrossbowItem.isCharged(m.getMainHandItem()) && recentlyAggressive(e)) {
         return 4;
      } else if (e instanceof Ghast g && g.isCharging()) {
         return 8;
      } else if (e instanceof Blaze) {
         long tick = currentClientTick();
         if (tick != lastBlazePruneTick) {
            lastBlazePruneTick = tick;
            blazeFireStartTick.keySet().removeIf(id -> !(mc.level.getEntity(id) instanceof Blaze b && b.isOnFire()));
         }

         if (!e.isOnFire()) {
            seenUnlitBlazes.add(e.getId());
            return -1;
         } else {
            long since = blazeFireStartTick.computeIfAbsent(e.getId(), id -> seenUnlitBlazes.contains(id) ? tick : tick - 60L);
            return (int)Math.max(0L, 60L - (tick - since));
         }
      } else {
         return -1;
      }
   }

   static Entity findImminentShooter(Minecraft mc, int within) {
      if (mc.player != null && mc.level != null) {
         observeTick(mc);
         Entity best = null;
         int bestIn = 2147483647;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Mob && entity.isAlive()) {
               double d2 = entity.distanceToSqr(mc.player);
               if (!(d2 > 1600.0)) {
                  int in = ticksUntilRangedShot(mc, entity);
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

   static Vec3 horizontalUnit(Vec3 v) {
      double l = Math.hypot(v.x, v.z);
      return l < 1.0E-6 ? Vec3.ZERO : new Vec3(v.x / l, 0.0, v.z / l);
   }

   enum Inner_DataUtil {
      ZOMBIE,
      BABY_ZOMBIE,
      SKELETON,
      CROSSBOW,
      CREEPER,
      SPIDER,
      ENDERMAN,
      BLAZE,
      GHAST,
      WITHER_SKELETON,
      PIGLIN,
      BRUTE,
      ZOMBIFIED_PIGLIN,
      HOGLIN,
      CUBE,
      SMALL,
      WITCH,
      PHANTOM,
      OTHER_HOSTILE,
      NEUTRAL,
      PASSIVE,
      AVOID;
   }
}
