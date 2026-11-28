package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiBurrow {
   static final int init_I = 600;
   static final int load = 900;
   private static final int read = 3;
   private static final int write = 4;
   private static final int update = 40;
   private static final List<Direction> HORIZONTALS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

   private AiBurrow() {
   }

   static AiBurrow.Inner_StreamUtil digDecision(boolean loaded, boolean fluid, boolean unbreakable, boolean open, boolean safeFloorBelow) {
      if (!loaded || fluid || unbreakable) {
         return AiBurrow.Inner_StreamUtil.STOP;
      } else if (open) {
         return safeFloorBelow ? AiBurrow.Inner_StreamUtil.OPEN : AiBurrow.Inner_StreamUtil.STOP;
      } else {
         return AiBurrow.Inner_StreamUtil.DIG;
      }
   }

   static BlockPos roofOf(BlockPos feet) {
      return feet.above(2);
   }

   static boolean isMorning(long dayTime) {
      return dayTime >= 0L && dayTime < 1000L;
   }

   static List<BlockPos> shellOf(BlockPos feet) {
      List<BlockPos> cells = new ArrayList<>();
      cells.add(feet.below());

      for (Direction side : HORIZONTALS) {
         cells.add(feet.relative(side));
         cells.add(feet.above().relative(side));
      }

      cells.add(roofOf(feet));
      return List.copyOf(cells);
   }

   static boolean isSealed(BlockPos feet, Predicate<BlockPos> fullBlock) {
      return shellOf(feet).stream().allMatch(fullBlock);
   }

   static List<BlockPos> plannedShell(BlockPos feet, Predicate<BlockPos> fullBlock) {
      List<BlockPos> cells = new ArrayList<>();
      Set<BlockPos> planned = new HashSet<>();
      Predicate<BlockPos> support = pos -> fullBlock.test(pos) || planned.contains(pos);

      for (Direction side : HORIZONTALS) {
         BlockPos wall = feet.relative(side);
         if (!fullBlock.test(wall)) {
            boolean adjacent = false;

            for (Direction face : Direction.values()) {
               if (support.test(wall.relative(face))) {
                  adjacent = true;
                  break;
               }
            }

            if (!adjacent && !support.test(wall.below())) {
               cells.add(wall.below());
               planned.add(wall.below());
            }

            cells.add(wall);
            planned.add(wall);
         }
      }

      for (Direction side : List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH)) {
         BlockPos wall = feet.above().relative(side);
         if (!fullBlock.test(wall)) {
            cells.add(wall);
            planned.add(wall);
         }
      }

      BlockPos roof = roofOf(feet);
      if (!fullBlock.test(roof)) {
         boolean adjacent = false;

         for (Direction face : Direction.values()) {
            if (support.test(roof.relative(face))) {
               adjacent = true;
               break;
            }
         }

         if (!adjacent) {
            BlockPos beam = roof.north();
            cells.add(beam);
            planned.add(beam);
         }

         cells.add(roof);
      }

      return List.copyOf(cells);
   }

   static boolean canEatInShelter(float health, float maxHealth, int food, boolean hasFood, boolean sealed, boolean supported, boolean interruptible, boolean pending) {
      return health < maxHealth - 0.01F && food < 18 && hasFood && sealed && supported && interruptible && !pending;
   }

   static boolean shouldWaitOutBurning(
      boolean healing,
      boolean waiting,
      boolean burning,
      boolean onFireDamage,
      boolean supported,
      boolean sealed,
      boolean hostileInside,
      boolean touchingFireOrLava
   ) {
      return healing && waiting && burning && onFireDamage && supported && sealed && !hostileInside && !touchingFireOrLava;
   }

   static boolean isPlacementPending(boolean placing, BooleanSupplier pending) {
      return placing && pending.getAsBoolean();
   }

   static boolean isDoneWaiting(float healTo, long dayTime) {
      return healTo <= 0.0F && isMorning(dayTime);
   }

   static boolean canHealInShelter(float healTo, boolean supported, boolean sealed, boolean hostileInside, boolean bodyHazard) {
      return healTo > 0.0F && supported && sealed && !hostileInside && !bodyHazard;
   }

   static boolean isCentredInCell(BlockPos feet, AABB body, double feetY) {
      return body.minX >= feet.getX() + 0.001
         && body.maxX <= feet.getX() + 0.999
         && body.minZ >= feet.getZ() + 0.001
         && body.maxZ <= feet.getZ() + 0.999
         && feetY >= feet.getY() - 0.05
         && feetY < feet.getY() + 1.5;
   }

   static boolean isDescended(int feetY, int groundY, boolean supported) {
      return feetY <= groundY && supported;
   }

   static boolean needsReconcileAfter(AiOperation.Status status, boolean pendingBefore, boolean pendingAfter) {
      return status == AiOperation.Status.UNCERTAIN || pendingAfter || status != AiOperation.Status.SUCCEEDED && pendingBefore;
   }

   static BlockPos bestNearbyStand(BlockPos feet, Predicate<BlockPos> supportedStand, Predicate<BlockPos> safeColumn) {
      BlockPos best = null;
      int price = 2147483647;

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            for (int y = -1; y <= 1; y++) {
               int walk = Math.abs(x) + Math.abs(z);
               if (walk != 0 && walk <= 3) {
                  BlockPos pos = feet.offset(x, y, z);
                  int candidate = walk + 4 * Math.abs(y);
                  if (candidate < price && supportedStand.test(pos) && safeColumn.test(pos)) {
                     best = pos;
                     price = candidate;
                  }
               }
            }
         }
      }

      return best;
   }

   static boolean init() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return false;
      }

      int blocks = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (!stack.isEmpty()
            && stack.getItem() instanceof BlockItem block
            && !(block.getBlock() instanceof FallingBlock)
            && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(mc.level, mc.player.blockPosition())) {
            blocks += stack.getCount();
         }
      }

      return blocks >= 8;
   }

   static AiOperation burrow(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(60, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 600)));
         final float healTo = (float)AiJson.getDouble(args, "healTo", 0.0);
         final boolean stay = AiJson.getBoolean(args, "stay", false);
         final boolean towerOk = AiJson.getBoolean(args, "towerOk", true);
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object startLevel = mc.level;
            final Object startPlayer = mc.player;
            final Object startConnection = mc.getConnection();
            final BlockPos startPos = mc.player.blockPosition().immutable();
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer().fast();
            final autismclient.ai.engine.AiRestock restock = new autismclient.ai.engine.AiRestock(mc);
            AiBurrow.Inner_DataUtil stage = AiBurrow.Inner_DataUtil.SHAFT;
            AiOperation childOperation;
            AiOperation mealOperation;
            BlockPos nearby = this.startPos;
            BlockPos feet;
            BlockPos currentCell;
            BlockPos shelterFeet;
            List<BlockPos> shellCells;
            Item placingItem;
            int shellIndex;
            int shaftEntryTicks;
            int hidTicks;
            int descentCentringTicks;
            int mealAttempts;
            int recentreAttempts;
            boolean placing;
            boolean capPlaced;
            boolean cancelled;
            boolean waterEscape;
            boolean relocateTried;
            boolean jumpHeld;
            boolean reconcileRequired;
            JsonObject pendingChildEvidence = new JsonObject();
            float health = mc.player.getHealth();
            int lastMealTick;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (mc.level != this.startLevel || mc.player != this.startPlayer || mc.getConnection() != this.startConnection) {
                     return this.finish(AiOperation.Status.FAILED, "World/player changed during burrow");
                  }

                  if (!mc.player.isAlive()) {
                     return this.finish(AiOperation.Status.FAILED, "Player died during burrow");
                  }

                  if (this.jumpHeld) {
                     mc.options.keyJump.setDown(false);
                     this.jumpHeld = false;
                  }

                  BlockPos feet = mc.player.blockPosition();
                  float health = mc.player.getHealth();
                  float previousHealth = this.health;
                  boolean hurt = health < previousHealth - 0.01F;
                  this.health = health;
                  if (this.stage == AiBurrow.Inner_DataUtil.SHAFT
                     && this.childOperation == null
                     && AiBurrow.canHealInShelter(
                        healTo,
                        autismclient.ai.engine.AiMotionSafety.isPlayerSupported(),
                        AiBurrow.isSealed(feet, this::isSolidFloor) && this.isInteriorClear(feet),
                        this.hasHostileInside(feet),
                        mc.player.isInWater() || mc.player.isInLava() || AiEndRun.init_Z(mc)
                     )) {
                     this.shelterFeet = feet.immutable();
                     this.currentCell = AiBurrow.roofOf(this.shelterFeet);
                     this.capPlaced = this.isCentredOver(this.shelterFeet);
                     this.stage = this.capPlaced ? AiBurrow.Inner_DataUtil.WAIT : AiBurrow.Inner_DataUtil.CAP;
                  }

                  boolean exiting = this.stage == AiBurrow.Inner_DataUtil.EXIT_DIG
                     || this.stage == AiBurrow.Inner_DataUtil.EXIT_TRAVEL;
                  if (!exiting && this.stage != AiBurrow.Inner_DataUtil.TOWER && mc.player.isInWater()) {
                     this.reconcileRequired = this.reconcileRequired | this.requiresReconciliationAfterEmergency();
                     if (this.childOperation != null) {
                        this.childOperation.cancel();
                        this.childOperation = null;
                     }

                     if (this.mealOperation != null) {
                        this.mealOperation.cancel();
                        this.mealOperation = null;
                     }

                     this.placer.reset();
                     this.placing = false;
                     this.waterEscape = true;
                     this.stage = this.capPlaced ? AiBurrow.Inner_DataUtil.EXIT_DIG : AiBurrow.Inner_DataUtil.EXIT_TRAVEL;
                     return this.running("Water in the burrow; climbing out");
                  } else {
                     boolean building = this.stage == AiBurrow.Inner_DataUtil.SHAFT
                        || this.stage == AiBurrow.Inner_DataUtil.RELOCATE
                        || this.stage == AiBurrow.Inner_DataUtil.CAP
                        || this.stage == AiBurrow.Inner_DataUtil.TOWER
                        || this.stage == AiBurrow.Inner_DataUtil.ROOF;
                     boolean residualBurn = hurt
                        && AiBurrow.shouldWaitOutBurning(
                           healTo > 0.0F,
                           this.stage == AiBurrow.Inner_DataUtil.WAIT,
                           mc.player.isOnFire(),
                           AiServerEvidence.matchesRecentBurnDamage(mc.player.getUUID(), mc.player.tickCount, previousHealth, health),
                           autismclient.ai.engine.AiMotionSafety.isPlayerSupported(),
                           this.stage == AiBurrow.Inner_DataUtil.WAIT && this.isShelterSealed(),
                           this.stage == AiBurrow.Inner_DataUtil.WAIT && this.hasHostileInside(this.shelterFeet),
                           mc.player.isInLava() || AiEndRun.init_Z(mc)
                        );
                     if (hurt && !exiting && (!(healTo > 0.0F) || !building) && !residualBurn) {
                        return this.finish(
                           AiOperation.Status.FAILED, "burrow compromised (hurt while " + this.stage.name().toLowerCase(Locale.ROOT) + ")"
                        );
                     }

                     if (this.stage != AiBurrow.Inner_DataUtil.WAIT || this.isShelterSealed() && !this.hasHostileInside(this.shelterFeet)) {
                        if (this.stage == AiBurrow.Inner_DataUtil.CAP && !this.capPlaced && this.hostileNear(feet, 1.0)) {
                           return this.finish(AiOperation.Status.FAILED, "burrow compromised (a hostile in the shaft before the cap)");
                        }

                        if (System.nanoTime() > this.deadline && !exiting && this.stage != AiBurrow.Inner_DataUtil.WAIT) {
                           return this.finish(AiOperation.Status.TIMED_OUT, "Burrow deadline reached before cover");
                        }

                        if (this.restock.isRunning()) {
                           autismclient.ai.engine.AiRestock.RestockState status = this.restock.tick();
                           if (status == autismclient.ai.engine.AiRestock.RestockState.FAILED) {
                              return this.finish(AiOperation.Status.FAILED, "Construction stock swap was not confirmed");
                           }

                           if (status == autismclient.ai.engine.AiRestock.RestockState.SUCCEEDED) {
                              this.restock.reset();
                           }

                           return this.running("Confirming shelter construction stock in the hotbar");
                        } else {
                           AiOperation.Result var10000;
                           switch (this.stage) {
                              case SHAFT:
                                 var10000 = this.tickShaft(feet);
                                 break;
                              case RELOCATE:
                                 var10000 = this.tickRelocate();
                                 break;
                              case CAP:
                              case ROOF:
                                 var10000 = this.tickCap(feet);
                                 break;
                              case TOWER:
                                 var10000 = this.tickTower(feet);
                                 break;
                              case WAIT:
                                 var10000 = this.tickWait(feet);
                                 break;
                              case EXIT_DIG:
                                 var10000 = this.tickExitDig(feet);
                                 break;
                              case EXIT_TRAVEL:
                                 var10000 = this.tickExitTravel();
                                 break;
                              default:
                                 throw new MatchException(null, null);
                           }

                           return var10000;
                        }
                     } else {
                        return this.finish(AiOperation.Status.FAILED, "burrow compromised (cover opened or a hostile inside)");
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Burrow cancelled");
               }
            }

            AiOperation.Result tickShaft(BlockPos feet) {
               int descended = this.nearby.getY() - feet.getY();
               if (descended >= 3) {
                  this.stage = AiBurrow.Inner_DataUtil.CAP;
                  return this.running("In the shaft; capping");
               }

               if (this.childOperation != null) {
                  boolean pending = this.childOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.childOperation.tick();
                  if (!r.terminal()) {
                     return this.running("Digging the shaft");
                  }

                  if (AiBurrow.needsReconcileAfter(r.status(), pending, this.childOperation.requiresReconciliationAfterEmergency())) {
                     return this.recordChildFailure(r, "Shaft digging");
                  }

                  this.childOperation.cancel();
                  this.childOperation = null;
                  return r.status() != AiOperation.Status.SUCCEEDED
                     ? this.handleShaftBlocked(feet, descended, "a shaft cell would not break")
                     : this.running("Digging the shaft");
               } else {
                  switch (this.classifyBelow(feet.below())) {
                     case DIG:
                        this.childOperation = autismclient.ai.engine.AiBreakBlock.breakBlock(feet.below(), 20);
                        return this.running("Digging the shaft");
                     case OPEN:
                        if (++this.shaftEntryTicks > 40) {
                           return this.handleShaftBlocked(feet, descended, "the shaft would not swallow the player");
                        }

                        return this.running("Stepping into the shaft");
                     default:
                        return this.handleShaftBlocked(feet, descended, "the column below is unsafe");
                  }
               }
            }

            AiOperation.Result handleShaftBlocked(BlockPos feet, int descended, String why) {
               if (descended == 0 && !this.relocateTried) {
                  this.relocateTried = true;
                  BlockPos nearby = this.findRelocateStand(feet);
                  if (nearby != null) {
                     this.nearby = nearby;
                     this.childOperation = AiGoto.travelToCompat(nearby, 0.2, false, false, false, 10);
                     this.stage = AiBurrow.Inner_DataUtil.RELOCATE;
                     return this.running(why + "; moving a few steps to a safe shaft");
                  }
               }

               if (descended < 2 && !towerOk) {
                  return this.finish(AiOperation.Status.FAILED, why + "; no cover here");
               } else if (descended < 2) {
                  this.stage = AiBurrow.Inner_DataUtil.TOWER;
                  return this.running(why + "; towering instead");
               } else {
                  this.stage = AiBurrow.Inner_DataUtil.CAP;
                  return this.running(why + "; capping the shallow cover");
               }
            }

            AiOperation.Result tickRelocate() {
               boolean pending = this.childOperation.requiresReconciliationAfterEmergency();
               AiOperation.Result r = this.childOperation.tick();
               if (!r.terminal()) {
                  return this.running("Moving to nearby solid ground for the shelter");
               }

               if (AiBurrow.needsReconcileAfter(r.status(), pending, this.childOperation.requiresReconciliationAfterEmergency())) {
                  return this.recordChildFailure(r, "Shelter approach");
               }

               this.childOperation.cancel();
               this.childOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.finish(r.status(), "Nearby shelter could not be reached: " + r.message());
               }

               this.stage = AiBurrow.Inner_DataUtil.SHAFT;
               return this.running("On safe ground; digging the shelter");
            }

            AiOperation.Result tickCap(BlockPos feet) {
               boolean beamInFlight = this.placing
                  && this.shelterFeet != null
                  && this.currentCell.getY() == this.shelterFeet.getY() + 2
                  && !this.currentCell.equals(AiBurrow.roofOf(this.shelterFeet))
                  && this.hasWalls(this.shelterFeet);
               if (!autismclient.ai.engine.AiMotionSafety.isPlayerSupported() && !beamInFlight) {
                  return this.running("Settling before closing the shelter");
               }

               if (this.shelterFeet == null) {
                  this.shelterFeet = feet.immutable();
               }

               if (this.childOperation != null) {
                  boolean pending = this.childOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.childOperation.tick();
                  if (!r.terminal()) {
                     return this.running("Centering inside the shelter");
                  }

                  if (AiBurrow.needsReconcileAfter(r.status(), pending, this.childOperation.requiresReconciliationAfterEmergency())) {
                     return this.recordChildFailure(r, "Shelter centering");
                  }

                  this.childOperation.cancel();
                  this.childOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.finish(r.status(), "Could not settle inside shelter: " + r.message());
                  }
               }

               if (!this.isCentredOver(this.shelterFeet)) {
                  if (++this.recentreAttempts > 3) {
                     return this.finish(AiOperation.Status.FAILED, "Could not settle into the shelter's own cell");
                  }

                  this.childOperation = AiGoto.travelToCompat(this.shelterFeet, 0.15, false, false, false, 5);
                  return this.running("Centering inside the shelter");
               } else {
                  if (this.hasHostileInside(this.shelterFeet)) {
                     return this.finish(AiOperation.Status.FAILED, "A hostile occupies the shelter interior");
                  }

                  if (this.shellCells == null) {
                     this.shellCells = AiBurrow.plannedShell(this.shelterFeet, this::isSolidFloor);
                     if (this.spareShellBlocks() < this.shellCells.size()) {
                        return this.finishWith(AiOperation.Status.FAILED, "Not enough spare blocks to close the shelter", this.missingBlocksJson(this.shellCells.size()));
                     }

                     for (BlockPos cell : this.shellCells) {
                        if (!this.isSolidFloor(cell)
                           && (!AiTerrain.isLoaded(mc.level, cell) || !mc.level.getBlockState(cell).canBeReplaced() || !mc.level.getFluidState(cell).isEmpty())) {
                           return this.finish(AiOperation.Status.FAILED, "Shelter boundary cannot be safely filled at " + cell.toShortString());
                        }
                     }
                  }

                  if (!this.placing) {
                     while (this.shellIndex < this.shellCells.size() && this.isSolidFloor(this.shellCells.get(this.shellIndex))) {
                        this.shellIndex++;
                     }

                     if (this.shellIndex >= this.shellCells.size()) {
                        if (!this.isShelterSealed()) {
                           return this.finish(AiOperation.Status.FAILED, "Shelter boundary is still open after construction");
                        }

                        this.currentCell = AiBurrow.roofOf(this.shelterFeet);
                        this.capPlaced = true;
                        this.stage = AiBurrow.Inner_DataUtil.WAIT;
                        return this.running("Shelter sealed; waiting safely until dawn");
                     }

                     int slot = this.findBlockSlot();
                     if (slot < 0) {
                        if (this.pullBlocksToHotbar()) {
                           return this.running("Moving spare shelter blocks to the hotbar");
                        }

                        return this.finishWith(
                           AiOperation.Status.FAILED, "No spare construction block to finish the shelter", this.missingBlocksJson(this.shellCells.size() - this.shellIndex)
                        );
                     }

                     this.currentCell = this.shellCells.get(this.shellIndex);
                     autismclient.ai.engine.AiEditLedger.forgetBreak(this.currentCell);
                     this.placingItem = mc.player.getInventory().getItem(slot).getItem();
                     this.placer.start(this.currentCell, this.placingItem);
                     this.placing = true;
                  }

                  if (this.currentCell.getY() == this.shelterFeet.getY() + 2
                     && !this.currentCell.equals(AiBurrow.roofOf(this.shelterFeet))
                     && !this.isSolidFloor(this.currentCell)
                     && this.hasWalls(this.shelterFeet)
                     && !this.placer.needsReconciliation()
                     && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()
                     && !autismclient.ai.engine.AiPlacer.canPlaceFromHere(this.currentCell, this.placingItem)) {
                     mc.options.keyJump.setDown(true);
                     this.jumpHeld = true;
                  }

                  autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
                  if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.WORKING) {
                     return this.running("Closing shelter walls and roof");
                  } else {
                     boolean pending = this.placer.needsReconciliation();
                     this.placer.reset();
                     this.placing = false;
                     if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                        this.reconcileRequired |= pending;
                        return this.finish(AiOperation.Status.FAILED, "Shelter placement failed: " + this.placer.failureReason());
                     } else {
                        this.shellIndex++;
                        return this.running("Shelter boundary confirmed; closing remaining gaps");
                     }
                  }
               }
            }

            AiOperation.Result tickTower(BlockPos feet) {
               if (this.feet == null) {
                  BlockPos top = feet.above(4);
                  List<BlockPos> boundary = AiBurrow.plannedShell(top, pos -> pos.equals(top.below()) || this.isSolidFloor(pos));
                  int needed = 4 + boundary.size();
                  if (this.spareShellBlocks() < needed) {
                     return this.finishWith(AiOperation.Status.FAILED, "Unsafe column and insufficient spare stock for a closed tower shelter", this.missingBlocksJson(needed));
                  }

                  for (BlockPos cell : boundary) {
                     if (!this.isSolidFloor(cell)
                        && (!AiTerrain.isLoaded(mc.level, cell) || !mc.level.getBlockState(cell).canBeReplaced() || !mc.level.getFluidState(cell).isEmpty())) {
                        return this.finish(AiOperation.Status.FAILED, "No observed safe boundary for a closed tower shelter at " + cell.toShortString());
                     }
                  }

                  if (this.findBlockSlot() < 0) {
                     if (this.pullBlocksToHotbar()) {
                        return this.running("Moving spare tower blocks to the hotbar");
                     }

                     return this.finishWith(AiOperation.Status.FAILED, "No spare construction stock for the tower shelter", this.missingBlocksJson(needed));
                  }

                  this.feet = feet;
                  this.childOperation = AiGoto.travelToCompat(this.feet.above(4), 0.5, true, true, false, 15);
               }

               boolean pending = this.childOperation.requiresReconciliationAfterEmergency();
               AiOperation.Result r = this.childOperation.tick();
               if (!r.terminal()) {
                  return this.running("Towering up");
               }

               if (AiBurrow.needsReconcileAfter(r.status(), pending, this.childOperation.requiresReconciliationAfterEmergency())) {
                  return this.recordChildFailure(r, "Shelter tower");
               }

               this.childOperation.cancel();
               this.childOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.finish(AiOperation.Status.FAILED, "Could not tower up (" + r.message() + ")");
               }

               this.stage = AiBurrow.Inner_DataUtil.ROOF;
               return this.running("Tower up; roofing");
            }

            AiOperation.Result tickWait(BlockPos feet) {
               this.hidTicks++;
               if (this.hidTicks % 200 == 0) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] burrow: hiding at {}, health {}, food {}",
                        feet.toShortString(),
                        (int)mc.player.getHealth(),
                        mc.player.getFoodData().getFoodLevel()
                     );
               }

               if (this.mealOperation != null) {
                  boolean pending = this.mealOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.mealOperation.tick();
                  if (!r.terminal()) {
                     return this.running("Eating in the sealed shelter to enable regeneration");
                  }

                  if (AiBurrow.needsReconcileAfter(r.status(), pending, this.mealOperation.requiresReconciliationAfterEmergency())) {
                     return this.recordChildFailure(r, "Shelter meal");
                  }

                  this.mealOperation.cancel();
                  this.mealOperation = null;
                  return this.running("Shelter meal observed; waiting safely");
               } else {
                  if (this.mealAttempts < 3
                     && this.deadline - System.nanoTime() >= 10000000000L
                     && AiBurrow.canEatInShelter(
                        mc.player.getHealth(),
                        mc.player.getMaxHealth(),
                        mc.player.getFoodData().getFoodLevel(),
                        AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0,
                        this.isShelterSealed(),
                        autismclient.ai.engine.AiMotionSafety.isPlayerSupported(),
                        this.canInterruptNow(),
                        this.requiresReconciliationAfterEmergency()
                     )) {
                     this.mealAttempts++;
                     this.mealOperation = AiSurvival.eat(AiJson.objectOf("food", 20, "timeoutSeconds", 10));
                     return this.running("Eating in the sealed shelter to enable regeneration");
                  }

                  if (healTo > 0.0F && mc.player.getFoodData().getFoodLevel() < 18 && mc.player.getHealth() < healTo && this.hidTicks - this.lastMealTick >= 100) {
                     if (stay && this.feet == null) {
                        return this.finish(
                           AiOperation.Status.SUCCEEDED,
                           "No mob in sight for five seconds (health " + (int)mc.player.getHealth() + ", too hungry to heal); going on from here"
                        );
                     }

                     this.stage = this.feet == null && !this.capPlaced
                        ? AiBurrow.Inner_DataUtil.EXIT_TRAVEL
                        : AiBurrow.Inner_DataUtil.EXIT_DIG;
                     return this.running("No mob in sight; leaving cover");
                  } else {
                     if (healTo > 0.0F && this.isThreatNearby()) {
                        this.lastMealTick = this.hidTicks;
                     }

                     if (healTo > 0.0F && mc.player.getHealth() >= healTo && this.hidTicks - this.lastMealTick >= 60) {
                        if (stay && this.feet == null) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "Healed to " + (int)mc.player.getHealth() + " in the hole; going on from here");
                        }

                        this.stage = this.feet == null && !this.capPlaced
                           ? AiBurrow.Inner_DataUtil.EXIT_TRAVEL
                           : AiBurrow.Inner_DataUtil.EXIT_DIG;
                        return this.running("Healed to " + (int)mc.player.getHealth() + "; leaving cover");
                     } else {
                        if (!AiBurrow.isDoneWaiting(healTo, AiSleep.dayTime()) && System.nanoTime() <= this.deadline) {
                           return this.running(
                              healTo > 0.0F
                                 ? "Healing in the sealed shelter (" + (int)mc.player.getHealth() + "/" + (int)healTo + ")"
                                 : "Hiding until dawn (day time " + AiSleep.dayTime() + ")"
                           );
                        }

                        this.stage = this.feet == null && !this.capPlaced
                           ? AiBurrow.Inner_DataUtil.EXIT_TRAVEL
                           : AiBurrow.Inner_DataUtil.EXIT_DIG;
                        return this.running(healTo > 0.0F ? "Shelter healing budget ended; leaving cover" : "Dawn; leaving cover");
                     }
                  }
               }
            }

            AiOperation.Result tickExitDig(BlockPos feet) {
               if (this.feet == null) {
                  if (this.childOperation == null) {
                     this.childOperation = autismclient.ai.engine.AiBreakBlock.breakBlock(this.currentCell, 20);
                  }

                  boolean pending = this.childOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.childOperation.tick();
                  if (!r.terminal()) {
                     return this.running("Breaking the cap");
                  }

                  if (AiBurrow.needsReconcileAfter(r.status(), pending, this.childOperation.requiresReconciliationAfterEmergency())) {
                     return this.recordChildFailure(r, "Shelter opening");
                  }

                  this.childOperation.cancel();
                  this.childOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.finish(r.status(), "Could not open the shelter cap: " + r.message());
                  }

                  this.stage = AiBurrow.Inner_DataUtil.EXIT_TRAVEL;
                  return this.running("Cap open; climbing out");
               } else {
                  if (AiBurrow.isDescended(feet.getY(), this.feet.getY(), autismclient.ai.engine.AiMotionSafety.isPlayerSupported())) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Down from the tower; the night is over");
                  }

                  if (!autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                     return this.running("Settling onto the ground while descending the shelter");
                  }

                  if (mc.level.getBlockState(feet.below()).isAir()) {
                     if (!mc.player.onGround()) {
                        return this.running("Stepping down the tower");
                     } else if (++this.descentCentringTicks > 40) {
                        this.restoreMovementKeys();
                        return this.finish(AiOperation.Status.FAILED, "Could not center over the shelter column for a safe descent");
                     } else {
                        double dx = this.feet.getX() + 0.5 - mc.player.getX();
                        double dz = this.feet.getZ() + 0.5 - mc.player.getZ();
                        double len = Math.hypot(dx, dz);
                        double yaw = Math.toRadians(mc.player.getYRot());
                        double f = len < 0.001 ? 0.0 : (-dx * Math.sin(yaw) + dz * Math.cos(yaw)) / len;
                        double l = len < 0.001 ? 0.0 : (dx * Math.cos(yaw) + dz * Math.sin(yaw)) / len;
                        mc.options.keyUp.setDown(len > 0.2 && f > 0.38);
                        mc.options.keyDown.setDown(len > 0.2 && f < -0.38);
                        mc.options.keyLeft.setDown(len > 0.2 && l > 0.38);
                        mc.options.keyRight.setDown(len > 0.2 && l < -0.38);
                        return this.running("Stepping back over the tower");
                     }
                  } else {
                     if (this.descentCentringTicks > 0) {
                        mc.options.keyUp.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                     }

                     this.descentCentringTicks = 0;
                     if (this.childOperation == null) {
                        this.childOperation = autismclient.ai.engine.AiBreakBlock.breakBlock(feet.below(), 20);
                     }

                     boolean pending = this.childOperation.requiresReconciliationAfterEmergency();
                     AiOperation.Result r = this.childOperation.tick();
                     if (!r.terminal()) {
                        return this.running("Digging down the tower");
                     }

                     if (AiBurrow.needsReconcileAfter(r.status(), pending, this.childOperation.requiresReconciliationAfterEmergency())) {
                        return this.recordChildFailure(r, "Shelter descent");
                     }

                     this.childOperation.cancel();
                     this.childOperation = null;
                     return r.status() == AiOperation.Status.SUCCEEDED
                        ? this.running("Digging down the tower")
                        : this.finish(r.status(), "Could not safely descend the shelter tower: " + r.message());
                  }
               }
            }

            AiOperation.Result tickExitTravel() {
               if (this.childOperation == null) {
                  this.childOperation = AiGoto.travelToCompat(this.startPos, 1.2, true, true, false, 60);
               }

               boolean pending = this.childOperation.requiresReconciliationAfterEmergency();
               AiOperation.Result r = this.childOperation.tick();
               if (!r.terminal()) {
                  return this.running("Climbing out");
               } else if (AiBurrow.needsReconcileAfter(r.status(), pending, this.childOperation.requiresReconciliationAfterEmergency())) {
                  return this.recordChildFailure(r, "Shelter exit");
               } else {
                  this.childOperation.cancel();
                  this.childOperation = null;
                  if (r.status() == AiOperation.Status.SUCCEEDED && !autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                     return this.running("Settling on the surface before handing back control");
                  } else if (r.status() == AiOperation.Status.SUCCEEDED && this.waterEscape) {
                     return this.finish(
                        AiOperation.Status.FAILED, "Water reached the burrow; back on the surface, still night - use game:sleep or shelter elsewhere"
                     );
                  } else {
                     return r.status() == AiOperation.Status.SUCCEEDED
                        ? this.finish(AiOperation.Status.SUCCEEDED, "Back on the surface; the night is over")
                        : this.finish(AiOperation.Status.FAILED, "Burrowed but could not climb out (" + r.message() + "); dig up manually");
                  }
               }
            }

            boolean isSolidFloor(BlockPos pos) {
               return AiTerrain.isLoaded(mc.level, pos) && mc.level.getFluidState(pos).isEmpty() && mc.level.getBlockState(pos).isCollisionShapeFullBlock(mc.level, pos);
            }

            boolean hasWalls(BlockPos feet) {
               for (Direction side : AiBurrow.HORIZONTALS) {
                  if (!this.isSolidFloor(feet.relative(side)) || !this.isSolidFloor(feet.above().relative(side))) {
                     return false;
                  }
               }

               return true;
            }

            boolean isCentredOver(BlockPos feet) {
               return AiBurrow.isCentredInCell(feet, mc.player.getBoundingBox(), mc.player.getY());
            }

            boolean isShelterSealed() {
               return this.shelterFeet != null
                     && this.isCentredOver(this.shelterFeet)
                     && AiBurrow.isSealed(this.shelterFeet, this::isSolidFloor)
                  ? this.isInteriorClear(this.shelterFeet)
                  : false;
            }

            boolean isInteriorClear(BlockPos feet) {
               for (BlockPos inside : List.of(feet, feet.above())) {
                  if (!AiTerrain.isLoaded(mc.level, inside)
                     || !mc.level.getFluidState(inside).isEmpty()
                     || !mc.level.getBlockState(inside).getCollisionShape(mc.level, inside).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }

            boolean hasHostileInside(BlockPos feet) {
               AABB room = new AABB(feet.getX() + 0.001, feet.getY(), feet.getZ() + 0.001, feet.getX() + 0.999, feet.getY() + 2, feet.getZ() + 0.999);

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Enemy && entity.isAlive() && entity.getBoundingBox().intersects(room)) {
                     return true;
                  }
               }

               return false;
            }

            BlockPos findRelocateStand(BlockPos feet) {
               return AiBurrow.bestNearbyStand(
                  feet,
                  pos -> {
                     if (this.isSolidFloor(pos.below()) && !this.hostileNear(pos, 3.0)) {
                        for (BlockPos inside : List.of(pos, pos.above())) {
                           if (!AiTerrain.isLoaded(mc.level, inside)
                              || !mc.level.getFluidState(inside).isEmpty()
                              || !mc.level.getBlockState(inside).getCollisionShape(mc.level, inside).isEmpty()) {
                              return false;
                           }
                        }

                        return true;
                     } else {
                        return false;
                     }
                  },
                  pos -> {
                     for (int down = 1; down <= 3; down++) {
                        if (!this.isSolidFloor(pos.below(down)) || this.classifyBelow(pos.below(down)) != AiBurrow.Inner_StreamUtil.DIG) {
                           return false;
                        }
                     }

                     BlockPos floor = pos.below(3);
                     return this.isSolidFloor(floor.below()) && this.hasWalls(floor);
                  }
               );
            }

            AiBurrow.Inner_StreamUtil classifyBelow(BlockPos pos) {
               if (!AiTerrain.isLoaded(mc.level, pos)) {
                  return AiBurrow.Inner_StreamUtil.STOP;
               }

               BlockState state = mc.level.getBlockState(pos);
               boolean fluid = !mc.level.getFluidState(pos).isEmpty() || state.is(Blocks.ICE);

               for (Direction side : Plane.HORIZONTAL) {
                  if (!mc.level.getFluidState(pos.relative(side)).isEmpty()) {
                     fluid = true;
                  }
               }

               boolean unbreakable = state.getDestroySpeed(mc.level, pos) < 0.0F;
               boolean open = state.getCollisionShape(mc.level, pos).isEmpty();
               boolean safeFloor = AiCombat.isSafeStep(mc, pos.below()) && !this.hostileNear(pos, 3.0);

               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos wall = pos.relative(side);
                  if (!this.isSolidFloor(wall)) {
                     return AiBurrow.Inner_StreamUtil.STOP;
                  }
               }

               return open && healTo > 0.0F ? AiBurrow.Inner_StreamUtil.STOP : AiBurrow.digDecision(true, fluid, unbreakable, open, safeFloor);
            }

            boolean isThreatNearby() {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 256.0 && mc.player.hasLineOfSight(e)) {
                     return true;
                  }
               }

               return false;
            }

            boolean hostileNear(BlockPos pos, double radius) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof Enemy && !(e instanceof Enderman) && e.isAlive() && e.distanceToSqr(Vec3.atCenterOf(pos)) < radius * radius) {
                     return true;
                  }
               }

               return false;
            }

            boolean isShellBlock(ItemStack stack) {
               return stack.getItem() instanceof BlockItem block
                  && autismclient.ai.engine.AiRestock.isScaffoldBlockId(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                  && !(block.getBlock() instanceof FallingBlock)
                  && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(mc.level, mc.player.blockPosition());
            }

            int findBlockSlot() {
               for (int slot = 0; slot < 9; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && this.isShellBlock(stack) && autismclient.ai.engine.AiRestock.canSpendStack(stack, mc.player.getInventory(), true)) {
                     return slot;
                  }
               }

               return -1;
            }

            int spareShellBlocks() {
               Map<Item, Integer> counts = new HashMap<>();

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && this.isShellBlock(stack)) {
                     counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                  }
               }

               int count = 0;
               int stone = 0;
               int planks = 0;

               for (Entry<Item, Integer> entry : counts.entrySet()) {
                  int spare = Math.max(0, entry.getValue() - autismclient.ai.engine.AiRestock.reservedItemCount(entry.getKey()));
                  ItemStack stack = entry.getKey().getDefaultInstance();
                  if (autismclient.ai.engine.AiRestock.isStoneBlock(stack)) {
                     stone += spare;
                  } else if (stack.is(ItemTags.PLANKS)) {
                     planks += spare;
                  } else {
                     count += spare;
                  }
               }

               return count + Math.max(0, stone - autismclient.ai.engine.AiRestock.activeJobStoneReserve()) + Math.max(0, planks - autismclient.ai.engine.AiRestock.activeJobWoodReserve());
            }

            boolean pullBlocksToHotbar() {
               if (mc.player.containerMenu == mc.player.inventoryMenu && mc.player.containerMenu.getCarried().isEmpty()) {
                  int source = -1;
                  int rank = 2147483647;

                  for (int slot = 9; slot < 36; slot++) {
                     ItemStack stack = mc.player.getInventory().getItem(slot);
                     if (!stack.isEmpty() && this.isShellBlock(stack) && autismclient.ai.engine.AiRestock.canSpendStack(stack, mc.player.getInventory(), true)) {
                        int candidate = autismclient.ai.engine.AiRestock.scaffoldRank(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                        if (candidate < rank) {
                           source = slot;
                           rank = candidate;
                        }
                     }
                  }

                  if (source < 0) {
                     return false;
                  }

                  this.restock.startSwap(source, AiItems.pickHotbarDestination(), BuiltInRegistries.ITEM.getKey(mc.player.getInventory().getItem(source).getItem()).toString());
                  return true;
               } else {
                  return false;
               }
            }

            JsonObject missingBlocksJson(int needed) {
               JsonObject missing = new JsonObject();
               missing.addProperty("minecraft:dirt", Math.max(1, needed - this.spareShellBlocks()));
               return AiJson.objectOf("missing", missing, "constructionRequired", needed, "constructionAvailable", this.spareShellBlocks());
            }

            JsonObject statusJson() {
               JsonObject data = AiJson.objectOf(
                  "slept",
                  false,
                  "hidTicks",
                  this.hidTicks,
                  "capPlaced",
                  this.capPlaced,
                  "sealed",
                  mc.player != null && mc.level == this.startLevel && this.isShelterSealed(),
                  "mode",
                  this.feet != null ? "tower" : "shaft",
                  "phase",
                  this.stage.name(),
                  "dayTime",
                  AiSleep.dayTime(),
                  "meals",
                  this.mealAttempts,
                  "coverBlocksConfirmed",
                  this.shellIndex,
                  "restocks",
                  this.restock.completedSwapCount(),
                  "pendingChangesNeedObservation",
                  this.requiresReconciliationAfterEmergency()
               );
               if (!this.pendingChildEvidence.isEmpty()) {
                  data.add("pendingChild", this.pendingChildEvidence);
               }

               return data;
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.statusJson());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finishWith(status, message, new JsonObject());
            }

            AiOperation.Result finishWith(AiOperation.Status status, String message, JsonObject extra) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               JsonObject data = this.statusJson();

               for (Entry<String, JsonElement> entry : extra.entrySet()) {
                  data.add(entry.getKey(), entry.getValue());
               }

               this.cancel();
               return AiGuardLayer.wrapPendingTermination(pending, AiGameOperations.result(status, message, data));
            }

            AiOperation.Result recordChildFailure(AiOperation.Result result, String action) {
               this.reconcileRequired = true;
               this.pendingChildEvidence = AiJson.objectOf(
                  "status", result.status().name(), "message", result.message(), "failureCode", result.failure().name()
               );
               this.pendingChildEvidence.add("result", AiRunResources.parseEvidenceJson(result.dataJson()));
               JsonObject data = this.statusJson();
               this.cancel();
               return AiGuardLayer.wrapPendingTermination(
                  true, new AiOperation.Result(result.status(), action + ": " + result.message(), data.toString(), result.evidence(), result.failure())
               );
            }

            void restoreMovementKeys() {
               for (KeyMapping key : List.of(mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight)) {
                  key.setDown(autismclient.util.KeyMappingControl.of(key).autism$physicalForBody());
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.reconcileRequired
                  && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()
                  && !this.jumpHeld
                  && !AiBurrow.isPlacementPending(this.placing, this.placer::needsReconciliation)
                  && !this.restock.needsReconciliation()
                  && (this.childOperation == null || this.childOperation.canInterruptNow())
                  && (this.mealOperation == null || this.mealOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.reconcileRequired
                  || AiBurrow.isPlacementPending(this.placing, this.placer::needsReconciliation)
                  || this.restock.needsReconciliation()
                  || this.childOperation != null && this.childOperation.requiresReconciliationAfterEmergency()
                  || this.mealOperation != null && this.mealOperation.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.reconcileRequired = this.reconcileRequired | this.requiresReconciliationAfterEmergency();
                  this.cancelled = true;
                  if (this.childOperation != null) {
                     this.childOperation.cancel();
                     this.childOperation = null;
                  }

                  if (this.mealOperation != null) {
                     this.mealOperation.cancel();
                     this.mealOperation = null;
                  }

                  this.placer.reset();
                  this.restock.reset();
                  if (this.jumpHeld) {
                     mc.options.keyJump.setDown(autismclient.util.KeyMappingControl.of(mc.options.keyJump).autism$physicalForBody());
                     this.jumpHeld = false;
                  }

                  if (this.descentCentringTicks > 0) {
                     this.restoreMovementKeys();
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Burrowing requires a connected living player");
      }
   }

   private enum Inner_DataUtil {
      SHAFT,
      RELOCATE,
      CAP,
      TOWER,
      ROOF,
      WAIT,
      EXIT_DIG,
      EXIT_TRAVEL;
   }

   enum Inner_StreamUtil {
      DIG,
      OPEN,
      STOP;
   }
}
