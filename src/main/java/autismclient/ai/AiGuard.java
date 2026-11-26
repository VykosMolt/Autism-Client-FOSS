package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

final class AiGuard {
   static final float init_F = 16.0F;
   static final float load_F = 7.0F;
   static final int init_I = 14;
   static final int load_I = 19;
   static final double init_D = 7.0;
   static final int read_I = 60;
   static final double load_D = 24.0;
   static final double read_D = 6.0;
   private static long write_J = -1L;
   private static Object init_Obj;
   private static Object load_Obj;
   private static final Map<Integer, UUID> init_Map = new HashMap<>();
   static final double write_D = 16.0;
   private static volatile int sync_I;
   private static volatile boolean update_Z;
   private static volatile boolean sync_Z;
   private static volatile boolean flush_Z;
   private static volatile boolean close_Z;
   private static volatile double close_D = 1.7976931348623157E308;
   private static final List<Long> init_List = new CopyOnWriteArrayList<>();
   static final double update_D = 8.0;
   private static volatile boolean open_Z;
   static volatile String init_Str = "{}";
   private static volatile long update_J;
   private static volatile long sync_J;
   static final long init_J = 10000000000L;
   private static volatile long flush_J;
   private static volatile long close_J;
   static volatile boolean init_Z;
   private static volatile int flush_I = -1;
   private static volatile boolean poll_Z;
   private static volatile long open_J;
   static volatile boolean load_Z;
   static volatile boolean read_Z;
   private static final Map<Integer, ArrayDeque<Long>> load_Map = new ConcurrentHashMap<>();
   static final long load_J = 90000000000L;
   static final double sync_D = 10.0;
   static final int write_I = 16;
   static volatile boolean write_Z;
   private static volatile long poll_J;
   static final int update_I = 3;
   static final String load_Str = "under attack with nothing to fight back with";
   private static long push;
   private static long pop;
   static final long read_J = 60000000000L;
   static final double flush_D = 24.0;
   static final String read_Str = "recovery ops allowed at low health: game:fish, game:farm (harvest only), game:collect_drops, game:eat, game:sleep, game:flee, game:obtain <food> with allowCombat:false, and game:goto while no hostile is within 24 blocks";

   private AiGuard() {
   }

   static boolean init(Entity e) {
      if (e instanceof Zombie) {
         return true;
      } else if (e instanceof PiglinBrute) {
         return true;
      } else {
         return e instanceof Piglin piglin ? !piglin.getMainHandItem().is(Items.CROSSBOW) : load(e);
      }
   }

   static boolean load(Entity e) {
      return e instanceof PiglinBrute || e instanceof Hoglin || e instanceof Zoglin || e instanceof WitherSkeleton;
   }

   static void init_V() {
      update_J = System.nanoTime() + 500000000L;
   }

   static boolean init_Z() {
      return System.nanoTime() < update_J;
   }

   static void load_V() {
      flush_J = System.nanoTime();
   }

   static void read_V() {
      if (init_Z) {
         close_J = System.nanoTime();
      }
   }

   static boolean load_Z() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         return mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, feet.getX(), feet.getZ()) > feet.getY() + 4;
      } else {
         return false;
      }
   }

   static void write_V() {
      sync_J = System.nanoTime();
   }

   static void update_V() {
      open_J = System.nanoTime();
   }

   static void init_V(int id) {
      if (id >= 0 && load(id)) {
         autismclient.ai.engine.AiSight.init(id);
         long now = System.nanoTime();
         load_Map.values().forEach(q -> q.removeIf(t -> now - t > 90000000000L));
         load_Map.values().removeIf(ArrayDeque::isEmpty);
         load_Map.computeIfAbsent(id, k -> new ArrayDeque<>()).add(now);
      }
   }

   static int init_I(int id) {
      if (!load(id)) {
         return 0;
      }

      ArrayDeque<Long> q = load_Map.get(id);
      if (q == null) {
         return 0;
      }

      long now = System.nanoTime();
      int n = 0;

      for (long t : q) {
         if (now - t <= 90000000000L) {
            n++;
         }
      }

      return n;
   }

   static Entity init_Entity() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null && flush_I >= 0 ? mc.level.getEntity(flush_I) : null;
   }

   static boolean init(Minecraft mc, Entity shooter) {
      if (AiCombat.init(shooter)) {
         return false;
      } else {
         return shooter.distanceTo(mc.player) <= 4.5 && Math.abs(shooter.getY() - mc.player.getY()) <= 3.0 && mc.player.hasLineOfSight(shooter)
            ? true
            : AiCombat.init_I_Minecraft(mc, shooter, 10.0) >= 0;
      }
   }

   static AiGuard.GuardDecision init_X_StreamUtil(AiGuard.GuardPolicy policy, AiGuard.StatusSnapshot vitals) {
      boolean threatened = vitals.hostilesNear() > 0 && (vitals.underAttack() || vitals.health() <= policy.engageHealth());
      if (threatened && vitals.health() <= policy.fleeHealth()) {
         if (policy.flee()) {
            return new AiGuard.GuardDecision(AiGuard.GuardAction.FLEE, "health " + (int)vitals.health() + " with " + vitals.hostilesNear() + " hostile(s) near");
         } else {
            return policy.fight() && vitals.hasWeapon()
               ? new AiGuard.GuardDecision(AiGuard.GuardAction.FIGHT, "cornered at health " + (int)vitals.health())
               : AiGuard.GuardDecision.NONE;
         }
      } else if (threatened && policy.flee() && load(vitals) && !init_Z(vitals)) {
         return new AiGuard.GuardDecision(AiGuard.GuardAction.FLEE, vitals.hostilesNear() + " hostiles at health " + (int)vitals.health() + ": outnumbered");
      } else if (threatened && policy.fight() && vitals.hasWeapon()) {
         return new AiGuard.GuardDecision(AiGuard.GuardAction.FIGHT, vitals.hostilesNear() + " hostile(s) within 7 blocks");
      } else if (threatened && policy.flee()) {
         return new AiGuard.GuardDecision(AiGuard.GuardAction.FLEE, "under attack with nothing to fight back with");
      } else if (policy.eat() && vitals.hasFood() && vitals.food() <= policy.eatFood() && vitals.hostilesNear() == 0 && AiSurvival.init_Z(vitals.food())) {
         return new AiGuard.GuardDecision(AiGuard.GuardAction.EAT, "food " + vitals.food() + "/20");
      } else {
         return policy.tidy() && vitals.freeSlots() == 0 && vitals.hostilesNear() == 0
            ? new AiGuard.GuardDecision(AiGuard.GuardAction.TIDY, "inventory full")
            : AiGuard.GuardDecision.NONE;
      }
   }

   static boolean init_Z(AiGuard.StatusSnapshot vitals) {
      Minecraft mc = Minecraft.getInstance();
      return !AiThreats.init_Z() && mc != null && mc.player != null ? mc.player.getArmorValue() >= 12 && vitals.health() >= 12.0F && vitals.hostilesNear() < 5 : false;
   }

   static boolean read_Z() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && mc.level != null) {
         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity.isAlive() && entity.distanceToSqr(mc.player) < 256.0 && AiThreats.init_cv_DataUtil(entity) == AiThreats.Inner_DataUtil.AVOID && AiThreats.init_Z(mc, entity)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean load(AiGuard.StatusSnapshot vitals) {
      return vitals.hostilesNear() >= 3 || vitals.hostilesNear() >= 2 && vitals.health() < 14.0F;
   }

   static String init_Str(AiGuard.StatusSnapshot vitals) {
      if (vitals.food() <= 6 && !vitals.hasFood()) {
         return "starving with no food in inventory";
      } else if (vitals.health() <= 7.0F && !vitals.hasFood()) {
         return "low health with no food to regenerate from";
      } else if (vitals.freeSlots() == 0) {
         return "inventory is full; further drops will be lost";
      } else if (vitals.hostilesNear() > 0 && !vitals.hasWeapon()) {
         return "hostiles nearby with no weapon";
      } else {
         return vitals.worstToolUses() <= 16 ? "a hotbar tool has only " + vitals.worstToolUses() + " uses left; replace it before it breaks" : "";
      }
   }

   static void sync_V() {
      poll_J = System.nanoTime();
   }

   static boolean write_Z() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.level != null && init(mc.level, mc.player.blockPosition());
   }

   static boolean init(BlockGetter level, BlockPos feet) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx != 0 || dz != 0) {
               boolean ground = false;

               for (int d = 0; d <= 3 && !ground; d++) {
                  BlockPos cell = feet.offset(dx, -d, dz);
                  BlockState state = level.getBlockState(cell);
                  if (!state.getFluidState().isEmpty()) {
                     break;
                  }

                  if (!state.getCollisionShape(level, cell).isEmpty()) {
                     ground = true;
                  }
               }

               if (!ground) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   static boolean update_Z() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         int zombies = 0;
         int inReach = 0;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Zombie zombie && zombie.isAlive() && zombie.distanceToSqr(mc.player) <= 49.0) {
               if (zombie.getY() > mc.player.getY() + 1.0) {
                  return false;
               }

               zombies++;
               if (zombie.distanceToSqr(mc.player) <= 6.25) {
                  inReach++;
               }
            }
         }

         if (zombies == 1 && inReach == 1) {
            return false;
         }

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Mob mob && mob.isAlive() && load(entity) && mob.distanceToSqr(mc.player) <= 6.25) {
               return false;
            }
         }

         for (int h = 2; h <= 4; h++) {
            BlockPos over = feet.above(h);
            if (!mc.level.getBlockState(over).getCollisionShape(mc.level, over).isEmpty() || !mc.level.getFluidState(over).isEmpty()) {
               return false;
            }
         }

         return init(mc.level, feet);
      } else {
         return false;
      }
   }

   static boolean init(boolean creeper, boolean skeleton, int ranged, boolean footingSafe) {
      return !creeper && !skeleton && ranged == 0 && footingSafe;
   }

   static int init_I() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      Inventory inventory = mc.player.getInventory();
      int n = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (stack.getItem() instanceof BlockItem block
            && !(block.getBlock() instanceof FallingBlock)
            && autismclient.ai.engine.AiRestock.init_I(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) >= 0
            && autismclient.ai.engine.AiRestock.init(stack, inventory)) {
            n += stack.getCount();
         }
      }

      return n;
   }

   private static void init(Minecraft mc) {
      if (init_Obj != mc.level || load_Obj != mc.player) {
         init_Obj = mc.level;
         load_Obj = mc.player;
         write_J = -1L;
         load_Map.clear();
         init_Map.clear();
         init_List.clear();
         poll_J = 0L;
         open_J = 0L;
         close_J = 0L;
         flush_J = 0L;
         sync_J = 0L;
         pop = 0L;
         push = 0L;
         flush_I = -1;
         poll_Z = false;
      }
   }

   private static boolean load(int id) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.level != null && mc.player != null) {
         init(mc);
         Entity entity = mc.level.getEntity(id);
         if (entity != null && entity.isAlive()) {
            UUID previous = init_Map.put(id, entity.getUUID());
            if (previous != null && !previous.equals(entity.getUUID())) {
               load_Map.remove(id);
            }

            return true;
         } else {
            load_Map.remove(id);
            init_Map.remove(id);
            return false;
         }
      } else {
         return false;
      }
   }

   static AiGuard.StatusSnapshot init_X_BufferHelper() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         init(mc);
         int hostiles = 0;
         int ranged = 0;
         boolean zombiesOnly = true;
         boolean creeperSeen = false;
         boolean skeletonSeen = false;
         boolean heavy = false;
         double nearestCreeper = 1.7976931348623157E308;
         Map<String, Integer> kinds = new TreeMap<>();
         long now = autismclient.util.AutismSessionState.init_ck().state_I();
         if (mc.player.hurtTime > 0) {
            write_J = now;
         }

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Ghast)) {
               if (entity instanceof Creeper creeper && creeper.isAlive() && creeper.distanceToSqr(mc.player) < 100.0 && mc.player.hasLineOfSight(creeper)) {
                  write_J = now;
                  break;
               }

               if (entity instanceof Mob mob
                  && mob.isAggressive()
                  && mob.isAlive()
                  && autismclient.ai.engine.AiSight.init(mob)
                  && mob.distanceToSqr(mc.player) < 256.0
                  && load(mc, mob)) {
                  write_J = now;
                  break;
               }

               if (entity instanceof Monster mob
                  && mob.isAggressive()
                  && mob.isAlive()
                  && !autismclient.ai.engine.AiSight.init(mob)
                  && !(mob instanceof Creeper)
                  && mob.distanceToSqr(mc.player) < 25.0
                  && mc.player.hasLineOfSight(mob)) {
                  write_J = now;
                  break;
               }

               if (entity instanceof Zombie zombie
                  && !(zombie instanceof ZombifiedPiglin)
                  && zombie.isAlive()
                  && zombie.distanceToSqr(mc.player) < 9.0
                  && mc.player.hasLineOfSight(zombie)) {
                  write_J = now;
                  break;
               }
            }
         }

         boolean hit = init(write_J, now);
         Entity nearestShooter = null;
         double nearestShooterSqr = 1.7976931348623157E308;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity.isAlive() && !(entity instanceof Ghast) && !(entity instanceof EnderDragon)) {
               if (autismclient.ai.engine.AiSight.init(entity)) {
                  double d2 = entity.distanceToSqr(mc.player);
                  boolean sees = d2 < 256.0 && load(mc, entity);
                  boolean aiming = entity instanceof Mob mob && mob.isAggressive();
                  if (sees && (hit || aiming || d2 < 49.0) || d2 < 9.0) {
                     hostiles++;
                     ranged++;
                     if (entity instanceof AbstractSkeleton) {
                        skeletonSeen = true;
                     }

                     kinds.merge(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath(), 1, Integer::sum);
                     if (d2 < nearestShooterSqr) {
                        nearestShooterSqr = d2;
                        nearestShooter = entity;
                     }
                  }
               } else {
                  if (entity instanceof Creeper && entity.distanceToSqr(mc.player) < 100.0 && mc.player.hasLineOfSight(entity)) {
                     creeperSeen = true;
                     nearestCreeper = Math.min(nearestCreeper, Math.sqrt(entity.distanceToSqr(mc.player)));
                     if (entity.distanceToSqr(mc.player) >= 49.0) {
                        hostiles++;
                        zombiesOnly = false;
                        kinds.merge("creeper", 1, Integer::sum);
                        continue;
                     }
                  }

                  if ((
                        !AiCombat.init(entity)
                           || !(entity.distanceToSqr(mc.player) >= 9.0) && !(entity instanceof Mob mob && AiThreats.init(mob, mc.player.getBoundingBox()) > 1.0)
                     )
                     && (!(entity instanceof Creeper) || mc.player.hasLineOfSight(entity))) {
                     if (entity instanceof Enemy
                        && entity.distanceToSqr(mc.player) < 49.0
                        && (entity.distanceToSqr(mc.player) < 9.0 || mc.player.hasLineOfSight(entity))
                        && (AiThreats.init_Z() || AiThreats.init_Z(mc, entity))) {
                        hostiles++;
                        if (AiThreats.init_Z() ? !(entity instanceof Zombie) : !init(entity)) {
                           zombiesOnly = false;
                        }

                        if (!AiThreats.init_Z() && load(entity)) {
                           heavy = true;
                        }

                        if (entity instanceof AbstractSkeleton) {
                           skeletonSeen = true;
                        }

                        kinds.merge(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath(), 1, Integer::sum);
                     } else if (init(entity, mc) && entity.distanceToSqr(mc.player) < Math.pow(AiThreats.init_Z() ? 24.0 : 6.0, 2.0)) {
                        hostiles++;
                        if (!AiThreats.init_Z()) {
                           zombiesOnly = false;
                        }
                     }
                  }
               }
            }
         }

         sync_I = ranged;
         flush_I = nearestShooter == null ? -1 : nearestShooter.getId();
         poll_Z = nearestShooter != null && init(mc, nearestShooter);
         update_Z = zombiesOnly && ranged == 0 && hostiles > 0;
         sync_Z = zombiesOnly && hostiles > ranged;
         flush_Z = heavy && zombiesOnly && hostiles > ranged;
         close_Z = creeperSeen;
         close_D = nearestCreeper;
         open_Z = skeletonSeen;
         init_Str = kinds.toString() + (ranged > 0 ? " +" + ranged + " shooting" : "");
         return new AiGuard.StatusSnapshot(
            mc.player.getHealth(),
            mc.player.getMaxHealth(),
            mc.player.getFoodData().getFoodLevel(),
            hostiles,
            init(write_J, now),
            open(),
            AiItems.read() >= 0,
            AiItems.init_I(),
            load_I()
         );
      } else {
         return new AiGuard.StatusSnapshot(0.0F, 20.0F, 20, 0, false, false, false, 0);
      }
   }

   static boolean init(Minecraft mc, Vec3 motion) {
      if (mc.player != null && mc.level != null) {
         boolean unknown = motion == null;
         if (unknown) {
            motion = new Vec3(0.2, 0.0, 0.0);
         }

         double speed = Math.hypot(motion.x, motion.z);
         if (speed < 0.12) {
            return false;
         }

         if (mc.player.isInWater()) {
            return false;
         }

         double health = mc.player.getHealth();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Mob mob && mob.isAlive() && !(mob.distanceToSqr(mc.player) > 256.0) && AiThreats.init_Z(mc, mob)) {
               AiThreats.Inner_DataUtil p = AiThreats.init_cv_DataUtil(mob);
               double dx = mob.getX() - mc.player.getX();
               double dz = mob.getZ() - mc.player.getZ();
               double d = Math.hypot(dx, dz);
               boolean ahead = !unknown && d > 0.001 && (dx * motion.x + dz * motion.z) / (d * speed) > 0.5;
               if (unknown && d < 5.0) {
                  return false;
               }

               if (!autismclient.ai.engine.AiSight.init(mob) && p != AiThreats.Inner_DataUtil.CROSSBOW) {
                  if (p == AiThreats.Inner_DataUtil.CREEPER) {
                     if (ahead || d < 5.0) {
                        return false;
                     }
                  } else {
                     if (AiThreats.load(p) && !(AiThreats.init_D(mob) > Math.max(0.2, speed * 0.85))) {
                        if (!ahead && !(AiThreats.init(mob, mc.player.getBoundingBox()) < 1.2)) {
                           continue;
                        }

                        return false;
                     }

                     return false;
                  }
               } else if (health < 12.0 || d < 8.0) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean load(Minecraft mc, Entity shooter) {
      return autismclient.ai.engine.AiSight.init_Z_BlockGetter_3(
         mc.level, shooter.getEyePosition(), mc.player.getEyePosition(), -9223372036854775808L, -9223372036854775808L
      );
   }

   static boolean init(long lastHurtAt, long now) {
      return lastHurtAt >= 0L && now >= lastHurtAt && now - lastHurtAt < 60L;
   }

   static boolean sync_Z() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Entity only = null;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Enemy && entity.isAlive() && !(entity.distanceToSqr(mc.player) > 256.0) && AiThreats.init_Z(mc, entity)) {
               if (only != null) {
                  return false;
               }

               only = entity;
            }
         }

         return only instanceof Spider || only instanceof Silverfish || only instanceof Zombie zombie && zombie.isBaby();
      } else {
         return false;
      }
   }

   static boolean flush() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Enemy
               && entity.isAlive()
               && entity.distanceToSqr(mc.player) < 9.0
               && AiThreats.load(AiThreats.init_cv_DataUtil(entity))
               && AiThreats.init_Z(mc, entity)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean init(Entity entity, Minecraft mc) {
      if (!(entity instanceof Enderman enderman)) {
         return false;
      } else {
         if (enderman.isCreepy()) {
            return AiThreats.init_Z() || AiThreats.init_Z(mc, entity);
         }

         EntityReference<LivingEntity> angerTarget = enderman.getPersistentAngerTarget();
         return angerTarget != null && angerTarget.matches(mc.player);
      }
   }

   static AiOperation init_AiOperation_X_CachePool(AiGuard.GuardPolicy policy, AiGuard.StatusSnapshot vitals, int budgetSeconds) {
      read_Z = false;
      AiGuard.GuardDecision decision = init_X_StreamUtil(policy, vitals);
      AiGuard.GuardAction action = decision.action();
      boolean blockedRetreat = flush_J != 0L && System.nanoTime() - flush_J < 10000000000L;
      if (action == AiGuard.GuardAction.NONE && policy.flee() && blockedRetreat) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Enemy
                  && entity.isAlive()
                  && !autismclient.ai.engine.AiSight.init(entity)
                  && Math.abs(entity.getY() - mc.player.getY()) <= 3.0
                  && entity.distanceToSqr(mc.player) <= 441.0
                  && mc.player.hasLineOfSight(entity)) {
                  action = AiGuard.GuardAction.FLEE;
                  break;
               }
            }
         }
      }

      if (action == AiGuard.GuardAction.FLEE
         && "under attack with nothing to fight back with".equals(decision.reason())
         && policy.fight()
         && vitals.health() >= 8.0F
         && sync_Z()) {
         action = AiGuard.GuardAction.FIGHT;
      }

      if (action == AiGuard.GuardAction.FIGHT && policy.flee() && sync_J != 0L && System.nanoTime() - sync_J < 10000000000L) {
         action = AiGuard.GuardAction.FLEE;
      }

      if ((action == AiGuard.GuardAction.FIGHT || action == AiGuard.GuardAction.FLEE)
         && sync_I > 0
         && vitals.hostilesNear() == sync_I
         && !close_Z
         && init_I(flush_I) >= 2
         && AiCombat.init(init_Entity())
         && vitals.health() >= 12.0F) {
         return null;
      }

      int blocks = init_I();
      boolean perchBackoff = poll_J != 0L && System.nanoTime() - poll_J < 10000000000L;
      if (action != AiGuard.GuardAction.NONE && action != AiGuard.GuardAction.EAT && action != AiGuard.GuardAction.TIDY) {
         AutismClientAddon.LOG
            .info(
               "[AI] guard: {} ({}) seeing {}, health {}, {} blocks, underground {}, zombies only {}{}{}{}",
               action,
               decision.reason(),
               init_Str,
               (int)vitals.health(),
               blocks,
               load_Z(),
               update_Z,
               perchBackoff ? ", perch backing off" : "",
               update_Z && !update_Z() ? ", a drop beside (no perch)" : "",
               sync_I > 0 ? (poll_Z ? ", shooter in a straight run" : ", shooter out of a straight run") : ""
            );
      }

      if (action != AiGuard.GuardAction.FIGHT && action != AiGuard.GuardAction.FLEE
         || !sync_Z
         || !flush_Z && vitals.hostilesNear() < 2 && !(vitals.health() < 16.0F) && !load_Z()
         || blocks < 3
         || perchBackoff
         || !update_Z()
         || !AiThreats.init_Z() && !flush_Z && Minecraft.getInstance().player.getArmorValue() >= 12 && !(vitals.health() < 12.0F)) {
         write_Z = false;
         load_Z = false;
         if ((action == AiGuard.GuardAction.FIGHT || action == AiGuard.GuardAction.FLEE) && !AiThreats.init_Z() && vitals.health() <= 12.0F) {
            AiOperation ward = init_AiOperation();
            if (ward != null) {
               init_Z = false;
               AutismClientAddon.LOG.info("[AI] guard: water at the feet against an angry enderman, health {}", (int)vitals.health());
               return ward;
            }
         }

         boolean striking = !AiThreats.init_Z() && flush();
         boolean swarmed = !AiThreats.init_Z()
            && vitals.hostilesNear() >= 3
            && sync_I >= 1
            && vitals.health() < 14.0F
            && (close_J == 0L || System.nanoTime() - close_J >= 10000000000L);
         if ((action == AiGuard.GuardAction.FIGHT || action == AiGuard.GuardAction.FLEE) && sync_I > 0 && !close_Z && !striking && !swarmed) {
            boolean coverBackoff = open_J != 0L && System.nanoTime() - open_J < 10000000000L;
            boolean hunt = init_I(flush_I) >= 1
               && !AiCombat.init(init_Entity())
               && vitals.health() >= 12.0F
               && (sync_J == 0L || System.nanoTime() - sync_J >= 10000000000L);
            Entity shooter = init_Entity();
            LocalPlayer self = Minecraft.getInstance().player;
            boolean upClose = shooter != null
               && self != null
               && shooter.distanceTo(self) <= 4.5
               && Math.abs(shooter.getY() - self.getY()) <= 3.0
               && !AiCombat.init(shooter);
            boolean blazeShooter = shooter instanceof Blaze;
            if (blazeShooter && !AiThreats.init_Z() && (vitals.health() < 14.0F || AiWorldScan.init_List_Str_D("minecraft:blaze", 16.0).size() > 1)) {
               upClose = false;
            }

            if (policy.flee() && !blockedRetreat && init_I(flush_I) >= 2 && AiCombat.init(shooter)) {
               init_Z = false;
               read_Z = true;
               AutismClientAddon.LOG.info("[AI] cover: repeated cover from an unreachable shooter, leaving its range");
               return init(AiSurvival.load(AiJson.init_JsonObject_ObjArr("radius", 16.0, "distance", 28, "timeoutSeconds", Math.max(15, Math.min(60, budgetSeconds)))));
            }

            boolean blazeCharge = blazeShooter && !AiThreats.init_Z();
            if (init(policy, vitals, upClose, poll_Z || hunt, sync_I) && (!blazeCharge || !(vitals.health() < 12.0F))) {
               init_Z = false;
               AiOperation charge = blazeCharge
                  ? AiBlazeFight.init(AiJson.init_JsonObject_ObjArr("count", 1, "radius", 16.0, "retreatHealth", 8, "timeoutSeconds", 20))
                  : AiCombat.init(
                     AiJson.init_JsonObject_ObjArr(
                        "entityId",
                        flush_I,
                        "count",
                        1,
                        "radius",
                        16.0,
                        "retreatHealth",
                        upClose ? 0.0F : Math.min(policy.fleeHealth(), 4.0F),
                        "timeoutSeconds",
                        20
                     )
                  );
               return upClose && vitals.health() < 8.0F ? init(charge) : charge;
            }

            if (!coverBackoff) {
               init_Z = false;
               load_Z = true;
               init_V(flush_I);
               return init(
                  AiCover.init(AiJson.init_JsonObject_ObjArr("healTo", vitals.health() < 12.0F ? Math.min(vitals.maxHealth(), 16.0F) : 0.0F, "timeoutSeconds", 45))
               );
            }
         }

         boolean lavaBeside = close_Z && !AiThreats.init_Z() && init_Z(5);
         boolean armouredCreeper = close_Z && vitals.hostilesNear() <= 1 && vitals.hasWeapon() && vitals.health() >= 16.0F && init_Z(vitals) && !lavaBeside;
         if (close_Z && !armouredCreeper && (action == AiGuard.GuardAction.FIGHT || action == AiGuard.GuardAction.FLEE)) {
            long now = System.nanoTime();
            init_List.removeIf(t -> now - t > 60000000000L);
            if (init_List.size() >= 3 && vitals.hostilesNear() <= 1 && vitals.hasWeapon() && vitals.health() >= 16.0F && !lavaBeside) {
               armouredCreeper = true;
               AutismClientAddon.LOG.info("[AI] guard: a creeper avoided {} times in a minute; fighting it", init_List.size());
            } else {
               init_List.add(now);
            }
         }

         if (action == AiGuard.GuardAction.FIGHT && close_Z && policy.flee() && !armouredCreeper) {
            action = AiGuard.GuardAction.FLEE;
         }

         if (!AiThreats.init_Z() && (action == AiGuard.GuardAction.FIGHT || action == AiGuard.GuardAction.NONE) && read_Z()) {
            action = policy.flee() ? AiGuard.GuardAction.FLEE : action;
         }

         boolean hide = action == AiGuard.GuardAction.FLEE && (load_Z() || blockedRetreat)
            || action == AiGuard.GuardAction.FIGHT && load_Z() && (sync_I > 0 || open_Z || vitals.hostilesNear() >= 2 || vitals.health() < 12.0F)
            || !AiThreats.init_Z()
               && (action == AiGuard.GuardAction.FIGHT || action == AiGuard.GuardAction.FLEE)
               && vitals.hostilesNear() >= 3
               && sync_I >= 1
               && vitals.health() < 14.0F;
         if (!hide
            && (action == AiGuard.GuardAction.FIGHT || action == AiGuard.GuardAction.FLEE)
            && Minecraft.getInstance().level != null
            && Minecraft.getInstance().level.dimension() == Level.END
            && (vitals.health() <= Math.max(policy.fleeHealth(), 10.0F) || vitals.hostilesNear() >= 2 && vitals.health() < 16.0F)) {
            hide = true;
         }

         if (hide && close_J != 0L && System.nanoTime() - close_J < 10000000000L) {
            hide = false;
         }

         if (hide && !AiThreats.init_Z() && flush()) {
            hide = false;
            if (action == AiGuard.GuardAction.FLEE || action == AiGuard.GuardAction.NONE) {
               action = AiGuard.GuardAction.FIGHT;
            }
         }

         if (hide && close_Z && close_D < 8.0) {
            hide = false;
            if (blockedRetreat && vitals.hasWeapon()) {
               action = AiGuard.GuardAction.FIGHT;
            }
         }

         if (hide && Minecraft.getInstance().player.isInWater()) {
            hide = false;
            if (policy.flee()) {
               action = AiGuard.GuardAction.FLEE;
            }
         }

         init_Z = hide;
         if (hide) {
            flush_J = 0L;
            return init(
               AiBurrow.init(
                  AiJson.init_JsonObject_ObjArr(
                     "healTo",
                     Math.min(vitals.maxHealth(), 16.0F),
                     "stay",
                     load_Z(),
                     "towerOk",
                     Minecraft.getInstance().level.dimension() != Level.END && init(close_Z, open_Z, sync_I, update_Z()),
                     "timeoutSeconds",
                     120
                  )
               )
            );
         }

         read_Z = action == AiGuard.GuardAction.FLEE;
         AiOperation var10000;
         switch (action) {
            case NONE:
               var10000 = null;
               break;
            case FIGHT:
               var10000 = AiCombat.init(
                  AiJson.init_JsonObject_ObjArr(
                     "target",
                     "hostiles",
                     "hold",
                     load_Z() && sync_I == 0 && write_Z(),
                     "count",
                     Math.max(1, vitals.hostilesNear()),
                     "radius",
                     sync_I > 0 ? 16.0 : 9.0,
                     "retreatHealth",
                     vitals.health() <= policy.fleeHealth() ? 0.0F : policy.fleeHealth(),
                     "timeoutSeconds",
                     Math.max(15, Math.min(60, budgetSeconds))
                  )
               );
               break;
            case FLEE:
               var10000 = AiSurvival.load(
                  AiJson.init_JsonObject_ObjArr(
                     "radius", close_Z ? 20 : 14, "distance", close_Z ? 40 : 28, "timeoutSeconds", Math.max(15, Math.min(60, budgetSeconds))
                  )
               );
               break;
            case EAT:
               var10000 = AiSurvival.init(AiJson.init_JsonObject_ObjArr("food", 19, "timeoutSeconds", Math.max(10, Math.min(40, budgetSeconds))));
               break;
            case TIDY:
               var10000 = init(policy);
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         init_Z = false;
         write_Z = true;
         return init(
            AiCombat.init(
               AiJson.init_JsonObject_ObjArr(
                  "target",
                  "hostiles",
                  "untilClear",
                  true,
                  "count",
                  64,
                  "radius",
                  10.0,
                  "perch",
                  3,
                  "retreatHealth",
                  0,
                  "timeoutSeconds",
                  Math.max(20, Math.min(90, budgetSeconds))
               )
            )
         );
      }
   }

   static AiOperation init_AiOperation() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && mc.player.onGround() && !mc.player.isInWater()) {
         if (push != 0L && System.nanoTime() - push < 8000000000L) {
            return null;
         }

         if (!mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES) && AiGameState.init_I_Str("minecraft:water_bucket") > 0) {
            boolean angry = false;

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Enderman man && man.isAlive() && man.isCreepy() && man.distanceTo(mc.player) <= 6.0F) {
                  angry = true;
                  break;
               }
            }

            if (!angry) {
               return null;
            } else {
               BlockPos feet = mc.player.blockPosition();
               if (mc.level.getBlockState(feet).canBeReplaced() && !mc.level.getBlockState(feet.below()).getCollisionShape(mc.level, feet.below()).isEmpty()) {
                  push = System.nanoTime();
                  return AiNetherRun.init_AiOperation_AiOperation_Supplier(
                     AiBucket.init(
                        AiJson.init_JsonObject_ObjArr(
                           "action", "empty", "fluid", "water", "x", feet.getX(), "y", feet.getY(), "z", feet.getZ(), "timeoutSeconds", 5
                        )
                     ),
                     () -> AiNetherRun.init_AiOperation_AiOperation_Supplier(
                        AiCombat.init(100), () -> AiBucket.init(AiJson.init_JsonObject_ObjArr("action", "fill", "fluid", "water", "timeoutSeconds", 5))
                     )
                  );
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static boolean init_Z(int r) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();

         for (BlockPos p : BlockPos.betweenClosed(feet.offset(-r, -3, -r), feet.offset(r, 2, r))) {
            if (mc.level.getFluidState(p).is(FluidTags.LAVA)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean init(AiGuard.GuardPolicy policy, AiGuard.StatusSnapshot vitals, boolean upClose, boolean approachable, int ranged) {
      return policy.fight()
         && vitals.hasWeapon()
         && (upClose && (ranged <= 1 || vitals.health() >= 14.0F) || approachable && ranged == 1 && vitals.hostilesNear() == ranged && vitals.health() >= 8.0F);
   }

   private static AiOperation init(AiGuard.GuardPolicy policy) {
      long now = System.nanoTime();
      if (pop != 0L && now - pop < 60000000000L) {
         return null;
      }

      pop = now;
      JsonArray keep = new JsonArray();
      policy.keep().forEach(keep::add);
      return AiTidy.init(AiJson.init_JsonObject_ObjArr("action", "drop", "freeSlots", 4, "keep", keep));
   }

   static String init_Str(AiGuard.GuardPolicy policy, AiGuard.StatusSnapshot vitals) {
      return init_X_StreamUtil(policy, vitals).reason();
   }

   static JsonObject init_JsonObject_X_BufferHelper(AiGuard.StatusSnapshot vitals, int interruptions, String lastReason) {
      JsonObject out = AiJson.init_JsonObject_ObjArr(
         "health",
         vitals.health(),
         "food",
         vitals.food(),
         "hostilesNear",
         vitals.hostilesNear(),
         "freeSlots",
         vitals.freeSlots(),
         "worstToolUses",
         vitals.worstToolUses() == 2147483647 ? null : vitals.worstToolUses(),
         "interruptions",
         interruptions
      );
      if (!lastReason.isEmpty()) {
         out.addProperty("lastInterruption", lastReason);
      }

      String concern = init_Str(vitals);
      if (!concern.isEmpty()) {
         out.addProperty("concern", concern);
      }

      return out;
   }

   static boolean init(String id, JsonObject args) {
      boolean var10000;
      switch (id) {
         case "game:fish":
         case "game:collect_drops":
         case "game:eat":
         case "game:sleep":
         case "game:flee":
         case "game:breathe":
         case "game:mlg":
            var10000 = true;
            break;
         case "game:farm":
            var10000 = AiJson.init_Str_JsonObject(args, "action", "harvest").trim().equalsIgnoreCase("harvest");
            break;
         case "game:obtain":
            var10000 = !AiJson.init_Z_JsonObject(args, "allowCombat", true) && load(AiJson.init_Str_JsonObject(args, "item", ""));
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   static boolean init(String id) {
      if (!id.equals("game:goto") && !id.equals("game:navigate")) {
         return false;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && mc.level != null) {
         ArrayList<Entity> entities = new ArrayList<>();

         for (Entity e : mc.level.entitiesForRendering()) {
            entities.add(e);
         }

         return AiSurvival.init_List_List(entities, mc.player.position(), 24.0).isEmpty();
      } else {
         return false;
      }
   }

   private static boolean load(String spec) {
      if (spec.isBlank()) {
         return false;
      }

      try {
         AiItems.ItemSpec target = AiItems.load_af_DataUtil(spec);
         return !target.items().isEmpty() && AiItems.init_af_DataUtil("food").items().containsAll(target.items());
      } catch (RuntimeException unresolvable) {
         return false;
      }
   }

   static boolean close() {
      return autismclient.ai.engine.AiLowHealth.init_Z();
   }

   static String init_Str_Str(String activity, float health, float maxHealth) {
      return autismclient.ai.engine.AiLowHealth.init(activity, health, maxHealth);
   }

   static AiOperation init(final AiOperation inner) {
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            autismclient.ai.engine.AiLowHealth.init_V();

            try {
               return inner.tick();
            } finally {
               autismclient.ai.engine.AiLowHealth.load();
            }
         }

         @Override
         public void cancel() {
            inner.cancel();
         }

         @Override
         public boolean canInterruptNow() {
            return inner.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return inner.requiresReconciliationAfterEmergency();
         }
      };
   }

   private static int load_I() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 2147483647;
      }

      int worst = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (!stack.isEmpty() && stack.isDamageableItem()) {
            worst = Math.min(worst, stack.getMaxDamage() - stack.getDamageValue());
         }
      }

      return worst;
   }

   private static boolean open() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && AiSurvival.init(mc.player.getInventory()) >= 0;
   }

   record StatusSnapshot(
      float health, float maxHealth, int food, int hostilesNear, boolean underAttack, boolean hasFood, boolean hasWeapon, int freeSlots, int worstToolUses
   ) {
      StatusSnapshot(float health, float maxHealth, int food, int hostilesNear, boolean underAttack, boolean hasFood, boolean hasWeapon, int freeSlots) {
         this(health, maxHealth, food, hostilesNear, underAttack, hasFood, hasWeapon, freeSlots, 2147483647);
      }
   }

   record GuardPolicy(boolean fight, boolean flee, boolean eat, float engageHealth, float fleeHealth, int eatFood, boolean tidy, Set<String> keep) {
      GuardPolicy(boolean fight, boolean flee, boolean eat, float engageHealth, float fleeHealth, int eatFood) {
         this(fight, flee, eat, engageHealth, fleeHealth, eatFood, false, Set.of());
      }

      static AiGuard.GuardPolicy standard() {
         return new AiGuard.GuardPolicy(true, true, true, 16.0F, 7.0F, 14, true, Set.of());
      }

      AiGuard.GuardPolicy withoutFlee() {
         return new AiGuard.GuardPolicy(this.fight, false, this.eat, this.engageHealth, this.fleeHealth, this.eatFood, this.tidy, this.keep);
      }

      AiGuard.GuardPolicy withoutCombat() {
         return new AiGuard.GuardPolicy(false, this.flee, this.eat, this.engageHealth, this.fleeHealth, this.eatFood, this.tidy, this.keep);
      }

      AiGuard.GuardPolicy withoutMaintenance() {
         return new AiGuard.GuardPolicy(this.fight, this.flee, false, this.engageHealth, this.fleeHealth, this.eatFood, false, this.keep);
      }

      AiGuard.GuardPolicy keeping(Collection<String> ids) {
         Set<String> all = new LinkedHashSet<>(this.keep);
         all.addAll(ids);
         return new AiGuard.GuardPolicy(this.fight, this.flee, this.eat, this.engageHealth, this.fleeHealth, this.eatFood, this.tidy, Set.copyOf(all));
      }
   }

   enum GuardAction {
      NONE,
      FIGHT,
      FLEE,
      EAT,
      TIDY;
   }

   record GuardDecision(AiGuard.GuardAction action, String reason) {
      static final AiGuard.GuardDecision NONE = new AiGuard.GuardDecision(AiGuard.GuardAction.NONE, "");
   }
}
