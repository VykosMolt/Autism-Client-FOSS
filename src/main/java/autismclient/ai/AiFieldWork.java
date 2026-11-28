package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Fail;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiFieldWork {
   static final int init = 6;
   static final int load = 20;

   private AiFieldWork() {
   }

   static AiFieldWork.PlotState classifyPlot(BlockState ground, BlockState above, Block crop, boolean seeds) {
      boolean open = above.isAir();
      if (above.is(crop)) {
         return AiFieldWork.PlotState.DONE;
      } else if (autismclient.util.FarmCropCatalog.canGrowOn(crop, ground)) {
         return !open ? AiFieldWork.PlotState.BLOCKED : (seeds ? AiFieldWork.PlotState.SOW : AiFieldWork.PlotState.DONE);
      } else if (crop != Blocks.NETHER_WART && (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.DIRT_PATH))) {
         return open ? AiFieldWork.PlotState.TILL : AiFieldWork.PlotState.BLOCKED;
      } else {
         return AiFieldWork.PlotState.BLOCKED;
      }
   }

   static List<Vec3> bandSweepPoints(BlockPos origin, int width, int length) {
      List<Vec3> out = new ArrayList<>();
      int bands = (length + 8) / 9;

      for (int b = 0; b < bands; b++) {
         int z0 = b * 9;
         int z1 = Math.min(length - 1, z0 + 8);
         double z = origin.getZ() + (z0 + z1) / 2 + 0.5;
         double west = origin.getX() - 0.5;
         double east = origin.getX() + width + 0.5;
         if (b % 2 == 0) {
            out.add(new Vec3(west, origin.getY() + 1, z));
            out.add(new Vec3(east, origin.getY() + 1, z));
         } else {
            out.add(new Vec3(east, origin.getY() + 1, z));
            out.add(new Vec3(west, origin.getY() + 1, z));
         }
      }

      return out;
   }

   static List<Vec3> rowSweepPoints(int minX, int minZ, int maxX, int maxZ, int y) {
      List<Vec3> out = new ArrayList<>();
      int row = 0;

      for (int z = minZ + 1; z <= maxZ + 1; row++) {
         double lineZ = Math.min(z, maxZ) + 0.5;
         double west = minX - 0.5;
         double east = maxX + 1.5;
         if (row % 2 == 0) {
            out.add(new Vec3(west, y, lineZ));
            out.add(new Vec3(east, y, lineZ));
         } else {
            out.add(new Vec3(east, y, lineZ));
            out.add(new Vec3(west, y, lineZ));
         }

         z += 3;
      }

      return out;
   }

   static AiOperation harvest(List<BlockPos> field, String cropBlock, String seedItem, String cropItem, int target) {
      return harvest(field, cropBlock, seedItem, cropItem, target, true);
   }

   static AiOperation harvest(final List<BlockPos> field, String cropBlock, final String seedItem, final String cropItem, final int target, final boolean replant) {
      if (field.isEmpty()) {
         throw new IllegalArgumentException("Choose a nonempty field");
      }

      final Minecraft mc = Minecraft.getInstance();
      final Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
      int minX = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxZ = -2147483648;

      for (BlockPos p : field) {
         minX = Math.min(minX, p.getX());
         minZ = Math.min(minZ, p.getZ());
         maxX = Math.max(maxX, p.getX());
         maxZ = Math.max(maxZ, p.getZ());
      }

      final List<Vec3> path = AiFieldPlan.sweepPath(field, 3, mc.player.blockPosition());
      final int minXf = minX;
      final int minZf = minZ;
      final int maxXf = maxX;
      final int maxZf = maxZ;
      final int yf = field.getFirst().getY() + 1;
      return new AiOperation() {
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final Map<Long, Integer> brokeAtTick;
         final Map<Long, Integer> sownAtTick;
         boolean rowSweep;
         boolean rowAimSet;
         int rowHarvests;
         final int startSlot;
         final Item startItem;
         final boolean startItemDamageable;
         int pathIndex;
         int stallTicks;
         int ticks;
         int brokenCount;
         int sownCount;
         int finishChecks;
         int aimRequests;
         int externalReplants;
         int confirmedSown;
         int finalDropTicks;
         AiFieldPlan.FaceTarget lastFaceTarget;
         final Map<Long, Long> breakRevision;
         final Set<Long> ownSown;
         AiOperation walkOp;
         boolean reachedField;
         boolean cancelled;
         Vec3 lastPos;
         final Set<Long> unresolvedReplants;

         {
            this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.CALM);
            this.brokeAtTick = new HashMap<>();
            this.sownAtTick = new HashMap<>();
            this.startSlot = mc.player.getInventory().getSelectedSlot();
            this.startItem = mc.player.getMainHandItem().getItem();
            this.startItemDamageable = mc.player.getMainHandItem().isDamageableItem();
            this.pathIndex = 1;
            this.breakRevision = new HashMap<>();
            this.ownSown = new HashSet<>();
            this.unresolvedReplants = new HashSet<>();
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               this.ticks++;
               if (mc.gui.screen() != null) {
                  autismclient.util.BlockAttackGraceWindow.clear();
                  this.releaseInputs();
                  this.aim.clear();
                  this.rowAimSet = false;
                  this.lastFaceTarget = null;
                  return this.running("Harvest paused while a screen is open");
               }

               this.reconcileReplants();
               if (!this.rowSweep && this.externalReplants >= 3 && AiFieldPlan.init(field)) {
                  path.clear();
                  path.addAll(AiFieldPlan.sweepPath(field, 1, mc.player.blockPosition()));
                  this.pathIndex = 1;
                  this.rowSweep = true;
                  this.rowAimSet = false;
               }

               for (long key : this.unresolvedReplants) {
                  AiServerEvidence.BlockUpdate update = AiServerEvidence.lastBlockUpdate(BlockPos.of(key).above());
                  boolean breakConfirmed = update != null && update.revision() > this.breakRevision.get(key) && update.state().isAir();
                  int since = this.sownAtTick.getOrDefault(key, this.brokeAtTick.getOrDefault(key, this.ticks));
                  if ((!breakConfirmed || this.sownAtTick.containsKey(key)) && this.ticks - since > 120) {
                     return this.finish(AiOperation.Status.FAILED, "Server has not confirmed the crop/replant at " + BlockPos.of(key).above().toShortString());
                  }
               }

               List<ItemEntity> lying = this.nearbyDrops();
               int onGround = 0;

               for (ItemEntity item : lying) {
                  if (cropItem.equals(BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString())) {
                     onGround += item.getItem().getCount();
                  }
               }

               int held = AiGameState.countInventoryItem(cropItem);
               boolean enough = held + onGround >= target;
               if (!enough && !this.hasItem(cropItem)) {
                  return this.finish(AiOperation.Status.FAILED, "Inventory full: make room for " + cropItem + " before harvesting");
               }

               if (held >= target && this.unresolvedReplants.isEmpty() && lying.isEmpty()) {
                  return ++this.finalDropTicks < 20
                     ? this.running("Harvest: confirming the final drops")
                     : this.finish(
                        AiOperation.Status.SUCCEEDED,
                        "Harvest inventory target reached; "
                           + this.sownCount
                           + " replanted, "
                           + this.externalReplants
                           + " server replants, "
                           + this.unresolvedReplants.size()
                           + " unresolved; drops picked up"
                     );
               }

               this.finalDropTicks = 0;
               if (!this.reachedField) {
                  if (this.walkOp == null) {
                     this.walkOp = AiGoto.travelToCompat(BlockPos.containing(path.getFirst()), 1.2, false, false, false, 60);
                  }

                  AiOperation.Result r = this.walkOp.tick();
                  if (!r.terminal()) {
                     return this.running("Walking to the field: " + r.message());
                  }

                  this.walkOp.cancel();
                  this.walkOp = null;
                  this.reachedField = true;
               }

               this.releaseInputs();
               if (this.walkOp != null) {
                  AiOperation.Result r = this.walkOp.tick();
                  if (!r.terminal()) {
                     return this.running("Walking around the field obstruction: " + r.message());
                  }

                  this.walkOp.cancel();
                  this.walkOp = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.finish(AiOperation.Status.FAILED, "No safe walking route around the field obstruction");
                  }
               }

               AiItems.ItemSpec seed = seedItem == null ? null : AiItems.resolveItem(seedItem);
               boolean seedAvailable = seed != null && AiGameState.countInventoryItem(seedItem) > 0;
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.15;
               Vec3 heading = this.pathHeading();
               BlockPos best = null;
               BlockHitResult bestHit = null;
               boolean bestBreak = false;
               double bestKey = 1.7976931348623157E308;
               int ripe = 0;
               int behind = 0;
               boolean fixedRow = this.rowSweep && this.pathIndex % 2 == 1;
               if (fixedRow && !this.rowAimSet) {
                  float yaw = Math.abs(heading.x) > Math.abs(heading.z) ? 0.0F : 90.0F;
                  this.aim.setTargetRotation(new autismclient.util.RotationMath.Rotation(yaw, 80.0F));
                  this.rowAimSet = true;
               }

               BlockHitResult currentRay = autismclient.ai.engine.AiAim.raycastLookBlock();

               for (BlockPos ground : field) {
                  BlockPos at = ground.above();
                  BlockState state = mc.level.getBlockState(at);
                  long key = ground.asLong();
                  boolean ripeHere = !enough && !this.unresolvedReplants.contains(key) && state.is(crop) && AiFarm.isRipe(state);
                  boolean sow = replant
                     && !ripeHere
                     && seedAvailable
                     && state.isAir()
                     && this.canReplant(ground)
                     && autismclient.util.FarmCropCatalog.canGrowOn(crop, mc.level.getBlockState(ground));
                  if (ripeHere || sow) {
                     if (ripeHere) {
                        ripe++;
                     }

                     double along = (ground.getX() + 0.5 - mc.player.getX()) * heading.x + (ground.getZ() + 0.5 - mc.player.getZ()) * heading.z;
                     if (along < 0.6) {
                        behind++;
                     }

                     BlockPos clickPos = ripeHere ? at : ground;
                     if (!(eye.distanceToSqr(Vec3.atCenterOf(clickPos)) > (reach + 1.0) * (reach + 1.0))) {
                        boolean onRay = currentRay != null
                           && currentRay.getBlockPos().equals(clickPos)
                           && (ripeHere || currentRay.getDirection() == Direction.UP);
                        if (AiFieldPlan.init(this.rowSweep, this.pathIndex, onRay)) {
                           BlockHitResult hit = onRay
                              ? currentRay
                              : (ripeHere ? autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, at) : autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, ground, Direction.UP));
                           if (hit != null && !(eye.distanceTo(hit.getLocation()) > reach)) {
                              double score = onRay ? -1.0E9 : along * 4.0 + eye.distanceToSqr(hit.getLocation()) * 0.1;
                              if (score < bestKey) {
                                 bestKey = score;
                                 best = clickPos;
                                 bestHit = hit;
                                 bestBreak = ripeHere;
                              }
                           }
                        }
                     }
                  }
               }

               boolean acted = false;
               if (best != null) {
                  AiFieldPlan.FaceTarget nextGoal = AiFieldPlan.FaceTarget.of(best, bestBreak ? bestHit.getDirection() : Direction.UP);
                  if (AiFieldPlan.init(this.lastFaceTarget, nextGoal, autismclient.ai.engine.AiAim.raycastLookBlock())) {
                     this.aim.aimAt(bestHit.getLocation());
                     this.aimRequests++;
                     this.rowAimSet = false;
                  }

                  this.lastFaceTarget = nextGoal;
               }

               this.aim.tick();
               if (best != null) {
                  BlockHitResult ray = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (ray != null && ray.getBlockPos().equals(best)) {
                     if (bestBreak) {
                        if (this.startItemDamageable && mc.player.getInventory().getItem(this.startSlot).is(this.startItem)) {
                           autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.startSlot);
                        }

                        long ground = best.below().asLong();
                        long revision = AiServerEvidence.blockRevision(best);
                        BlockState before = mc.level.getBlockState(best);
                        if (autismclient.ai.engine.AiBlockDigger.continueBreakingBlockCompat(mc, best, ray.getDirection())
                           && AiReplantEvidence.wasHarvested(before, mc.level.getBlockState(best), crop)) {
                           this.brokenCount++;
                           acted = true;
                           if (fixedRow) {
                              this.rowHarvests++;
                           }

                           if (replant && this.unresolvedReplants.add(ground)) {
                              this.breakRevision.put(ground, revision);
                              this.brokeAtTick.put(ground, this.ticks);
                           }

                           this.ownSown.remove(ground);
                        }
                     } else if (ray.getDirection() == Direction.UP && this.canReplant(best) && mc.level.getBlockState(best.above()).isAir() && this.holdSeed(seed)) {
                        for (InteractionHand hand : InteractionHand.values()) {
                           InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, ray);
                           if (result instanceof Success success) {
                              if (success.swingSource() == SwingSource.PREDICTED) {
                                 Compat263.swing(mc.player, hand);
                              }

                              this.sownCount++;
                              acted = true;
                              this.ownSown.add(best.asLong());
                              this.sownAtTick.put(best.asLong(), this.ticks);
                              break;
                           }

                           if (result instanceof Fail) {
                              break;
                           }
                        }
                     }
                  }
               }

               if (enough && !lying.isEmpty()) {
                  ItemEntity nearest = lying.stream().min(Comparator.comparingDouble(i -> i.distanceToSqr(mc.player))).orElseThrow();
                  if (behind <= 0 || best == null) {
                     this.moveToward(nearest.position());
                  }

                  Vec3 pos = mc.player.position();
                  boolean moved = this.lastPos != null && pos.distanceToSqr(this.lastPos) > 1.0E-4;
                  this.lastPos = pos;
                  this.stallTicks = !acted && !moved ? this.stallTicks + 1 : 0;
                  return this.stallTicks > 60
                     ? this.finish(AiOperation.Status.FAILED, "Drops out of reach: " + lying.size() + " left lying")
                     : this.running("Harvest: picking up " + lying.size() + " drops");
               } else {
                  Vec3 targetx = path.get(Math.min(this.pathIndex, path.size() - 1));
                  if (Math.hypot(targetx.x - mc.player.getX(), targetx.z - mc.player.getZ()) < 0.35) {
                     if (this.pathIndex < path.size() - 1) {
                        this.pathIndex++;
                     } else {
                        int bare = this.bareReplantCount();
                        if (ripe == 0 && !this.unresolvedReplants.isEmpty()) {
                           boolean waiting = this.unresolvedReplants
                              .stream()
                              .anyMatch(key -> autismclient.util.BlockBreakGraceCalc.inGrace(this.ticks, this.brokeAtTick.get(key), this.pingMs()));
                           if (lying.isEmpty() && bare > 0 && AiGameState.countInventoryItem(seedItem) <= 0 && !waiting) {
                              return this.finish(AiOperation.Status.FAILED, "Harvest needs " + seedItem + " to replant " + bare + " plots");
                           }

                           if (bare == 0 || waiting) {
                              return this.running("Harvest: waiting for " + this.unresolvedReplants.size() + " server crop confirmations");
                           }
                        }

                        if (ripe == 0 && bare == 0 || ++this.finishChecks > 2) {
                           return this.finish(
                              ripe == 0 && bare == 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                              ripe == 0 && bare == 0
                                 ? "Field harvested: "
                                    + this.brokenCount
                                    + " broken, "
                                    + this.sownCount
                                    + " sown, "
                                    + this.externalReplants
                                    + " server replants, "
                                    + this.unresolvedReplants.size()
                                    + " unresolved"
                                 : ripe + " ripe crops and " + bare + " bare plots out of reach of the field's lines"
                           );
                        }

                        Collections.reverse(path);
                        this.pathIndex = 1;
                     }
                  }

                  if (behind <= 0 || best == null) {
                     this.moveToward(targetx);
                  }

                  Vec3 pos = mc.player.position();
                  boolean moved = this.lastPos != null && pos.distanceToSqr(this.lastPos) > 1.0E-4;
                  this.lastPos = pos;
                  this.stallTicks = !acted && !moved ? this.stallTicks + 1 : 0;
                  return this.stallTicks > 100
                     ? this.finish(AiOperation.Status.FAILED, "Harvest stalled with " + ripe + " ripe crops left")
                     : this.running("Harvest: " + this.brokenCount + " broken, " + this.sownCount + " sown, " + ripe + " ripe left");
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Harvest cancelled");
            }
         }

         void moveToward(Vec3 point) {
            if (!AiFieldWork.steerToward(mc, point) && this.walkOp == null) {
               this.walkOp = AiGoto.travelToCompat(BlockPos.containing(point), 0.65, false, false, false, 20);
            }
         }

         Vec3 pathHeading() {
            Vec3 to = path.get(Math.min(this.pathIndex, path.size() - 1));
            Vec3 from = path.get(Math.max(0, Math.min(this.pathIndex, path.size() - 1) - 1));
            Vec3 d = new Vec3(to.x - from.x, 0.0, to.z - from.z);
            return d.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : d.normalize();
         }

         List<ItemEntity> nearbyDrops() {
            AABB box = new AABB(minXf - 2, yf - 2, minZf - 2, maxXf + 3, yf + 3, maxZf + 3);
            return mc.level.getEntitiesOfClass(ItemEntity.class, box, item -> {
               String id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
               return item.isAlive() && (id.equals(cropItem) || id.equals(seedItem));
            });
         }

         void reconcileReplants() {
            Iterator<Long> iterator = this.unresolvedReplants.iterator();

            while (iterator.hasNext()) {
               long key = iterator.next();
               BlockPos ground = BlockPos.of(key);
               BlockPos above = ground.above();
               boolean ownClick = this.ownSown.contains(key);
               AiReplantEvidence.ReplantState state = AiReplantEvidence.replantState(
                  mc.level.getBlockState(above),
                  AiServerEvidence.lastBlockUpdate(above),
                  this.breakRevision.getOrDefault(key, 0L),
                  crop,
                  ownClick,
                  AiGameState.countInventoryItem(seedItem)
               );
               if (state == AiReplantEvidence.ReplantState.LOCAL || state == AiReplantEvidence.ReplantState.EXTERNAL) {
                  iterator.remove();
                  this.ownSown.remove(key);
                  this.brokeAtTick.remove(key);
                  this.sownAtTick.remove(key);
                  this.breakRevision.remove(key);
                  if (state == AiReplantEvidence.ReplantState.LOCAL) {
                     this.confirmedSown++;
                  } else {
                     this.externalReplants++;
                  }
               }
            }
         }

         boolean holdSeed(AiItems.ItemSpec seed) {
            if (seed == null) {
               return false;
            }

            if (seed.matches(mc.player.getMainHandItem())) {
               return true;
            }

            if (seed.matches(mc.player.getOffhandItem())) {
               if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
                  return true;
               } else {
                  return !autismclient.util.InventorySlotHelper.swapToOffhand(mc, mc.player.getInventory().getSelectedSlot()) ? false : seed.matches(mc.player.getMainHandItem());
               }
            } else {
               return AiItems.equipHotbar(seed) >= 0 && seed.matches(mc.player.getMainHandItem());
            }
         }

         int pingMs() {
            ClientPacketListener connection = mc.getConnection();
            PlayerInfo info = connection == null ? null : connection.getPlayerInfo(mc.player.getUUID());
            return info == null ? 0 : info.getLatency();
         }

         boolean canReplant(BlockPos ground) {
            long key = ground.asLong();
            if (this.ownSown.contains(key)) {
               return false;
            } else {
               return !this.unresolvedReplants.contains(key)
                  ? true
                  : AiReplantEvidence.isBreakConfirmed(
                     mc.level.getBlockState(ground.above()),
                     AiServerEvidence.lastBlockUpdate(ground.above()),
                     this.breakRevision.get(key),
                     this.ticks,
                     this.brokeAtTick.get(key),
                     this.pingMs(),
                     false
                  );
            }
         }

         boolean hasItem(String itemId) {
            AiItems.ItemSpec wanted = AiItems.resolveItem(itemId);
            Inventory inventory = mc.player.getInventory();

            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = inventory.getItem(slot);
               if (stack.isEmpty() || wanted.matches(stack) && stack.getCount() < stack.getMaxStackSize()) {
                  return true;
               }
            }

            return false;
         }

         int bareReplantCount() {
            if (replant && seedItem != null) {
               int n = 0;

               for (long key : this.unresolvedReplants) {
                  BlockPos ground = BlockPos.of(key);
                  if (autismclient.util.FarmCropCatalog.canGrowOn(crop, mc.level.getBlockState(ground)) && mc.level.getBlockState(ground.above()).isAir()) {
                     n++;
                  }
               }

               return n;
            } else {
               return 0;
            }
         }

         void releaseInputs() {
            AiFieldWork.releaseControls(mc);
         }

         JsonObject evidence() {
            return AiJson.objectOf(
               "broken",
               this.brokenCount,
               "sown",
               this.sownCount,
               "confirmedSown",
               this.confirmedSown,
               "externalReplants",
               this.externalReplants,
               "unresolvedReplants",
               this.unresolvedReplants.size(),
               "aimRequests",
               this.aimRequests,
               "rowSweep",
               this.rowSweep,
               "rowHarvests",
               this.rowHarvests,
               "replant",
               replant,
               "ticks",
               this.ticks
            );
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.evidence());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            this.cancel();
            return AiGameOperations.result(status, message, this.evidence());
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.walkOp != null) {
               this.walkOp.cancel();
            }

            autismclient.util.BlockAttackGraceWindow.clear();
            if (mc.player != null) {
               this.releaseInputs();
            }

            this.aim.clear();
         }
      };
   }

   static boolean steerToward(Minecraft mc, Vec3 target) {
      double dx = target.x - mc.player.getX();
      double dz = target.z - mc.player.getZ();
      if (dx * dx + dz * dz < 0.01) {
         return true;
      }

      double distance = Math.hypot(dx, dz);
      double step = Math.min(0.55, distance);
      double nextX = mc.player.getX() + dx / distance * step;
      double nextZ = mc.player.getZ() + dz / distance * step;

      for (double ox : new double[]{-0.28, 0.28}) {
         for (double oz : new double[]{-0.28, 0.28}) {
            int x = Mth.floor(nextX + ox);
            int z = Mth.floor(nextZ + oz);
            double floor = -1.0 / 0.0;
            int y = Mth.floor(mc.player.getY());

            for (int by = y - 1; by <= y + 1; by++) {
               BlockPos pos = new BlockPos(x, by, z);
               if (!mc.level.hasChunkAt(pos)) {
                  return false;
               }

               VoxelShape shape = mc.level.getBlockState(pos).getCollisionShape(mc.level, pos);
               if (!shape.isEmpty()) {
                  double top = by + shape.max(Axis.Y);
                  if (top <= mc.player.getY() + 0.6 + 1.0E-5) {
                     floor = Math.max(floor, top);
                  }
               }
            }

            if (floor < mc.player.getY() - 0.49) {
               return false;
            }
         }
      }

      double yaw = Math.toRadians(mc.player.getYRot());
      double forward = -Math.sin(yaw) * dx + Math.cos(yaw) * dz;
      double left = Math.cos(yaw) * dx + Math.sin(yaw) * dz;
      double len = Math.hypot(forward, left);
      forward /= len;
      left /= len;
      mc.options.keyUp.setDown(forward > 0.38);
      mc.options.keyDown.setDown(forward < -0.38);
      mc.options.keyLeft.setDown(left > 0.38);
      mc.options.keyRight.setDown(left < -0.38);
      return true;
   }

   static void releaseControls(Minecraft mc) {
      mc.options.keyAttack.setDown(false);
      if (mc.gameMode != null && mc.gameMode.isDestroying() && !autismclient.util.BlockAttackGraceWindow.isWithinGrace(mc)) {
         mc.gameMode.stopDestroyBlock();
      }

      mc.options.keyUp.setDown(false);
      mc.options.keyDown.setDown(false);
      mc.options.keyLeft.setDown(false);
      mc.options.keyRight.setDown(false);
      mc.options.keyJump.setDown(false);
      mc.options.keySprint.setDown(false);
      mc.player.setSprinting(false);
   }

   static AiOperation plantPlots(final List<BlockPos> plots, final BlockPos origin, final int width, final int length, String cropBlock, final String seedItem) {
      final Minecraft mc = Minecraft.getInstance();
      final Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
      final List<Vec3> path = AiFieldPlan.sweepPath(plots, 9, mc.player.blockPosition());
      return new AiOperation() {
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final Set<Long> plotKeys = new HashSet<>();
         int pathIndex;
         int stallTicks;
         int ticks;
         int tilled;
         int sownCount;
         int equipAttempts;
         int strandedPasses;
         int lostCount;
         int seedsSeen;
         int aimRequests;
         AiFieldPlan.FaceTarget lastFaceTarget;
         final Set<Long> seenCrops;
         final Set<Long> seenFarmland;
         double maxFallDistance;
         final Set<Integer> loggedItemIds;
         String lastClick;
         AiOperation walkOp;
         boolean reachedField;
         boolean cancelled;
         Vec3 lastPos;
         int missedRetargets;
         int settleTicks;

         {
            for (BlockPos p : plots) {
               this.plotKeys.add(p.asLong());
            }

            this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
            this.pathIndex = 1;
            this.seedsSeen = -1;
            this.seenCrops = new HashSet<>();
            this.seenFarmland = new HashSet<>();
            this.loggedItemIds = new HashSet<>();
            this.lastClick = "-";
         }

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               this.ticks++;
               if (mc.gui.screen() != null) {
                  autismclient.util.BlockAttackGraceWindow.clear();
                  this.releaseControls();
                  this.aim.clear();
                  return this.running("Field work paused while a screen is open");
               }

               if (!this.reachedField) {
                  if (this.walkOp == null) {
                     this.walkOp = AiGoto.travelToCompat(BlockPos.containing(path.getFirst()), 1.2, false, false, false, 60);
                  }

                  AiOperation.Result r = this.walkOp.tick();
                  if (!r.terminal()) {
                     return this.running("Walking to the field: " + r.message());
                  }

                  this.walkOp.cancel();
                  this.walkOp = null;
                  this.reachedField = true;
               }

               if (this.walkOp != null) {
                  AiOperation.Result r = this.walkOp.tick();
                  if (!r.terminal()) {
                     return this.running("Walking to a plot the lines missed: " + r.message());
                  }

                  this.walkOp.cancel();
                  this.walkOp = null;
                  this.settleTicks = 20;
               }

               this.releaseControls();
               if (!this.equipTools()) {
                  return ++this.equipAttempts > 40
                     ? this.finish(AiOperation.Status.FAILED, "Could not take the hoe and the seeds in hand")
                     : this.running("Taking the hoe and the seeds in hand");
               }

               this.equipAttempts = 0;
               this.debugTrackLosses();
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.15;
               boolean seeds = AiGameState.countInventoryItem(seedItem) > 0;
               Vec3 heading = this.pathHeading();
               BlockPos best = null;
               BlockHitResult bestHit = null;
               double bestKey = 1.7976931348623157E308;
               int pendingBehind = 0;
               int pending = 0;

               for (BlockPos p : plots) {
                  AiFieldWork.PlotState need = AiFieldWork.classifyPlot(mc.level.getBlockState(p), mc.level.getBlockState(p.above()), crop, seeds);
                  if (need != AiFieldWork.PlotState.DONE && need != AiFieldWork.PlotState.BLOCKED) {
                     pending++;
                     double along = (p.getX() + 0.5 - mc.player.getX()) * heading.x + (p.getZ() + 0.5 - mc.player.getZ()) * heading.z;
                     if (along < 0.6) {
                        pendingBehind++;
                     }

                     Vec3 top = Vec3.atCenterOf(p).add(0.0, 0.5, 0.0);
                     if (!(eye.distanceToSqr(top) > (reach + 1.0) * (reach + 1.0))) {
                        BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, p, Direction.UP);
                        if (hit != null && !(eye.distanceTo(hit.getLocation()) > reach)) {
                           double key = along * 4.0 + eye.distanceToSqr(hit.getLocation()) * 0.1;
                           if (key < bestKey) {
                              bestKey = key;
                              best = p;
                              bestHit = hit;
                           }
                        }
                     }
                  }
               }

               if (pending == 0) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Field tilled and sown: " + this.tilled + " tilled, " + this.sownCount + " sown");
               }

               boolean clicked = false;
               if (best != null) {
                  AiFieldPlan.FaceTarget nextGoal = AiFieldPlan.FaceTarget.of(best, Direction.UP);
                  if (AiFieldPlan.init(this.lastFaceTarget, nextGoal, autismclient.ai.engine.AiAim.raycastLookBlock())) {
                     this.aim.aimAt(bestHit.getLocation());
                     this.aimRequests++;
                  }

                  this.lastFaceTarget = nextGoal;
               }

               this.aim.tick();
               if (best != null) {
                  BlockHitResult ray = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (ray != null && ray.getBlockPos().equals(best) && ray.getDirection() == Direction.UP) {
                     clicked = this.workPlot(best, ray);
                  }
               }

               boolean holdBack = pendingBehind > 0 && best != null;
               Vec3 target = path.get(Math.min(this.pathIndex, path.size() - 1));
               double gap = Math.hypot(target.x - mc.player.getX(), target.z - mc.player.getZ());
               if (gap < 0.35) {
                  if (this.pathIndex < path.size() - 1) {
                     this.pathIndex++;
                  } else if (pending > 0 && !holdBack && best == null) {
                     BlockPos missed = this.strandedPasses >= 2 && this.missedRetargets < 6 ? this.nearestPendingPlot(seeds) : null;
                     if (missed != null) {
                        this.missedRetargets++;
                        this.walkOp = AiGoto.travelToCompat(missed.above(), 1.6, false, false, false, 20);
                        return this.running("Walking to a plot the lines missed at " + missed.toShortString());
                     }

                     if (++this.strandedPasses > 2) {
                        StringBuilder left = new StringBuilder();
                        int listed = 0;

                        for (BlockPos p : plots) {
                           AiFieldWork.PlotState need = AiFieldWork.classifyPlot(mc.level.getBlockState(p), mc.level.getBlockState(p.above()), crop, seeds);
                           if (need != AiFieldWork.PlotState.DONE && need != AiFieldWork.PlotState.BLOCKED && listed++ < 6) {
                              left.append(' ')
                                 .append(p.toShortString())
                                 .append(' ')
                                 .append(need)
                                 .append(" (")
                                 .append(mc.level.getBlockState(p).getBlock().getName().getString())
                                 .append(autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, p, Direction.UP) == null ? ", top unseen" : "")
                                 .append(')');
                           }
                        }

                        AutismClientAddon.LOG.info("[AI] field: unreached plots from {}:{}", mc.player.blockPosition().toShortString(), left);
                        return this.finish(AiOperation.Status.FAILED, pending + " plots could not be reached from the field's lines");
                     }

                     Collections.reverse(path);
                     this.pathIndex = 1;
                  }
               }

               if (this.settleTicks > 0) {
                  this.settleTicks--;
               } else if (!holdBack) {
                  this.moveToward(target);
               }

               Vec3 pos = mc.player.position();
               boolean moved = this.lastPos != null && pos.distanceToSqr(this.lastPos) > 1.0E-4;
               this.lastPos = pos;
               this.stallTicks = !clicked && !moved ? this.stallTicks + 1 : 0;
               return this.stallTicks > 100
                  ? this.finish(AiOperation.Status.FAILED, "Field work stalled with " + pending + " plots left")
                  : this.running("Field: " + this.tilled + " tilled, " + this.sownCount + " sown, " + pending + " left");
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Field work cancelled");
            }
         }

         BlockPos nearestPendingPlot(boolean seeds) {
            BlockPos best = null;
            double bestDistance = 1.7976931348623157E308;

            for (BlockPos p : plots) {
               AiFieldWork.PlotState need = AiFieldWork.classifyPlot(mc.level.getBlockState(p), mc.level.getBlockState(p.above()), crop, seeds);
               if (need != AiFieldWork.PlotState.DONE && need != AiFieldWork.PlotState.BLOCKED) {
                  double d = p.distToCenterSqr(mc.player.position());
                  if (d < bestDistance) {
                     bestDistance = d;
                     best = p;
                  }
               }
            }

            return best;
         }

         void debugTrackLosses() {
            int seedsNow = AiGameState.countInventoryItem(seedItem);
            boolean debug = Boolean.getBoolean("autism.navigation.debug");
            if (debug && this.seedsSeen >= 0 && seedsNow > this.seedsSeen) {
               AutismClientAddon.LOG
                  .info(
                     "[AI] field: seeds rose {} -> {} at {} (ground {})",
                     this.seedsSeen,
                     seedsNow,
                     mc.player.position(),
                     mc.level.getBlockState(mc.player.blockPosition().below())
                  );
            }

            this.seedsSeen = seedsNow;
            this.maxFallDistance = Math.max(this.maxFallDistance, mc.player.fallDistance);
            AABB box = new AABB(
               origin.getX() - 3, origin.getY() - 2, origin.getZ() - 3, origin.getX() + width + 3, origin.getY() + 4, origin.getZ() + length + 3
            );
            if (debug) {
               for (ItemEntity item : mc.level.getEntitiesOfClass(ItemEntity.class, box)) {
                  if (this.loggedItemIds.add(item.getId())) {
                     BlockPos at = item.blockPosition();
                     AutismClientAddon.LOG
                        .info(
                           "[AI] field: item {} x{} appeared at {} (block {}, below {}), last click {}, tick {}",
                           BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).getPath(),
                           item.getItem().getCount(),
                           item.position(),
                           mc.level.getBlockState(at),
                           mc.level.getBlockState(at.below()),
                           this.lastClick,
                           this.ticks
                        );
                  }
               }
            }

            for (BlockPos p : plots) {
               boolean farm = mc.level.getBlockState(p).is(Blocks.FARMLAND);
               if (farm) {
                  this.seenFarmland.add(p.asLong());
               } else if (this.seenFarmland.remove(p.asLong())) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] field: farmland lost at {} -> {}: body {} onGround {} vy {} fall {} (max {}) jump {} sprint {} keys {}{}{}{}",
                        p.toShortString(),
                        mc.level.getBlockState(p),
                        mc.player.position(),
                        mc.player.onGround(),
                        mc.player.getDeltaMovement().y,
                        mc.player.fallDistance,
                        this.maxFallDistance,
                        mc.options.keyJump.isDown(),
                        mc.player.isSprinting(),
                        mc.options.keyUp.isDown() ? "W" : "",
                        mc.options.keyLeft.isDown() ? "A" : "",
                        mc.options.keyDown.isDown() ? "S" : "",
                        mc.options.keyRight.isDown() ? "D" : ""
                     );
               }

               boolean has = mc.level.getBlockState(p.above()).is(crop);
               if (has) {
                  this.seenCrops.add(p.asLong());
               } else if (this.seenCrops.remove(p.asLong())) {
                  this.lostCount++;
                  AutismClientAddon.LOG
                     .info(
                        "[AI] field: crop lost at {}: ground {}, above {}, body {} onGround {} vy {} fall {}",
                        p.toShortString(),
                        mc.level.getBlockState(p),
                        mc.level.getBlockState(p.above()),
                        mc.player.position(),
                        mc.player.onGround(),
                        mc.player.getDeltaMovement().y,
                        mc.player.fallDistance
                     );
               }
            }
         }

         boolean workPlot(BlockPos p, BlockHitResult hit) {
            boolean wasTill = crop != Blocks.NETHER_WART && !mc.level.getBlockState(p).is(Blocks.FARMLAND);

            for (InteractionHand hand : InteractionHand.values()) {
               InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, hit);
               this.lastClick = p.toShortString()
                  + " "
                  + hand
                  + " "
                  + result.getClass().getSimpleName()
                  + " "
                  + mc.player.getItemInHand(hand).getItem()
                  + " tick "
                  + this.ticks;
               if (result instanceof Success success) {
                  if (success.swingSource() == SwingSource.PREDICTED) {
                     Compat263.swing(mc.player, hand);
                  }

                  if (wasTill) {
                     this.tilled++;
                  } else {
                     this.sownCount++;
                  }

                  return true;
               }

               if (result instanceof Fail) {
                  return false;
               }
            }

            return false;
         }

         boolean equipTools() {
            AiItems.ItemSpec seed = AiItems.resolveItem(seedItem);
            if (crop == Blocks.NETHER_WART) {
               if (seed.matches(mc.player.getMainHandItem()) || seed.matches(mc.player.getOffhandItem())) {
                  return true;
               }

               if (AiGameState.countInventoryItem(seedItem) <= 0) {
                  return true;
               }

               if (AiItems.equipHotbar(seed) >= 0) {
                  return true;
               }

               AiItems.equipOffhand(seed);
               return false;
            } else if (!mc.player.getMainHandItem().is(ItemTags.HOES) && AiItems.equipHotbar(AiItems.resolveItem("hoe")) < 0) {
               return false;
            } else if (!seed.matches(mc.player.getOffhandItem()) && AiGameState.countInventoryItem(seedItem) > 0) {
               AiItems.equipOffhand(seed);
               return false;
            } else {
               return mc.player.getMainHandItem().is(ItemTags.HOES);
            }
         }

         Vec3 pathHeading() {
            Vec3 to = path.get(Math.min(this.pathIndex, path.size() - 1));
            Vec3 from = path.get(Math.max(0, Math.min(this.pathIndex, path.size() - 1) - 1));
            Vec3 d = new Vec3(to.x - from.x, 0.0, to.z - from.z);
            return d.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : d.normalize();
         }

         void moveToward(Vec3 target) {
            if (!AiFieldWork.steerToward(mc, target) && this.walkOp == null) {
               this.walkOp = AiGoto.travelToCompat(BlockPos.containing(target), 0.65, false, false, false, 20);
            }
         }

         void releaseControls() {
            AiFieldWork.releaseControls(mc);
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.evidence());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            this.cancel();
            return AiGameOperations.result(status, message, this.evidence());
         }

         JsonObject evidence() {
            return AiJson.objectOf("tilled", this.tilled, "sown", this.sownCount, "lost", this.lostCount, "ticks", this.ticks, "aimRequests", this.aimRequests);
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.walkOp != null) {
               this.walkOp.cancel();
            }

            autismclient.util.BlockAttackGraceWindow.clear();
            if (mc.player != null) {
               this.releaseControls();
            }

            this.aim.clear();
         }
      };
   }

   enum PlotState {
      TILL,
      SOW,
      DONE,
      BLOCKED;
   }
}
