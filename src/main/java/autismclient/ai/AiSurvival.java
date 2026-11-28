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
   private static final Set<Item> HARMFUL_FOODS = Set.of(Items.ROTTEN_FLESH, Items.PUFFERFISH, Items.SPIDER_EYE, Items.POISONOUS_POTATO, Items.CHICKEN);
   private static final Set<Item> NEVER_EAT = Set.of(Items.SUSPICIOUS_STEW, Items.CHORUS_FRUIT);
   private static final Set<Item> PRECIOUS_FOODS = Set.of(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
   static final Set<String> IGNORED_FOOD_IDS = Set.of(
      "minecraft:beef", "minecraft:porkchop", "minecraft:chicken", "minecraft:mutton", "minecraft:rabbit", "minecraft:cod", "minecraft:salmon"
   );

   private AiSurvival() {
   }

   static boolean creeperAboutToBlow(double distance, boolean swelling, boolean ignited) {
      return distance <= 6.0 && (swelling || ignited);
   }

   static boolean isUseStalled(int unchangedUseTicks, int consumeTicks) {
      return unchangedUseTicks > Math.max(1L, consumeTicks) + 40L;
   }

   static Entity nearestThreat(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         Entity nearest = null;
         double best = 36.0;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Mob mob && mob.isAlive() && !(mob instanceof Ghast) && (mob.getTarget() == null || mob.getTarget() == mc.player)) {
               double distance = mob.distanceToSqr(mc.player);
               if (!(distance >= best) && mc.player.hasLineOfSight(mob)) {
                  boolean engaged = AiThreats.isActiveThreat(mc, mob);
                  if (engaged || AiCombatPriority.init(AiThreats.classify(mob)) && !(mob instanceof Piglin)) {
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

   static AiOperation defend(Minecraft mc) {
      return defend(mc, AiGuard.GuardPolicy.standard(), 15);
   }

   static AiOperation defend(Minecraft mc, AiGuard.GuardPolicy policy, int seconds) {
      policy = policy.withoutMaintenance();
      if (mc.level.dimension() != Level.OVERWORLD) {
         policy = policy.withoutFlee();
      }

      AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
      Entity threat = nearestThreat(mc);
      if (threat != null && AiThreats.classify(threat) == AiThreats.ThreatKind.AVOID) {
         return load(AiJson.objectOf("radius", 12, "distance", 12, "timeoutSeconds", 15));
      }

      if (threat != null && vitals.hostilesNear() == 0) {
         vitals = new AiGuard.StatusSnapshot(
            vitals.health(), vitals.maxHealth(), vitals.food(), 1, true, vitals.hasFood(), vitals.hasWeapon(), vitals.freeSlots(), vitals.worstToolUses()
         );
      }

      AiOperation defense = AiGuard.createResponseOperation(policy, vitals, Math.max(1, Math.min(15, seconds)));
      return defense != null ? defense : load(AiJson.objectOf("radius", 12, "distance", 12, "timeoutSeconds", 15));
   }

   static AiOperation init(JsonObject args) {
      final Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final int target = Math.max(1, Math.min(20, AiJson.getInt(args, "food", 18)));
         final long deadline = System.nanoTime() + Math.min(3600, Math.max(1, AiJson.getInt(args, "timeoutSeconds", 30))) * 1000000000L;
         return new AiOperation() {
            final Minecraft mc = Minecraft.getInstance();
            final Object startLevel = client.level;
            final Object startPlayer = client.player;
            final Object startConnection = client.getConnection();
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            int foodBefore = -1;
            int lastFood = -1;
            int originalSlot = -1;
            int foodSlot = -1;
            int swapWaitTicks;
            int startWaitTicks;
            boolean closedContainer;
            int useTicks;
            int useRestarts;
            int releaseWaitTicks;
            boolean swapPending;
            boolean cancelled;
            boolean releasePending;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && this.mc.player != null && this.mc.level != null) {
                  if (this.mc.level == this.startLevel
                     && this.mc.player == this.startPlayer
                     && this.mc.getConnection() == this.startConnection) {
                     Inventory inventory = this.mc.player.getInventory();
                     int food = this.mc.player.getFoodData().getFoodLevel();
                     if (this.foodBefore < 0) {
                        this.foodBefore = food;
                        this.lastFood = food;
                     }

                     if (food >= target) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Food level target reached");
                     }

                     if (food != this.lastFood) {
                        this.lastFood = food;
                        this.startWaitTicks = this.useTicks = 0;
                     }

                     if (System.nanoTime() > deadline) {
                        return this.finish(
                           this.requiresReconciliationAfterEmergency() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                           "Food target not reached before deadline"
                        );
                     }

                     if (this.mc.player.containerMenu != this.mc.player.inventoryMenu) {
                        if (this.mc.player.containerMenu.getCarried().isEmpty() && !this.closedContainer) {
                           this.closedContainer = true;
                           this.mc.player.closeContainer();
                           return this.running("Closing the container to eat");
                        } else {
                           return this.finish(AiOperation.Status.FAILED, "Close the open container before eating");
                        }
                     } else {
                        if (this.mc.gui.screen() != null) {
                           return this.finish(AiOperation.Status.FAILED, "Close the open screen before eating");
                        }

                        if (!this.mc.player.containerMenu.getCarried().isEmpty()) {
                           return this.finish(AiOperation.Status.FAILED, "Clear the cursor stack before eating");
                        }

                        if (this.releasePending) {
                           this.mc.options.keyUse.setDown(false);
                           if (!this.mc.player.isUsingItem()) {
                              this.releasePending = false;
                              this.releaseWaitTicks = this.useTicks = 0;
                              return this.running("Stalled food use released; rechecking nutrition before restarting");
                           }

                           if (++this.releaseWaitTicks > 40) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "Food use release not confirmed; reobserve nutrition and item use before retrying");
                           }

                           this.mc.gameMode.releaseUsingItem(this.mc.player);
                           return this.running("Waiting for stalled food use to stop before retrying");
                        } else if (this.mc.player.isUsingItem()) {
                           if (!AiSurvival.isEdible(this.mc.player.getUseItem())) {
                              return this.finish(AiOperation.Status.FAILED, "Another item use is already active; finish or stop it before eating");
                           }

                           if (!AiSurvival.isUseStalled(++this.useTicks, this.mc.player.getUseItem().getUseDuration(this.mc.player))) {
                              this.startWaitTicks = 0;
                              this.mc.options.keyUse.setDown(true);
                              return this.running("Eating (" + food + "/" + target + ")");
                           }

                           if (this.useRestarts >= 1) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "Food use repeated without observed nutrition; reobserve before retrying");
                           }

                           this.useRestarts++;
                           this.releasePending = true;
                           this.mc.options.keyUse.setDown(false);
                           this.mc.gameMode.releaseUsingItem(this.mc.player);
                           return this.running("Releasing stalled food use before one bounded retry");
                        } else if (!this.swapPending) {
                           int slot = AiSurvival.init(inventory);
                           if (slot < 0) {
                              return this.finish(AiOperation.Status.FAILED, "No food in inventory");
                           }

                           if (!Inventory.isHotbarSlot(slot)) {
                              AiContainerTransactions.sendPredictedClick(this.mc.player.containerMenu.containerId, slot, inventory.getSelectedSlot(), ContainerInput.SWAP);
                              this.swapPending = true;
                              return this.running("Moving food to the hotbar");
                           }

                           if (inventory.getSelectedSlot() != slot) {
                              if (this.originalSlot < 0) {
                                 this.originalSlot = inventory.getSelectedSlot();
                              }

                              this.foodSlot = slot;
                              inventory.setSelectedSlot(slot);
                           }

                           this.aim.setTargetRotation(new autismclient.util.RotationMath.Rotation(this.mc.player.getYRot(), -89.5F));
                           this.aim.tick();
                           this.mc.options.keyUse.setDown(true);
                           return ++this.startWaitTicks > 100
                              ? this.finish(AiOperation.Status.FAILED, "Eating did not start; the item use may have been rejected")
                              : this.running("Starting to eat (" + food + "/" + target + ")");
                        } else if (AiSurvival.isEdible(inventory.getItem(inventory.getSelectedSlot()))) {
                           this.swapPending = false;
                           this.swapWaitTicks = 0;
                           return this.running("Food moved to the hotbar");
                        } else {
                           return ++this.swapWaitTicks > 60
                              ? this.finish(
                                 AiOperation.Status.UNCERTAIN, "Hotbar swap dispatched without an observed inventory change; reobserve before retrying"
                              )
                              : this.running("Moving food to the hotbar");
                        }
                     }
                  } else {
                     return this.finish(
                        this.requiresReconciliationAfterEmergency() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED,
                        "Eating world, player or connection changed; reobserve inventory"
                     );
                  }
               } else {
                  return this.finish(
                     this.requiresReconciliationAfterEmergency() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.CANCELLED,
                     "Eating cancelled; reobserve pending changes before retrying"
                  );
               }
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
            }

            JsonObject progressJson() {
               return AiJson.objectOf(
                  "foodBefore",
                  this.foodBefore,
                  "food",
                  this.mc.player == null ? -1 : this.mc.player.getFoodData().getFoodLevel(),
                  "target",
                  target,
                  "useRestarts",
                  this.useRestarts,
                  "pendingFoodUseRelease",
                  this.releasePending
               );
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(status, message, this.progressJson());
            }

            @Override
            public boolean canInterruptNow() {
               return !this.swapPending;
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.swapPending || this.releasePending;
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.aim.clear();
                  if (this.mc.options != null) {
                     this.mc.options.keyUse.setDown(autismclient.util.KeyMappingControl.of(this.mc.options.keyUse).autism$physicalForBody());
                  }

                  if (this.mc.player == this.startPlayer && this.mc.level == this.startLevel) {
                     if (this.mc.player.isUsingItem() && AiSurvival.isEdible(this.mc.player.getUseItem()) && this.mc.gameMode != null) {
                        this.mc.gameMode.releaseUsingItem(this.mc.player);
                     }

                     if (this.foodSlot >= 0 && this.originalSlot >= 0 && this.mc.player.getInventory().getSelectedSlot() == this.foodSlot) {
                        this.mc.player.getInventory().setSelectedSlot(this.originalSlot);
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
         final double radius = Math.max(1.0, Math.min(64.0, AiJson.getDouble(args, "radius", 12.0)));
         final double distance = Math.max(4.0, Math.min(128.0, AiJson.getDouble(args, "distance", 24.0)));
         final long deadline = System.nanoTime() + Math.min(3600, Math.max(1, AiJson.getInt(args, "timeoutSeconds", 60))) * 1000000000L;
         return new AiOperation() {
            final Minecraft mc = Minecraft.getInstance();
            AiOperation leg;
            boolean cancelled;
            boolean shielding;
            boolean blastSprint;
            AiOperation.Result pendingResult;
            final Vec3 origin = client.player.position();
            int legs;
            List<BlockPos> candidates;
            int candidateIndex;
            int projectileDodges;
            int unusedThreatCount;
            boolean projectileIncoming;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && this.mc.player != null && this.mc.level != null) {
                  ArrayList<Entity> observed = new ArrayList<>();
                  this.mc.level.entitiesForRendering().forEach(observed::add);
                  List<LivingEntity> close = AiSurvival.livingWithin(observed, this.mc.player.position(), radius * 1.5);
                  List<Vec3> escapeThreats = close.stream().map(Entity::position).toList();
                  boolean safeHandoff = this.leg == null || this.leg.canInterruptNow();
                  if (close.isEmpty() && safeHandoff) {
                     return this.finish(
                        AiOperation.Status.SUCCEEDED,
                        this.candidates == null && this.leg == null
                           ? "No threats observed"
                           : "Escaped; no threats within " + (int)Math.ceil(radius * 1.5) + " blocks"
                     );
                  }

                  if (System.nanoTime() > deadline && safeHandoff) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Escape deadline reached with threats still observed");
                  }

                  Creeper immediateBlast = safeHandoff ? AiCombat.mostDangerousCreeper(this.mc) : null;
                  Float retreatYaw = immediateBlast == null ? null : AiBlastEscape.init(this.mc, immediateBlast);
                  if (retreatYaw != null) {
                     if (this.leg != null) {
                        this.leg.cancel();
                     }

                     this.leg = null;
                     this.pendingResult = null;
                     this.candidates = null;
                     this.shielding = false;
                     AiBlastEscape.init(this.mc, retreatYaw);
                     this.blastSprint = true;
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING,
                        "Immediate safe sprint out of the creeper blast before planning cover",
                        AiJson.objectOf("priority", "creeper-fuse", "escape", "verified-sprint-and-coast", "creeperId", immediateBlast.getId())
                     );
                  } else {
                     if (this.blastSprint && this.mc.player.onGround()) {
                        this.blastSprint = false;

                        for (KeyMapping key : new KeyMapping[]{
                           this.mc.options.keyUp,
                           this.mc.options.keyDown,
                           this.mc.options.keyLeft,
                           this.mc.options.keyRight,
                           this.mc.options.keySprint
                        }) {
                           key.setDown(false);
                        }
                     }

                     if (safeHandoff && !this.shielding && AiShield.init(this.mc) != null) {
                        for (Entity threat : close) {
                           if (threat instanceof Creeper creeper
                              && AiSurvival.creeperAboutToBlow(creeper.distanceTo(this.mc.player), creeper.getSwellDir() > 0, creeper.isIgnited())
                              && this.mc.player.hasLineOfSight(creeper)
                              && AiShield.init(this.mc, creeper) != null) {
                              if (this.leg != null) {
                                 this.leg.cancel();
                              }

                              this.pendingResult = null;
                              this.shielding = true;
                              AutismClientAddon.LOG
                                 .info("[AI] flee: shielding an igniting creeper at {} blocks", creeper.distanceTo(this.mc.player));
                              this.leg = AiCover.init(AiJson.objectOf("sealOnly", true, "timeoutSeconds", 10));
                              AiOperation.Result shield = this.leg.tick();
                              return shield.terminal() ? this.finish(shield.status(), shield.message()) : shield;
                           }
                        }
                     }

                     if (this.leg != null) {
                        AiOperation.Result result = this.pendingResult == null ? this.leg.tick() : this.pendingResult;
                        this.pendingResult = null;
                        if (!result.terminal()) {
                           return result;
                        }

                        this.leg = null;
                        if (this.shielding) {
                           return this.finish(result.status(), result.message());
                        }

                        if (++this.legs >= 6 && this.mc.player.position().distanceTo(this.origin) < 6.0) {
                           return this.finish(AiOperation.Status.FAILED, "Boxed in: " + this.legs + " legs and still within six blocks of the start");
                        }

                        if (result.status() == AiOperation.Status.SUCCEEDED) {
                           this.candidates = null;
                        } else {
                           this.candidateIndex++;
                        }
                     }

                     if (close.isEmpty()) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Escaped and landed clear of the threats");
                     }

                     if (this.candidates == null) {
                        List<LivingEntity> near = AiSurvival.livingWithin(observed, this.mc.player.position(), radius);
                        ArrayList<Vec3> positions = new ArrayList<>();

                        for (LivingEntity threat : near.isEmpty() ? close : near) {
                           positions.add(threat.position());
                        }

                        this.unusedThreatCount = positions.size();
                        this.projectileIncoming = false;

                        for (AiProjectiles.IncomingProjectile t : AiProjectiles.init(this.mc)) {
                           if (AiProjectiles.init(t)) {
                              this.projectileIncoming = true;
                              break;
                           }
                        }

                        double primary = this.projectileIncoming ? AiSurvival.dodgeBearing(this.projectileDodges++) : 0.0;
                        double legDistance = Math.min(6.0, distance);
                        double[] fractions = new double[]{1.0, 0.75, 0.5, 0.25};
                        this.candidates = new ArrayList<>();

                        for (double bearing : new double[]{0.0, -45.0, 45.0, -90.0, 90.0}) {
                           for (double fraction : fractions) {
                              this.candidates
                                 .add(
                                    BlockPos.containing(
                                       AiSurvival.awayFrom(this.mc.player.position(), positions, legDistance * fraction, primary + bearing)
                                    )
                                 );
                           }
                        }

                        ArrayList<Vec3> routeThreats = new ArrayList<>();

                        for (LivingEntity threat : AiSurvival.livingWithin(observed, this.mc.player.position(), distance + radius)) {
                           routeThreats.add(threat.position());
                        }

                        Vec3 start = this.mc.player.position();
                        this.candidates.sort(Comparator.comparingDouble(p -> AiSurvival.routeRisk(start, Vec3.atBottomCenterOf(p), routeThreats)));
                        this.candidateIndex = 0;
                     }

                     BlockPos target = null;

                     while (this.candidateIndex < this.candidates.size()) {
                        BlockPos candidate = this.candidates.get(this.candidateIndex);
                        BlockPos grounded = AiGoto.findStandableNearCompat(candidate, 2);
                        if (grounded != null
                           && !grounded.equals(this.mc.player.blockPosition())
                           && AiSurvival.movesAway(this.mc.player.position(), Vec3.atBottomCenterOf(grounded), escapeThreats)
                           && AiGoto.isStandableCompat(grounded)
                           && (
                              AiCombat.isSafeStep(this.mc, grounded)
                                 || this.mc.level.getFluidState(grounded).is(FluidTags.WATER)
                           )) {
                           target = grounded;
                           break;
                        }

                        this.candidateIndex++;
                     }

                     boolean shaft = false;
                     if (target == null && autismclient.ai.engine.AiMotionSafety.isPlayerSupported() && !this.mc.player.isInWater()) {
                        target = AiSurvival.shaftClimbTarget(
                           this.mc.player.blockPosition(),
                           escapeThreats,
                           AiGuard.countPerchBlocks(),
                           cell -> this.mc.level.getChunkSource().getChunk(cell.getX() >> 4, cell.getZ() >> 4, ChunkStatus.FULL, false) != null
                              && this.mc.level.getBlockState(cell).getCollisionShape(this.mc.level, cell).isEmpty()
                              && this.mc.level.getFluidState(cell).isEmpty()
                        );
                        shaft = target != null;
                        if (shaft) {
                           AutismClientAddon.LOG.info("[AI] flee: ascending the cleared shaft to {} above threats below", target.toShortString());
                        }
                     }

                     if (target == null) {
                        Entity creeper = close.stream()
                           .filter(entity -> entity instanceof Creeper)
                           .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this.mc.player)))
                           .orElse(null);
                        if (creeper != null && AiShield.init(this.mc) != null && AiShield.init(this.mc, creeper) != null) {
                           this.shielding = true;
                           this.leg = AiCover.init(AiJson.objectOf("sealOnly", true, "timeoutSeconds", 10));
                           AiOperation.Result first = this.leg.tick();
                           return first.terminal() ? this.finish(first.status(), first.message()) : first;
                        } else {
                           return this.finish(
                              AiOperation.Status.FAILED, "No safe escape direction; every bearing ends at lava, a drop, submersion or unloaded terrain"
                           );
                        }
                     } else {
                        int remaining = (int)Math.max(1L, (deadline - System.nanoTime()) / 1000000000L);
                        this.leg = AiGoto.travelToCompat(
                           target,
                           0.6,
                           false,
                           shaft,
                           false,
                           Math.min(shaft ? 5 : 3, remaining),
                           autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                           shaft,
                           2.0,
                           AiSurvival.init(this.mc.player.blockPosition(), target)
                        );
                        AiOperation.Result first = this.leg.tick();
                        if (!first.terminal()) {
                           return first;
                        }

                        this.pendingResult = first;
                        return AiGameOperations.result(AiOperation.Status.RUNNING, "Re-evaluating escape direction", this.progressJson());
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Flee cancelled");
               }
            }

            JsonObject progressJson() {
               return AiJson.objectOf("radius", radius, "distance", distance);
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(status, message, this.progressJson());
            }

            @Override
            public boolean canInterruptNow() {
               return this.leg == null || this.leg.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.leg != null && this.leg.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               this.cancelled = true;
               if (this.leg != null) {
                  this.leg.cancel();
                  this.leg = null;
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Fleeing requires a connected living player");
      }
   }

   static boolean isEdible(ItemStack stack) {
      return !stack.isEmpty() && stack.has(DataComponents.FOOD) && stack.has(DataComponents.CONSUMABLE);
   }

   static boolean shouldEat(int food) {
      if (food <= 6) {
         return true;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         Inventory inventory = mc.player.getInventory();

         for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD) && !IGNORED_FOOD_IDS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
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
               if (isEdible(stack) && !NEVER_EAT.contains(stack.getItem())) {
                  boolean precious = PRECIOUS_FOODS.contains(stack.getItem());
                  boolean harmful = HARMFUL_FOODS.contains(stack.getItem());
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

   static Vec3 awayFrom(Vec3 playerPos, List<Vec3> threatPositions, double distance) {
      return awayFrom(playerPos, threatPositions, distance, 0.0);
   }

   static Vec3 awayFrom(Vec3 playerPos, List<Vec3> threatPositions, double distance, double bearingDegrees) {
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

   static double dodgeBearing(int leg) {
      return leg % 2 == 0 ? 22.5 : -22.5;
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal init(final BlockPos start, BlockPos target) {
      final autismclient.ai.engine.AiPathfinder.PathGoal near = autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(target, 0.6);
      return new autismclient.ai.engine.AiPathfinder.PathGoal() {
         @Override
         public boolean isInGoal(int x, int y, int z) {
            return near.isInGoal(x, y, z);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return near.heuristic(x, y, z);
         }

         @Override
         public boolean allowsCell(BlockPos cell) {
            double dx = cell.getX() - start.getX();
            double dz = cell.getZ() - start.getZ();
            return dx * dx + dz * dz <= 64.0 && Math.abs(cell.getY() - start.getY()) <= 3;
         }
      };
   }

   static boolean movesAway(Vec3 start, Vec3 end, List<Vec3> threats) {
      if (start.distanceToSqr(end) < 1.0) {
         return false;
      }

      Vec3 away = awayFrom(start, threats, 1.0);
      return away != null && (end.x - start.x) * (away.x - start.x) + (end.z - start.z) * (away.z - start.z) >= -0.05;
   }

   static BlockPos shaftClimbTarget(BlockPos feet, List<Vec3> threats, int blocks, Predicate<BlockPos> clear) {
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

   static double routeRisk(Vec3 start, Vec3 end, List<Vec3> threats) {
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

   static List<LivingEntity> livingWithin(List<Entity> entities, Vec3 playerPos, double radius) {
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
