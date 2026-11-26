package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

final class AiCover {
   static final int init_I = 80;
   static final int load_I = 7;
   static final int read_I = 3;
   static final int write_I = 12;
   static final int update_I = 48;
   static final double init_D = 4.5;
   static final int sync_I = 6;
   static final double load_D = 2.5;
   static final double read_D = 3.0;
   static final double write_D = 3.0;
   static final double update_D = 8.0;
   static final double sync_D = 6.0;
   private static int flush = -1073741824;

   private AiCover() {
   }

   static boolean init(Minecraft mc) {
      return mc.player != null && mc.player.tickCount - flush <= 1;
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(10, Math.min(120, AiJson.init_I_JsonObject(args, "timeoutSeconds", 45)));
         final float healTo = (float)AiJson.init_D_JsonObject(args, "healTo", 0.0);
         final boolean sealOnly = AiJson.init_Z_JsonObject(args, "sealOnly", false);
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object init_Obj = mc.level;
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            int init_I;
            final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer().init_DataPipe();
            final Set<Long> init_Set = new HashSet<>();
            final Set<Integer> load_Set = new HashSet<>();
            AiGoto.Inner_CachePool init_HashUtil_CachePool;
            Vec3 init_Vec3;
            BlockPos init_BlockPos;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            int sync;
            int flush;
            int close;
            AiOperation init_AiOperation;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            String init_Str = "shooter";
            Vec3 load_Vec3;
            boolean update_Z;

            {
               this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.engine.AiAim.Inner_DataUtil.FAST);
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null) {
                  AiCover.flush = mc.player.tickCount;
               }

               if (this.write_Z || mc.player == null || mc.level == null) {
                  return this.init(AiOperation.Status.CANCELLED, "Cover cancelled");
               }

               if (mc.level != this.init_Obj) {
                  return this.init(AiOperation.Status.FAILED, "World changed while taking cover");
               }

               if (System.nanoTime() > this.init_J) {
                  return this.load_I > 0
                     ? this.init(AiOperation.Status.SUCCEEDED, "Hidden at the deadline")
                     : this.load("No cover reached in " + timeoutSeconds + " s");
               }

               if (this.sync > 0) {
                  this.sync--;
               }

               List<Entity> shooters = this.init_List_8();
               if (shooters.isEmpty()) {
                  return this.init(AiOperation.Status.SUCCEEDED, "No shooter or creeper left near");
               }

               Vec3 eye = mc.player.getEyePosition();
               Entity seer = null;
               double seerDistance = 1.7976931348623157E308;
               boolean creeperSees = false;

               for (Entity shooter : shooters) {
                  double d = shooter.distanceTo(mc.player);
                  if (!(d > 16.5)
                     && autismclient.ai.engine.AiSight.init_Z_BlockGetter_3(mc.level, shooter.getEyePosition(), eye, -9223372036854775808L, -9223372036854775808L)) {
                     this.load_Set.add(shooter.getId());
                     if (shooter instanceof Creeper) {
                        creeperSees = true;
                     }

                     if (d < seerDistance) {
                        seerDistance = d;
                        seer = shooter;
                     }
                  }
               }

               if (creeperSees) {
                  this.load_Vec3 = eye;
               }

               Entity striker = this.init_Entity_7();
               if (!sealOnly && striker != null) {
                  this.update();
                  if (this.init_Z && this.init_DataPipe.load_Z()) {
                     return this.init_AiOperation_Result_37();
                  }

                  if (this.init_HashUtil_CachePool == null
                     || !this.init_HashUtil_CachePool.requiresReconciliationAfterEmergency() && autismclient.ai.engine.AiMotionSafety.fill_Z()) {
                     this.write();
                     if (this.init_Z) {
                        this.init_DataPipe.init_V();
                        this.init_Z = false;
                     }

                     this.load();
                     this.load_Z = false;
                     this.init_I = this.init_Z(striker) ? 0 : this.init_I + 1;
                     if (this.init_I > 10) {
                        if (this.load(striker)) {
                           return this.init("Stepping back from the " + BuiltInRegistries.ENTITY_TYPE.getKey(striker.getType()).getPath());
                        }
                     } else {
                        this.read();
                     }

                     return this.init("Defending cover against the " + BuiltInRegistries.ENTITY_TYPE.getKey(striker.getType()).getPath());
                  } else {
                     AiOperation.Result settling = this.init_HashUtil_CachePool.tick();
                     if (settling.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, settling.message());
                     }

                     if (settling.terminal()) {
                        this.write();
                     }

                     return this.init("Settling before defending cover");
                  }
               } else {
                  boolean baitExposed = seer == null
                     && this.load_Vec3 != null
                     && this.init_Z_2(shooters)
                     && (
                        this.load_Vec3.distanceTo(eye) < 6.0
                           || autismclient.ai.engine.AiSight.init_Z_BlockGetter_3(mc.level, this.load_Vec3, eye, -9223372036854775808L, -9223372036854775808L)
                     );
                  if (seer != null || !sealOnly && baitExposed) {
                     this.load_I = 0;
                     this.update();
                     if (seer == null) {
                        seer = this.init_Entity(shooters);
                        seerDistance = seer.distanceTo(mc.player);
                     }

                     this.init_Str = BuiltInRegistries.ENTITY_TYPE.getKey(seer.getType()).getPath();
                     if (seerDistance <= 3.0 && !(seer instanceof Creeper)) {
                        this.write();
                        Entity mount = seer.getVehicle() instanceof Mob m && m.isAlive() ? m : null;
                        Entity melee = this.init_Entity_7();
                        Entity strike = mount != null ? mount : (melee != null && !autismclient.ai.engine.AiSight.init(melee) ? melee : seer);
                        this.init_Z(strike);
                        return this.init("Hitting the " + BuiltInRegistries.ENTITY_TYPE.getKey(strike.getType()).getPath());
                     }

                     if (!(seerDistance <= 4.5)
                        || !(Math.abs(seer.getY() - mc.player.getY()) <= 3.0)
                        || AiCombat.init(seer)
                        || seer instanceof Creeper
                        || this.init_I_2(shooters) > 1
                        || seer instanceof Blaze && !AiThreats.init_Z() && (mc.player.getHealth() < 16.0F || AiWorldScan.init_List_Str_D("minecraft:blaze", 16.0).size() > 1)) {
                        if (this.init_Z) {
                           return this.init_AiOperation_Result_37();
                        }

                        if (this.load_Z) {
                           if (this.write_I < 6 && this.init_Item_5() != null && this.init_BlockPos(seer) != null) {
                              return this.init(seer, "finishing the shield column");
                           }

                           this.load_Z = false;
                        }

                        this.load();
                        if (sealOnly) {
                           return this.init(seer, "no safe walking escape");
                        }

                        if (this.init_HashUtil_CachePool != null
                           && this.init_Vec3 != null
                           && seer.getEyePosition().distanceTo(this.init_Vec3) > 2.5
                           && this.sync == 0) {
                           this.write();
                        }

                        if (this.init_HashUtil_CachePool == null) {
                           if (!mc.player.onGround() && !mc.player.isInWater()) {
                              return this.init("Landing before taking cover");
                           }

                           BlockPos feet = mc.player.blockPosition();
                           List<Vec3> eyes = this.init_List_2(shooters);
                           List<Vec3> keepAway = new ArrayList<>();

                           for (Entity e : shooters) {
                              if (e instanceof Creeper) {
                                 keepAway.add(e.position());
                              }
                           }

                           if (!keepAway.isEmpty() && this.load_Vec3 != null) {
                              eyes.add(this.load_Vec3);
                              keepAway.add(this.load_Vec3);
                           }

                           boolean creeper = !keepAway.isEmpty();
                           List<BlockPos> stances = this.read_I >= 3
                              ? List.of()
                              : autismclient.ai.engine.AiSight.init(
                                 mc.level,
                                 feet,
                                 eyes,
                                 keepAway,
                                 creeper ? 8.0 : 0.0,
                                 creeper,
                                 7 + (creeper ? 7 : 0),
                                 3,
                                 creeper ? 3 : 12,
                                 this.init_Set,
                                 48,
                                 mc.player.isInWater()
                              );
                           if (creeper && stances.isEmpty()) {
                              return this.load("No open ground out of the creeper's sight within 14 blocks");
                           }

                           if (stances.isEmpty()) {
                              return this.init(seer, "no stance out of its sight within 7 blocks");
                           }

                           BlockPos nearest = stances.get(0);
                           boolean wetDig = mc.player.isInWater()
                              && (
                                 !mc.level.getBlockState(nearest).getCollisionShape(mc.level, nearest).isEmpty()
                                    || !mc.level.getBlockState(nearest.above()).getCollisionShape(mc.level, nearest.above()).isEmpty()
                              );
                           boolean far = wetDig || autismclient.ai.engine.AiSight.init(mc.level, feet, nearest) > 3.0;
                           if ((far || this.init_Z_25())
                              && keepAway.isEmpty()
                              && this.write_I < 6
                              && this.init_Item_5() != null
                              && this.init_BlockPos(seer) != null) {
                              return this.init(seer, far ? "the cover is far" : "mobs on the way to cover");
                           }

                           this.update_I++;
                           this.sync = 10;
                           this.init_Vec3 = seer.getEyePosition();
                           this.init_BlockPos = feet;
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] cover: from the {} at {} ({} m), {} stances, nearest {}",
                                 this.init_Str,
                                 seer.blockPosition().toShortString(),
                                 (int)seerDistance,
                                 stances.size(),
                                 stances.get(0).toShortString()
                              );
                           this.init_HashUtil_CachePool = AiGoto.init(
                              feet,
                              0.6,
                              !creeper,
                              false,
                              false,
                              20,
                              autismclient.ai.engine.AiPathfinder.Inner_IoWorker.SAFEST,
                              false,
                              2.0,
                              new autismclient.ai.engine.CoverGoal(stances),
                              new AiGoto.Inner_BufferHelper(true, true, 3, !mc.player.isInWater(), true, true),
                              false
                           );
                        }

                        AiOperation.Result r = this.init_HashUtil_CachePool.tick();
                        if (r.terminal()) {
                           this.write();
                           if (r.status() == AiOperation.Status.SUCCEEDED) {
                              this.init_Set.add(mc.player.blockPosition().asLong());
                           } else {
                              this.read_I++;
                              AutismClientAddon.LOG.info("[AI] cover: route failed ({}): {}", this.read_I, r.message());
                           }
                        }

                        return this.init("Taking cover from the " + this.init_Str + " (" + (int)seerDistance + " m)");
                     } else {
                        return this.init(
                           AiOperation.Status.SUCCEEDED, "The " + this.init_Str + " came within " + (int)Math.ceil(seerDistance) + " blocks: fight it"
                        );
                     }
                  } else {
                     if (sealOnly) {
                        if (this.init_Z) {
                           AiOperation.Result progress = this.init_AiOperation_Result_37();
                           if (this.init_Z) {
                              return progress;
                           }
                        }

                        if (autismclient.ai.engine.AiMotionSafety.fill_Z()) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Escape tunnel sealed out of the creeper's sight");
                        }
                     }

                     this.load();
                     this.load_Z = false;
                     this.load_I++;
                     if (this.init_Z) {
                        this.init_DataPipe.init_V();
                        this.init_Z = false;
                     }

                     if (this.init_HashUtil_CachePool != null) {
                        BlockPos feet = mc.player.blockPosition();
                        if (!mc.player.onGround()
                           || autismclient.ai.engine.AiSight.init_Z_BlockGetter(mc.level, this.init_List_2(shooters), feet.getX(), feet.getY(), feet.getZ())) {
                           AiOperation.Result r = this.init_HashUtil_CachePool.tick();
                           if (r.terminal()) {
                              this.write();
                           }

                           return this.init("In cover from the " + this.init_Str + ", settling in");
                        }

                        this.write();
                     }

                     Entity melee = this.init_Entity_7();
                     if (melee != null) {
                        this.update();
                        this.init_Z(melee);
                     } else if (healTo > 0.0F
                        && mc.player.getHealth() < healTo
                        && (
                           this.init_AiOperation != null
                              || mc.player.getFoodData().getFoodLevel() < 18 && this.close < 3 && AiSurvival.init(mc.player.getInventory()) >= 0
                        )) {
                        if (this.init_AiOperation == null) {
                           this.close++;
                           this.init_AiOperation = AiSurvival.init(AiJson.init_JsonObject_ObjArr("food", 20, "timeoutSeconds", 10));
                        }

                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           return this.init("Eating in cover");
                        }

                        this.init_AiOperation = null;
                     }

                     boolean healing = healTo > 0.0F && mc.player.getHealth() < healTo && mc.player.getFoodData().getFoodLevel() >= 18 && ++this.flush < 1200;
                     return this.load_I >= 80 && !healing
                        ? this.init(AiOperation.Status.SUCCEEDED, "Out of sight of " + this.load_Set.size() + " shooter(s) for " + this.load_I / 20 + " s")
                        : this.init(healing ? "Healing in cover" : "In cover from the " + this.init_Str + " (" + this.load_I / 20 + " s)");
                  }
               }
            }

            AiOperation.Result init_AiOperation_Result_37() {
               this.init_V_29();
               autismclient.ai.engine.AiPlacer.Inner_DataUtil s = this.init_DataPipe.init_DataPipe_DataUtil();
               if (s == autismclient.ai.engine.AiPlacer.Inner_DataUtil.WORKING) {
                  return this.init("Putting a block between the " + this.init_Str + " and me");
               }

               this.init_Z = false;
               if (s == autismclient.ai.engine.AiPlacer.Inner_DataUtil.PLACED) {
                  this.write_I++;
                  AutismClientAddon.LOG
                     .info("[AI] cover: shield block at {} against the {}", this.init_DataPipe.init_BlockPos().toShortString(), this.init_Str);
               } else {
                  AutismClientAddon.LOG.info("[AI] cover: shield placement failed: {}", this.init_DataPipe.load_Str());
                  this.load_Z = false;
                  this.write_I = 6;
               }

               return this.init("Checking the shield");
            }

            int init_I_2(List<Entity> shooters) {
               int n = 0;

               for (Entity e : shooters) {
                  if (e.isAlive() && mc.player.hasLineOfSight(e)) {
                     n++;
                  }
               }

               return n;
            }

            AiOperation.Result init(Entity seer, String why) {
               if (this.write_I >= 6) {
                  return this.load("No cover from the " + this.init_Str + ": " + why + ", and no shield block would go down");
               }

               Item block = this.init_Item_5();
               BlockPos cell = block == null ? null : this.init_BlockPos(seer);
               if (cell == null) {
                  return this.load(
                     "No cover from the " + this.init_Str + ": " + why + (block == null ? ", and no building block to shield with" : ", and no cell to shield")
                  );
               }

               this.init_DataPipe.init_V_BlockPos_Item(cell, block);
               this.init_Z = true;
               this.load_Z = true;
               this.init_V_29();
               return this.init("Putting a block between the " + this.init_Str + " and me");
            }

            void init_V_29() {
               mc.options.keySprint.setDown(false);
               mc.player.setSprinting(false);
               if (mc.player.isInWater()) {
                  mc.options.keyJump.setDown(true);
                  this.read_Z = true;
               } else {
                  this.load();
               }
            }

            void load() {
               if (this.read_Z) {
                  mc.options.keyJump.setDown(false);
                  this.read_Z = false;
               }
            }

            BlockPos init_BlockPos(Entity seer) {
               return AiShield.init(mc, seer);
            }

            Item init_Item_5() {
               return AiShield.init(mc);
            }

            boolean init_Z_25() {
               return AiShield.init_Z(mc, 6.0);
            }

            List<Vec3> init_List_2(List<Entity> shooters) {
               List<Vec3> eyes = new ArrayList<>();

               for (Entity shooter : shooters) {
                  eyes.add(shooter.getEyePosition());
               }

               return eyes;
            }

            List<Entity> init_List_8() {
               List<Entity> found = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity.isAlive() && (autismclient.ai.engine.AiSight.init(entity) || entity instanceof Creeper) && entity.distanceTo(mc.player) <= 20.0) {
                     found.add(entity);
                  }
               }

               found.sort(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)));
               return found;
            }

            boolean init_Z_2(List<Entity> watched) {
               for (Entity e : watched) {
                  if (e instanceof Creeper && e.distanceTo(mc.player) <= 16.0) {
                     return true;
                  }
               }

               return false;
            }

            Entity init_Entity(List<Entity> watched) {
               for (Entity e : watched) {
                  if (e instanceof Creeper) {
                     return e;
                  }
               }

               return watched.get(0);
            }

            Entity init_Entity_7() {
               Entity best = null;
               double bestDistance = 3.0;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Enemy && entity.isAlive() && !(entity instanceof Creeper) && AiThreats.init_Z(mc, entity)) {
                     double d = entity.distanceTo(mc.player);
                     if (d <= bestDistance && mc.player.hasLineOfSight(entity)) {
                        best = entity;
                        bestDistance = d;
                     }
                  }
               }

               return best;
            }

            boolean init_Z(Entity mob) {
               int slot = AiItems.read();
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  autismclient.util.InventorySlotHelper.init_V(mc, slot);
               }

               this.init_DataUtil.init_V_Vec3(autismclient.ai.engine.AiAim.load_Vec3_Entity(mob));
               this.init_DataUtil.load_V();
               boolean onTarget = this.init_DataUtil.load_Z_Entity(mob);
               if (onTarget && mc.player.getAttackStrengthScale(0.0F) >= 0.92F) {
                  AiRunTrace.init_V_Str_JsonObject(
                     "cover_attack",
                     AiJson.init_JsonObject_ObjArr(
                        "targetId",
                        mob.getId(),
                        "targetUuid",
                        mob.getUUID().toString(),
                        "targetType",
                        BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString(),
                        "targetHealth",
                        mob instanceof LivingEntity living ? living.getHealth() : -1,
                        "playerHealth",
                        mc.player.getHealth(),
                        "charge",
                        mc.player.getAttackStrengthScale(0.0F),
                        "position",
                        mc.player.blockPosition().toShortString()
                     )
                  );
                  mc.gameMode.attack(mc.player, mob);
                  Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                  mc.player.resetAttackStrengthTicker();
               }

               return onTarget;
            }

            boolean load(Entity mob) {
               boolean safe = mc.player.onGround() && autismclient.ai.engine.AiCombatMotion.init(mc, -1.0, 0.0, 6) != null;
               mc.options.keyUp.setDown(false);
               mc.options.keyDown.setDown(safe);
               mc.options.keyLeft.setDown(false);
               mc.options.keyRight.setDown(false);
               mc.options.keySprint.setDown(false);
               this.update_Z = safe;
               return safe;
            }

            void read() {
               if (this.update_Z) {
                  this.update_Z = false;
                  mc.options.keyDown.setDown(false);
               }
            }

            void write() {
               if (this.init_HashUtil_CachePool != null) {
                  this.init_HashUtil_CachePool.cancel();
                  this.init_HashUtil_CachePool = null;
               }
            }

            void update() {
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }

            JsonObject init_JsonObject_55() {
               return AiJson.init_JsonObject_ObjArr(
                  "shooters",
                  this.load_Set.size(),
                  "plans",
                  this.update_I,
                  "shields",
                  this.write_I,
                  "hiddenTicks",
                  this.load_I,
                  "startedAt",
                  this.init_BlockPos == null ? "" : this.init_BlockPos.toShortString()
               );
            }

            AiOperation.Result init(String message) {
               return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_55());
            }

            AiOperation.Result load(String message) {
               AiGuard.update_V();
               return this.init(AiOperation.Status.FAILED, message);
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.load();
               this.read();
               this.write();
               this.update();
               if (this.init_Z) {
                  this.init_DataPipe.init_V();
                  this.init_Z = false;
               }

               this.init_DataUtil.init_V();
               return AiGameOperations.init_AiOperation_Result(status, message, this.init_JsonObject_55());
            }

            @Override
            public boolean canInterruptNow() {
               return (!this.init_Z || !this.init_DataPipe.load_Z())
                  && (this.init_HashUtil_CachePool == null || this.init_HashUtil_CachePool.canInterruptNow())
                  && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_Z && this.init_DataPipe.load_Z()
                  || this.init_HashUtil_CachePool != null && this.init_HashUtil_CachePool.requiresReconciliationAfterEmergency()
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               this.load();
               this.read();
               this.write_Z = true;
               this.write();
               this.update();
               this.init_DataPipe.init_V();
               this.init_DataUtil.init_V();
            }
         };
      } else {
         throw new IllegalArgumentException("Taking cover requires a connected living player");
      }
   }
}
