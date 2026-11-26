package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

final class AiSurvival {
   private static final Set<Item> load = Set.of(Items.ROTTEN_FLESH, Items.PUFFERFISH, Items.SPIDER_EYE, Items.POISONOUS_POTATO, Items.CHICKEN);
   private static final Set<Item> read = Set.of(Items.SUSPICIOUS_STEW, Items.CHORUS_FRUIT);
   private static final Set<Item> write = Set.of(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
   static final Set<String> init = Set.of(
      "minecraft:beef", "minecraft:porkchop", "minecraft:chicken", "minecraft:mutton", "minecraft:rabbit", "minecraft:cod", "minecraft:salmon"
   );

   private AiSurvival() {
   }

   static boolean init_Z_D(double distance, boolean swelling, boolean ignited) {
      return distance <= 6.0 && (swelling || ignited);
   }

   static boolean init(int unchangedUseTicks, int consumeTicks) {
      return unchangedUseTicks > Math.max(1L, consumeTicks) + 40L;
   }

   static Entity init_Entity(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         Entity nearest = null;
         double best = 36.0;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Mob mob && mob.isAlive() && !(mob instanceof Ghast) && (mob.getTarget() == null || mob.getTarget() == mc.player)) {
               double distance = mob.distanceToSqr(mc.player);
               if (!(distance >= best) && mc.player.hasLineOfSight(mob)) {
                  boolean engaged = AiThreats.init_Z(mc, mob);
                  if (engaged || AiCombatPriority.init(AiThreats.init_cv_DataUtil(mob)) && !(mob instanceof Piglin)) {
                     nearest = mob;
                     best = distance;
                  }
               }
            }
         }

         return nearest;
      } else {
         return null;
      }
   }

   static AiOperation init_AiOperation(Minecraft mc) {
      return init_AiOperation_Minecraft(mc, AiGuard.Inner_CachePool.standard(), 15);
   }

   static AiOperation init_AiOperation_Minecraft(Minecraft mc, AiGuard.Inner_CachePool policy, int seconds) {
      policy = policy.withoutMaintenance();
      if (mc.level.dimension() != Level.OVERWORLD) {
         policy = policy.withoutFlee();
      }

      AiGuard.Inner_BufferHelper vitals = AiGuard.init_X_BufferHelper();
      Entity threat = init_Entity(mc);
      if (threat != null && AiThreats.init_cv_DataUtil(threat) == AiThreats.Inner_DataUtil.stop) {
         return load(AiJson.init_JsonObject_ObjArr("radius", 12, "distance", 12, "timeoutSeconds", 15));
      }

      if (threat != null && vitals.hostilesNear() == 0) {
         vitals = new AiGuard.Inner_BufferHelper(
            vitals.health(), vitals.maxHealth(), vitals.food(), 1, true, vitals.hasFood(), vitals.hasWeapon(), vitals.freeSlots(), vitals.worstToolUses()
         );
      }

      AiOperation defense = AiGuard.init_AiOperation_X_CachePool(policy, vitals, Math.max(1, Math.min(15, seconds)));
      return defense != null ? defense : load(AiJson.init_JsonObject_ObjArr("radius", 12, "distance", 12, "timeoutSeconds", 15));
   }

   static AiOperation init(JsonObject args) {
      final Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final int target = Math.max(1, Math.min(20, AiJson.init_I_JsonObject(args, "food", 18)));
         final long deadline = System.nanoTime() + Math.min(3600, Math.max(1, AiJson.init_I_JsonObject(args, "timeoutSeconds", 30))) * 1000000000L;
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final Object init_Obj = client.level;
            final Object load_Obj = client.player;
            final Object read_Obj = client.getConnection();
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            int init_I = -1;
            int load_I = -1;
            int read_I = -1;
            int write_I = -1;
            int update;
            int sync;
            boolean init_Z;
            int flush;
            int close;
            int open;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.read_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (this.init_Minecraft.level == this.init_Obj
                     && this.init_Minecraft.player == this.load_Obj
                     && this.init_Minecraft.getConnection() == this.read_Obj) {
                     Inventory inventory = this.init_Minecraft.player.getInventory();
                     int food = this.init_Minecraft.player.getFoodData().getFoodLevel();
                     if (this.init_I < 0) {
                        this.init_I = food;
                        this.load_I = food;
                     }

                     if (food >= target) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Food level target reached");
                     }

                     if (food != this.load_I) {
                        this.load_I = food;
                        this.sync = this.flush = 0;
                     }

                     if (System.nanoTime() > deadline) {
                        return this.init(
                           this.requiresReconciliationAfterEmergency() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                           "Food target not reached before deadline"
                        );
                     }

                     if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu) {
                        if (this.init_Minecraft.player.containerMenu.getCarried().isEmpty() && !this.init_Z) {
                           this.init_Z = true;
                           this.init_Minecraft.player.closeContainer();
                           return this.init("Closing the container to eat");
                        } else {
                           return this.init(AiOperation.Status.FAILED, "Close the open container before eating");
                        }
                     } else {
                        if (this.init_Minecraft.gui.screen() != null) {
                           return this.init(AiOperation.Status.FAILED, "Close the open screen before eating");
                        }

                        if (!this.init_Minecraft.player.containerMenu.getCarried().isEmpty()) {
                           return this.init(AiOperation.Status.FAILED, "Clear the cursor stack before eating");
                        }

                        if (this.write_Z) {
                           this.init_Minecraft.options.keyUse.setDown(false);
                           if (!this.init_Minecraft.player.isUsingItem()) {
                              this.write_Z = false;
                              this.open = this.flush = 0;
                              return this.init("Stalled food use released; rechecking nutrition before restarting");
                           }

                           if (++this.open > 40) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Food use release not confirmed; reobserve nutrition and item use before retrying");
                           }

                           this.init_Minecraft.gameMode.releaseUsingItem(this.init_Minecraft.player);
                           return this.init("Waiting for stalled food use to stop before retrying");
                        } else if (this.init_Minecraft.player.isUsingItem()) {
                           if (!AiSurvival.init(this.init_Minecraft.player.getUseItem())) {
                              return this.init(AiOperation.Status.FAILED, "Another item use is already active; finish or stop it before eating");
                           }

                           if (!AiSurvival.init(++this.flush, this.init_Minecraft.player.getUseItem().getUseDuration(this.init_Minecraft.player))) {
                              this.sync = 0;
                              this.init_Minecraft.options.keyUse.setDown(true);
                              return this.init("Eating (" + food + "/" + target + ")");
                           }

                           if (this.close >= 1) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Food use repeated without observed nutrition; reobserve before retrying");
                           }

                           this.close++;
                           this.write_Z = true;
                           this.init_Minecraft.options.keyUse.setDown(false);
                           this.init_Minecraft.gameMode.releaseUsingItem(this.init_Minecraft.player);
                           return this.init("Releasing stalled food use before one bounded retry");
                        } else if (!this.load_Z) {
                           int slot = AiSurvival.init(inventory);
                           if (slot < 0) {
                              return this.init(AiOperation.Status.FAILED, "No food in inventory");
                           }

                           if (!Inventory.isHotbarSlot(slot)) {
                              AiContainerTransactions.init(this.init_Minecraft.player.containerMenu.containerId, slot, inventory.getSelectedSlot(), ContainerInput.SWAP);
                              this.load_Z = true;
                              return this.init("Moving food to the hotbar");
                           }

                           if (inventory.getSelectedSlot() != slot) {
                              if (this.read_I < 0) {
                                 this.read_I = inventory.getSelectedSlot();
                              }

                              this.write_I = slot;
                              inventory.setSelectedSlot(slot);
                           }

                           this.init_DataUtil.init_V_ce_DataUtil(new autismclient.util.RotationMath.Inner_DataUtil(this.init_Minecraft.player.getYRot(), -89.5F));
                           this.init_DataUtil.load_V();
                           this.init_Minecraft.options.keyUse.setDown(true);
                           return ++this.sync > 100
                              ? this.init(AiOperation.Status.FAILED, "Eating did not start; the item use may have been rejected")
                              : this.init("Starting to eat (" + food + "/" + target + ")");
                        } else if (AiSurvival.init(inventory.getItem(inventory.getSelectedSlot()))) {
                           this.load_Z = false;
                           this.update = 0;
                           return this.init("Food moved to the hotbar");
                        } else {
                           return ++this.update > 60
                              ? this.init(
                                 AiOperation.Status.UNCERTAIN, "Hotbar swap dispatched without an observed inventory change; reobserve before retrying"
                              )
                              : this.init("Moving food to the hotbar");
                        }
                     }
                  } else {
                     return this.init(
                        this.requiresReconciliationAfterEmergency() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED,
                        "Eating world, player or connection changed; reobserve inventory"
                     );
                  }
               } else {
                  return this.init(
                     this.requiresReconciliationAfterEmergency() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED,
                     "Eating cancelled; reobserve pending changes before retrying"
                  );
               }
            }

            AiOperation.Result init(String message) {
               return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init());
            }

            JsonObject init() {
               return AiJson.init_JsonObject_ObjArr(
                  "foodBefore",
                  this.init_I,
                  "food",
                  this.init_Minecraft.player == null ? -1 : this.init_Minecraft.player.getFoodData().getFoodLevel(),
                  "target",
                  target,
                  "useRestarts",
                  this.close,
                  "pendingFoodUseRelease",
                  this.write_Z
               );
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.init_AiOperation_Result(status, message, this.init());
            }

            @Override
            public boolean canInterruptNow() {
               return !this.load_Z;
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.load_Z || this.write_Z;
            }

            @Override
            public void cancel() {
               if (!this.read_Z) {
                  this.read_Z = true;
                  this.init_DataUtil.init_V();
                  if (this.init_Minecraft.options != null) {
                     this.init_Minecraft.options.keyUse.setDown(autismclient.util.KeyMappingControl.init(this.init_Minecraft.options.keyUse).autism$physicalForBody());
                  }

                  if (this.init_Minecraft.player == this.load_Obj && this.init_Minecraft.level == this.init_Obj) {
                     if (this.init_Minecraft.player.isUsingItem() && AiSurvival.init(this.init_Minecraft.player.getUseItem()) && this.init_Minecraft.gameMode != null) {
                        this.init_Minecraft.gameMode.releaseUsingItem(this.init_Minecraft.player);
                     }

                     if (this.write_I >= 0 && this.read_I >= 0 && this.init_Minecraft.player.getInventory().getSelectedSlot() == this.write_I) {
                        this.init_Minecraft.player.getInventory().setSelectedSlot(this.read_I);
                     }
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Eating requires a connected living player");
      }
   }

   static AiOperation load(JsonObject args) {
      final Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final double radius = Math.max(1.0, Math.min(64.0, AiJson.init_D_JsonObject(args, "radius", 12.0)));
         final double distance = Math.max(4.0, Math.min(128.0, AiJson.init_D_JsonObject(args, "distance", 24.0)));
         final long deadline = System.nanoTime() + Math.min(3600, Math.max(1, AiJson.init_I_JsonObject(args, "timeoutSeconds", 60))) * 1000000000L;
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            AiOperation init_AiOperation;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            AiOperation.Result init_AiOperation_Result;
            final Vec3 init_Vec3 = client.player.position();
            int init_I;
            List<BlockPos> init_List;
            int load_I;
            int read_I;
            int write_I;
            boolean write_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  ArrayList<Entity> observed = new ArrayList<>();
                  this.init_Minecraft.level.entitiesForRendering().forEach(observed::add);
                  List<LivingEntity> close = AiSurvival.init_List_List(observed, this.init_Minecraft.player.position(), radius * 1.5);
                  List<Vec3> escapeThreats = close.stream().map(Entity::position).toList();
                  boolean safeHandoff = this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
                  if (close.isEmpty() && safeHandoff) {
                     return this.init(
                        AiOperation.Status.SUCCEEDED,
                        this.init_List == null && this.init_AiOperation == null
                           ? "No threats observed"
                           : "Escaped; no threats within " + (int)Math.ceil(radius * 1.5) + " blocks"
                     );
                  }

                  if (System.nanoTime() > deadline && safeHandoff) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Escape deadline reached with threats still observed");
                  }

                  Creeper immediateBlast = safeHandoff ? AiCombat.init_Creeper(this.init_Minecraft) : null;
                  Float retreatYaw = immediateBlast == null ? null : AiBlastEscape.init(this.init_Minecraft, immediateBlast);
                  if (retreatYaw != null) {
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                     }

                     this.init_AiOperation = null;
                     this.init_AiOperation_Result = null;
                     this.init_List = null;
                     this.load_Z = false;
                     AiBlastEscape.init(this.init_Minecraft, retreatYaw);
                     this.read_Z = true;
                     return AiGameOperations.init_AiOperation_Result(
                        AiOperation.Status.RUNNING,
                        "Immediate safe sprint out of the creeper blast before planning cover",
                        AiJson.init_JsonObject_ObjArr("priority", "creeper-fuse", "escape", "verified-sprint-and-coast", "creeperId", immediateBlast.getId())
                     );
                  } else {
                     if (this.read_Z && this.init_Minecraft.player.onGround()) {
                        this.read_Z = false;

                        for (KeyMapping key : new KeyMapping[]{
                           this.init_Minecraft.options.keyUp,
                           this.init_Minecraft.options.keyDown,
                           this.init_Minecraft.options.keyLeft,
                           this.init_Minecraft.options.keyRight,
                           this.init_Minecraft.options.keySprint
                        }) {
                           key.setDown(false);
                        }
                     }

                     if (safeHandoff && !this.load_Z && AiShield.init(this.init_Minecraft) != null) {
                        for (Entity threat : close) {
                           if (threat instanceof Creeper creeper
                              && AiSurvival.init_Z_D(creeper.distanceTo(this.init_Minecraft.player), creeper.getSwellDir() > 0, creeper.isIgnited())
                              && this.init_Minecraft.player.hasLineOfSight(creeper)
                              && AiShield.init(this.init_Minecraft, creeper) != null) {
                              if (this.init_AiOperation != null) {
                                 this.init_AiOperation.cancel();
                              }

                              this.init_AiOperation_Result = null;
                              this.load_Z = true;
                              AutismClientAddon.LOG
                                 .info("[AI] flee: shielding an igniting creeper at {} blocks", creeper.distanceTo(this.init_Minecraft.player));
                              this.init_AiOperation = AiCover.init(AiJson.init_JsonObject_ObjArr("sealOnly", true, "timeoutSeconds", 10));
                              AiOperation.Result shield = this.init_AiOperation.tick();
                              return shield.terminal() ? this.init(shield.status(), shield.message()) : shield;
                           }
                        }
                     }

                     if (this.init_AiOperation != null) {
                        AiOperation.Result result = this.init_AiOperation_Result == null ? this.init_AiOperation.tick() : this.init_AiOperation_Result;
                        this.init_AiOperation_Result = null;
                        if (!result.terminal()) {
                           return result;
                        }

                        this.init_AiOperation = null;
                        if (this.load_Z) {
                           return this.init(result.status(), result.message());
                        }

                        if (++this.init_I >= 6 && this.init_Minecraft.player.position().distanceTo(this.init_Vec3) < 6.0) {
                           return this.init(AiOperation.Status.FAILED, "Boxed in: " + this.init_I + " legs and still within six blocks of the start");
                        }

                        if (result.status() == AiOperation.Status.SUCCEEDED) {
                           this.init_List = null;
                        } else {
                           this.load_I++;
                        }
                     }

                     if (close.isEmpty()) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Escaped and landed clear of the threats");
                     }

                     if (this.init_List == null) {
                        List<LivingEntity> near = AiSurvival.init_List_List(observed, this.init_Minecraft.player.position(), radius);
                        ArrayList<Vec3> positions = new ArrayList<>();

                        for (LivingEntity threat : near.isEmpty() ? close : near) {
                           positions.add(threat.position());
                        }

                        this.write_I = positions.size();
                        this.write_Z = false;

                        for (AiProjectiles.Inner_StreamUtil t : AiProjectiles.init(this.init_Minecraft)) {
                           if (AiProjectiles.init(t)) {
                              this.write_Z = true;
                              break;
                           }
                        }

                        double primary = this.write_Z ? AiSurvival.init_D(this.read_I++) : 0.0;
                        double legDistance = Math.min(6.0, distance);
                        double[] fractions = new double[]{1.0, 0.75, 0.5, 0.25};
                        this.init_List = new ArrayList<>();

                        for (double bearing : new double[]{0.0, -45.0, 45.0, -90.0, 90.0}) {
                           for (double fraction : fractions) {
                              this.init_List
                                 .add(
                                    BlockPos.containing(
                                       AiSurvival.init_Vec3_Vec3(this.init_Minecraft.player.position(), positions, legDistance * fraction, primary + bearing)
                                    )
                                 );
                           }
                        }

                        ArrayList<Vec3> routeThreats = new ArrayList<>();

                        for (LivingEntity threat : AiSurvival.init_List_List(observed, this.init_Minecraft.player.position(), distance + radius)) {
                           routeThreats.add(threat.position());
                        }

                        Vec3 start = this.init_Minecraft.player.position();
                        this.init_List.sort(Comparator.comparingDouble(p -> AiSurvival.init_D_Vec3(start, Vec3.atBottomCenterOf(p), routeThreats)));
                        this.load_I = 0;
                     }

                     BlockPos target = null;

                     while (this.load_I < this.init_List.size()) {
                        BlockPos candidate = this.init_List.get(this.load_I);
                        BlockPos grounded = AiGoto.init_BlockPos(candidate, 2);
                        if (grounded != null
                           && !grounded.equals(this.init_Minecraft.player.blockPosition())
                           && AiSurvival.init_Z_Vec3(this.init_Minecraft.player.position(), Vec3.atBottomCenterOf(grounded), escapeThreats)
                           && AiGoto.init_Z(grounded)
                           && (
                              AiCombat.init_Z_Minecraft_BlockPos(this.init_Minecraft, grounded)
                                 || this.init_Minecraft.level.getFluidState(grounded).is(FluidTags.WATER)
                           )) {
                           target = grounded;
                           break;
                        }

                        this.load_I++;
                     }

                     boolean shaft = false;
                     if (target == null && autismclient.ai.engine.AiMotionSafety.fill_Z() && !this.init_Minecraft.player.isInWater()) {
                        target = AiSurvival.init_BlockPos_BlockPos(
                           this.init_Minecraft.player.blockPosition(),
                           escapeThreats,
                           AiGuard.init_I(),
                           cell -> this.init_Minecraft.level.getChunkSource().getChunk(cell.getX() >> 4, cell.getZ() >> 4, ChunkStatus.FULL, false) != null
                              && this.init_Minecraft.level.getBlockState(cell).getCollisionShape(this.init_Minecraft.level, cell).isEmpty()
                              && this.init_Minecraft.level.getFluidState(cell).isEmpty()
                        );
                        shaft = target != null;
                        if (shaft) {
                           AutismClientAddon.LOG.info("[AI] flee: ascending the cleared shaft to {} above threats below", target.toShortString());
                        }
                     }

                     if (target == null) {
                        Entity creeper = close.stream()
                           .filter(entity -> entity instanceof Creeper)
                           .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this.init_Minecraft.player)))
                           .orElse(null);
                        if (creeper != null && AiShield.init(this.init_Minecraft) != null && AiShield.init(this.init_Minecraft, creeper) != null) {
                           this.load_Z = true;
                           this.init_AiOperation = AiCover.init(AiJson.init_JsonObject_ObjArr("sealOnly", true, "timeoutSeconds", 10));
                           AiOperation.Result first = this.init_AiOperation.tick();
                           return first.terminal() ? this.init(first.status(), first.message()) : first;
                        } else {
                           return this.init(
                              AiOperation.Status.FAILED, "No safe escape direction; every bearing ends at lava, a drop, submersion or unloaded terrain"
                           );
                        }
                     } else {
                        int remaining = (int)Math.max(1L, (deadline - System.nanoTime()) / 1000000000L);
                        this.init_AiOperation = AiGoto.init(
                           target,
                           0.6,
                           false,
                           shaft,
                           false,
                           Math.min(shaft ? 5 : 3, remaining),
                           autismclient.ai.engine.AiPathfinder.Inner_IoWorker.load,
                           shaft,
                           2.0,
                           AiSurvival.init(this.init_Minecraft.player.blockPosition(), target)
                        );
                        AiOperation.Result first = this.init_AiOperation.tick();
                        if (!first.terminal()) {
                           return first;
                        }

                        this.init_AiOperation_Result = first;
                        return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, "Re-evaluating escape direction", this.init());
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Flee cancelled");
               }
            }

            JsonObject init() {
               return AiJson.init_JsonObject_ObjArr("radius", radius, "distance", distance);
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.init_AiOperation_Result(status, message, this.init());
            }

            @Override
            public boolean canInterruptNow() {
               return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               this.init_Z = true;
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Fleeing requires a connected living player");
      }
   }

   static boolean init(ItemStack stack) {
      return !stack.isEmpty() && stack.has(DataComponents.FOOD) && stack.has(DataComponents.CONSUMABLE);
   }

   static boolean init_Z(int food) {
      if (food <= 6) {
         return true;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         Inventory inventory = mc.player.getInventory();

         for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD) && !init.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   static int init(Inventory inventory) {
      for (int tier = 0; tier < 3; tier++) {
         for (int region = 0; region < 2; region++) {
            int best = -1;
            double bestScore = -1.0;

            for (int i = region == 0 ? 0 : 9; i < (region == 0 ? 9 : 36); i++) {
               ItemStack stack = inventory.getItem(i);
               if (init(stack) && !read.contains(stack.getItem())) {
                  boolean precious = write.contains(stack.getItem());
                  boolean harmful = load.contains(stack.getItem());
                  if ((tier != 0 || !precious && !harmful) && (tier != 1 || precious) && (tier != 2 || harmful)) {
                     FoodProperties food = stack.get(DataComponents.FOOD);
                     double score = food.nutrition() + food.saturation();
                     if (score > bestScore) {
                        bestScore = score;
                        best = i;
                     }
                  }
               }
            }

            if (best >= 0) {
               return best;
            }
         }
      }

      return -1;
   }

   static Vec3 init_Vec3_Vec3(Vec3 playerPos, List<Vec3> threatPositions, double distance) {
      return init_Vec3_Vec3(playerPos, threatPositions, distance, 0.0);
   }

   static Vec3 init_Vec3_Vec3(Vec3 playerPos, List<Vec3> threatPositions, double distance, double bearingDegrees) {
      if (threatPositions.isEmpty()) {
         return null;
      }

      double x = 0.0;
      double z = 0.0;

      for (Vec3 threat : threatPositions) {
         x += threat.x;
         z += threat.z;
      }

      Vec3 away = new Vec3(playerPos.x - x / threatPositions.size(), 0.0, playerPos.z - z / threatPositions.size());
      double horizontal = Math.hypot(away.x, away.z);
      away = horizontal < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : away.scale(1.0 / horizontal);
      double radians = Math.toRadians(bearingDegrees);
      double cos = Math.cos(radians);
      double sin = Math.sin(radians);
      Vec3 rotated = new Vec3(away.x * cos - away.z * sin, 0.0, away.x * sin + away.z * cos);
      return playerPos.add(rotated.scale(distance));
   }

   static double init_D(int leg) {
      return leg % 2 == 0 ? 22.5 : -22.5;
   }

   static autismclient.ai.engine.AiPathfinder.Inner_JsonUtil init(final BlockPos start, BlockPos target) {
      final autismclient.ai.engine.AiPathfinder.Inner_JsonUtil near = autismclient.ai.engine.AiPathfinder.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(target, 0.6);
      return new autismclient.ai.engine.AiPathfinder.Inner_JsonUtil() {
         @Override
         public boolean isInGoal(int x, int y, int z) {
            return near.isInGoal(x, y, z);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return near.heuristic(x, y, z);
         }

         @Override
         public boolean init_Z(BlockPos cell) {
            double dx = cell.getX() - start.getX();
            double dz = cell.getZ() - start.getZ();
            return dx * dx + dz * dz <= 64.0 && Math.abs(cell.getY() - start.getY()) <= 3;
         }
      };
   }

   static boolean init_Z_Vec3(Vec3 start, Vec3 end, List<Vec3> threats) {
      if (start.distanceToSqr(end) < 1.0) {
         return false;
      }

      Vec3 away = init_Vec3_Vec3(start, threats, 1.0);
      return away != null && (end.x - start.x) * (away.x - start.x) + (end.z - start.z) * (away.z - start.z) >= -0.05;
   }

   static BlockPos init_BlockPos_BlockPos(BlockPos feet, List<Vec3> threats, int blocks, Predicate<BlockPos> clear) {
      if (blocks > 0 && !threats.isEmpty()) {
         for (Vec3 threat : threats) {
            if (threat.y > feet.getY() - 4) {
               return null;
            }
         }

         int rise = 0;
         int up = 1;

         while (up <= Math.min(3, blocks) && clear.test(feet.above(up)) && clear.test(feet.above(up + 1))) {
            rise = up++;
         }

         return rise == 0 ? null : feet.above(rise);
      } else {
         return null;
      }
   }

   static double init_D_Vec3(Vec3 start, Vec3 end, List<Vec3> threats) {
      double dx = end.x - start.x;
      double dz = end.z - start.z;
      double lengthSq = dx * dx + dz * dz;
      double risk = 0.0;

      for (Vec3 mob : threats) {
         if (!(Math.abs(mob.y - start.y) > 3.0)) {
            double t = lengthSq < 1.0E-6 ? 0.0 : Math.max(0.0, Math.min(1.0, ((mob.x - start.x) * dx + (mob.z - start.z) * dz) / lengthSq));
            double closest = Math.hypot(start.x + t * dx - mob.x, start.z + t * dz - mob.z);
            double before = Math.hypot(start.x - mob.x, start.z - mob.z);
            if (closest < before - 0.1) {
               risk += Math.pow(Math.max(0.0, 12.0 - closest), 2.0);
            }

            risk += Math.pow(Math.max(0.0, 12.0 - Math.hypot(end.x - mob.x, end.z - mob.z)), 2.0);
         }
      }

      return risk;
   }

   static List<LivingEntity> init_List_List(List<Entity> entities, Vec3 playerPos, double radius) {
      ArrayList<LivingEntity> result = new ArrayList<>();
      double limit = radius * radius;

      for (Entity entity : entities) {
         if (entity instanceof Enemy && entity instanceof LivingEntity living && living.isAlive() && living.position().distanceToSqr(playerPos) <= limit) {
            result.add(living);
         }
      }

      return result;
   }
}
