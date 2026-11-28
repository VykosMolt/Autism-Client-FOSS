package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.util.BlockPlacementPlanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;

final class AiScaffoldWalk {
   static int snapYaw45(double yaw) {
      long q = Math.round(yaw / 45.0) * 45L;
      return (int)((q % 360L + 360L) % 360L);
   }

   static AiOperation create(JsonObject args) {
      final AiScaffoldWalk.ScaffoldPlan plan = AiScaffoldWalk.ScaffoldPlan.parse(args);
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         return new AiOperation() {
            static final double init_D = 1.6;
            static final double load_D = 4.5;
            static final double read_D = -0.35;
            static final int init_I = 5;
            static final int load_I = 10;
            static final int read_I = 60;
            static final double write_D = 1.0;
            final BlockPos startPos = mc.player.blockPosition();
            final long deadlineNanos = System.nanoTime() + plan.timeoutSeconds() * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final int followYaw = AiScaffoldWalk.snapYaw45(mc.player.getYRot());
            autismclient.ai.engine.AiBridgeDriver bridgeDriver;
            final int originalSlot = mc.player.getInventory().getSelectedSlot();
            int placedCount;
            int switchedSlot = -1;
            BlockPos pendingCell;
            long pendingRevision;
            int verifyWaitTicks;
            boolean replaceTried;
            boolean load_Z;
            boolean placedThisTick;
            boolean write_Z;
            boolean cleanedUp;
            int legIndex;
            int legPlaced;
            double legBaseY = 0.0 / 0.0;
            BlockPos legEndFeet;
            BlockPos towerPlaceCell;
            double sync_D = 0.0 / 0.0;
            double stuckX = 0.0 / 0.0;
            double stuckY;
            double stuckZ;
            int stuckTicks;
            String statusLabel = "Scaffolding";

            AiScaffoldWalk.Leg currentLeg() {
               return plan.legs().get(this.legIndex);
            }

            boolean isLastLeg() {
               return this.legIndex >= plan.legs().size() - 1;
            }

            boolean isSolidLoaded(BlockPos cell) {
               return AiTerrain.isLoaded(mc.level, cell) && !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
            }

            boolean hasSupportAt(int bx, int bz) {
               BlockPos cell = new BlockPos(bx, (int)Math.floor(this.legBaseY - 1.0), bz);
               return !AiTerrain.isLoaded(mc.level, cell) ? true : !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
            }

            boolean lavaBelow(BlockPos support) {
               for (int i = 1; i <= 3; i++) {
                  if (mc.level.getBlockState(support.below(i)).is(Blocks.LAVA)) {
                     return true;
                  }
               }

               return false;
            }

            List<AiTelly.HotbarEntry> hotbarView() {
               List<AiTelly.HotbarEntry> view = new ArrayList<>(9);

               for (int i = 0; i < 9; i++) {
                  ItemStack stack = mc.player.getInventory().getItem(i);
                  boolean construction = stack.getItem() instanceof BlockItem block
                     && !(block.getBlock() instanceof FallingBlock)
                     && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(mc.level, mc.player.blockPosition())
                     && !block.getBlock().defaultBlockState().hasBlockEntity()
                     && autismclient.ai.engine.AiRestock.isConsumableBuildingBlock(stack, mc.player.getInventory());
                  view.add(new AiTelly.HotbarEntry(construction, construction ? stack.getCount() : 0));
               }

               return view;
            }

            int buildBlockCount() {
               int total = 0;

               for (AiTelly.HotbarEntry entry : this.hotbarView()) {
                  total += entry.count();
               }

               return total;
            }

            boolean selectBuildBlock() {
               int slot = AiTelly.init_I_I_List(mc.player.getInventory().getSelectedSlot(), this.hotbarView());
               if (slot < 0) {
                  return false;
               }

               if (slot != mc.player.getInventory().getSelectedSlot()) {
                  mc.player.getInventory().setSelectedSlot(slot);
                  this.switchedSlot = slot;
               }

               return true;
            }

            boolean placeAt(BlockPos cell) {
               ItemStack stack = mc.player.getInventory().getItem(mc.player.getInventory().getSelectedSlot());
               BlockPlacementPlanner.Query request = new BlockPlacementPlanner.Query(
                     cell, mc.player.getEyePosition(), Math.max(4.5, mc.player.blockInteractionRange()), BlockPlacementPlanner.blockItemValidator(stack, mc.player, InteractionHand.MAIN_HAND)
                  )
                  .withSneaking(mc.player.isShiftKeyDown())
                  .withRayBudget(new BlockPlacementPlanner.RayBudget(6));
               BlockPlacementPlanner.PlacementPlan candidate = BlockPlacementPlanner.plan(request);
               if (candidate == null) {
                  return false;
               }

               float prevYaw = mc.player.getYRot();
               float prevPitch = mc.player.getXRot();
               mc.player.setYRot(candidate.aim().emittedRotation().yaw());
               mc.player.setXRot(candidate.aim().emittedRotation().pitch());
               autismclient.ai.engine.AiCameraTurn.recordTurn(mc.player, prevYaw, prevPitch);
               this.write_Z = true;
               mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, candidate.hit());
               return true;
            }

            AiScaffoldWalk.BridgeMode effectiveMode() {
               return mc.player.getFoodData().getFoodLevel() <= 6 ? AiScaffoldWalk.BridgeMode.WALK : plan.mode();
            }

            void advanceLeg(String reason) {
               this.legIndex++;
               this.legPlaced = 0;
               this.legBaseY = 0.0 / 0.0;
               this.legEndFeet = null;
               this.towerPlaceCell = null;
               this.sync_D = 0.0 / 0.0;
               if (!this.isLastLeg()) {
                  this.statusLabel = "Leg advanced (" + reason + ")";
               }
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.cleanedUp && mc.player != null && mc.level != null) {
                  if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.finish(AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("scaffolding", mc.player.getHealth(), mc.player.getMaxHealth()));
                  }

                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Scaffold deadline reached");
                  }

                  if (!AiTerrain.isLoaded(mc.level, mc.player.blockPosition())) {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Waiting for current terrain to load", AiJson.objectOf("placed", this.placedCount)
                     );
                  }

                  this.write_Z = false;
                  Vec3 pos = mc.player.position();
                  this.placedThisTick = false;
                  if (this.pendingCell != null) {
                     if (this.isSolidLoaded(this.pendingCell)) {
                        if (!this.load_Z) {
                           this.placedCount++;
                           this.legPlaced++;
                           this.placedThisTick = true;
                        }

                        if (plan.verify() && AiServerEvidence.blockRevision(this.pendingCell) <= this.pendingRevision) {
                           if (++this.verifyWaitTicks > 60) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "Placement dispatched; no applied server block confirmation");
                           }
                        } else {
                           this.pendingCell = null;
                        }
                     } else if (++this.verifyWaitTicks > 5) {
                        if (!this.replaceTried) {
                           this.replaceTried = true;
                           if (!this.selectBuildBlock()) {
                              return this.finish(AiOperation.Status.FAILED, "Out of blocks");
                           }

                           this.placeAt(this.pendingCell);
                        } else if (this.verifyWaitTicks > 10) {
                           return this.finish(AiOperation.Status.FAILED, "Placement was not applied at " + this.pendingCell.toShortString());
                        }
                     }
                  }

                  if (!this.placedThisTick
                     && Double.compare(this.stuckX, pos.x) == 0
                     && Double.compare(this.stuckY, pos.y) == 0
                     && Double.compare(this.stuckZ, pos.z) == 0) {
                     if (++this.stuckTicks > 100) {
                        return this.finish(AiOperation.Status.FAILED, "Wedged against terrain; no progress");
                     }
                  } else {
                     this.stuckTicks = 0;
                  }

                  this.stuckX = pos.x;
                  this.stuckY = pos.y;
                  this.stuckZ = pos.z;
                  if (this.placedCount >= plan.blocks()) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
                  } else if (this.legIndex >= plan.legs().size()) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Scaffold route complete");
                  } else {
                     return this.currentLeg().tower() ? this.tickTower(pos) : this.tickBridge(pos);
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Scaffold cancelled");
               }
            }

            void selectSlot(int slot) {
               if (slot != mc.player.getInventory().getSelectedSlot()) {
                  mc.player.getInventory().setSelectedSlot(slot);
                  this.switchedSlot = slot;
               }
            }

            AiOperation.Result tickBridge(Vec3 pos) {
               if (this.bridgeDriver == null) {
                  if (this.legPlaced >= this.currentLeg().blocks() || this.legEndFeet != null) {
                     if (this.isLastLeg()) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
                     }

                     this.advanceLeg("bridge leg done");
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Leg advanced", AiJson.objectOf("placed", this.placedCount));
                  }

                  BlockPos feet = mc.player.blockPosition();
                  int[] d = autismclient.ai.engine.AiBridgeDriver.cardinalStep(plan.follow() ? this.followYaw : this.currentLeg().direction());
                  autismclient.ai.engine.AiBridger.Inner_CachePool cells = new autismclient.ai.engine.AiBridger.Inner_CachePool(
                     feet.getX(), feet.getZ(), feet.getY() - 1, d[0], d[1], this.currentLeg().blocks() - this.legPlaced
                  );
                  this.bridgeDriver = new autismclient.ai.engine.AiBridgeDriver(
                     cells,
                     autismclient.ai.engine.AiBridgeDriver.chooseStyle(
                        this.effectiveMode() == AiScaffoldWalk.BridgeMode.TELLY, cells, mc.player, this.buildBlockCount()
                     ),
                     this.aim
                  );
                  this.legBaseY = feet.getY();
               }

               if (pos.y < this.legBaseY - 2.0) {
                  return this.finish(AiOperation.Status.FAILED, "Fell off the scaffold");
               } else {
                  BlockPos support = this.bridgeDriver.nextSupport();
                  if (support != null && this.lavaBelow(support)) {
                     return this.finish(AiOperation.Status.FAILED, "Refusing to scaffold over lava at " + support.toShortString());
                  } else {
                     int slot = AiTelly.init_I_I_List(mc.player.getInventory().getSelectedSlot(), this.hotbarView());
                     if (slot < 0 && support != null) {
                        autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                        mc.options.keyUp.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                        mc.options.keyJump.setDown(false);
                        mc.options.keyShift.setDown(true);
                        Vec3 v = mc.player.getDeltaMovement();
                        return mc.player.onGround() && v.x * v.x + v.z * v.z < 1.0E-4
                           ? this.finish(AiOperation.Status.FAILED, "Out of blocks")
                           : AiGameOperations.result(
                              AiOperation.Status.RUNNING, "Out of blocks, stopping on the scaffold", AiJson.objectOf("placed", this.placedCount)
                           );
                     } else {
                        autismclient.ai.engine.AiBridgeDriver.BridgeStatus step = this.bridgeDriver.step(slot, this::selectSlot, cell -> {
                           this.placedCount++;
                           this.legPlaced++;
                           this.placedThisTick = true;
                        });
                        if (step == autismclient.ai.engine.AiBridgeDriver.BridgeStatus.INVALID_FOOTING) {
                           return this.finish(AiOperation.Status.FAILED, "Scaffold bridge needs footing at the support's top level");
                        } else if (step == autismclient.ai.engine.AiBridgeDriver.BridgeStatus.FELL) {
                           return this.finish(AiOperation.Status.FAILED, "Fell off the scaffold");
                        } else {
                           String style = this.bridgeDriver.getBridger().getStyle().name().toLowerCase(Locale.ROOT);
                           if (step == autismclient.ai.engine.AiBridgeDriver.BridgeStatus.DONE) {
                              this.legEndFeet = mc.player.blockPosition();
                              this.bridgeDriver = null;
                              return AiGameOperations.result(
                                 AiOperation.Status.RUNNING, "Leg done", AiJson.objectOf("placed", this.placedCount, "leg", this.legIndex)
                              );
                           } else {
                              return AiGameOperations.result(
                                 AiOperation.Status.RUNNING,
                                 this.statusLabel + ", " + this.placedCount + " placed",
                                 AiJson.objectOf(
                                    "placed",
                                    this.placedCount,
                                    "mode",
                                    style,
                                    "leg",
                                    this.legIndex,
                                    "remainingBlocks",
                                    this.buildBlockCount(),
                                    "nextCell",
                                    support == null ? "secured" : support.toShortString()
                                 )
                              );
                           }
                        }
                     }
                  }
               }
            }

            AiOperation.Result tickTower(Vec3 pos) {
               if (this.legPlaced >= this.currentLeg().blocks()) {
                  if (this.isLastLeg()) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Placed the requested blocks");
                  }

                  this.advanceLeg("tower done");
                  return AiGameOperations.result(AiOperation.Status.RUNNING, "Leg advanced", AiJson.objectOf("placed", this.placedCount));
               } else {
                  BlockPos feet = mc.player.blockPosition();
                  boolean onGround = mc.player.onGround();
                  if (this.isSolidLoaded(feet.above(2))) {
                     if (this.isLastLeg()) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Ceiling reached");
                     }

                     this.advanceLeg("ceiling reached");
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Leg advanced (ceiling)", AiJson.objectOf("placed", this.placedCount));
                  } else {
                     boolean jump = false;
                     if (onGround) {
                        this.towerPlaceCell = feet;
                        this.sync_D = pos.y;
                        jump = true;
                     } else if (this.towerPlaceCell != null
                        && !Double.isNaN(this.sync_D)
                        && pos.y >= this.sync_D + 1.0
                        && !this.isSolidLoaded(this.towerPlaceCell)
                        && this.pendingCell == null) {
                        if (this.lavaBelow(this.towerPlaceCell)) {
                           return this.finish(AiOperation.Status.FAILED, "Refusing to scaffold over lava at " + this.towerPlaceCell.toShortString());
                        }

                        if (!this.selectBuildBlock()) {
                           return this.finish(AiOperation.Status.FAILED, "Out of blocks");
                        }

                        if (this.placeAt(this.towerPlaceCell)) {
                           this.pendingCell = this.towerPlaceCell;
                           this.pendingRevision = AiServerEvidence.blockRevision(this.towerPlaceCell);
                           this.verifyWaitTicks = 0;
                           this.replaceTried = false;
                           this.load_Z = false;
                           if (this.isSolidLoaded(this.towerPlaceCell)) {
                              this.placedCount++;
                              this.legPlaced++;
                              this.load_Z = true;
                              this.placedThisTick = true;
                              if (!plan.verify()) {
                                 this.pendingCell = null;
                              }
                           }
                        }
                     }

                     mc.options.keyUp.setDown(false);
                     mc.options.keySprint.setDown(false);
                     mc.options.keyJump.setDown(jump);
                     mc.options.keyShift.setDown(true);
                     if (!this.write_Z) {
                        mc.player.setXRot(60.0F);
                     }

                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING,
                        this.statusLabel + " (tower), " + this.placedCount + " placed",
                        AiJson.objectOf("placed", this.placedCount, "mode", "tower", "leg", this.legIndex, "remainingBlocks", this.buildBlockCount())
                     );
                  }
               }
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(
                  status,
                  message,
                  AiJson.objectOf(
                     "placed",
                     this.placedCount,
                     "from",
                     this.startPos.toShortString(),
                     "to",
                     mc.player != null ? mc.player.blockPosition().toShortString() : this.startPos.toShortString()
                  )
               );
            }

            @Override
            public void cancel() {
               if (!this.cleanedUp) {
                  this.cleanedUp = true;
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  this.aim.clear();
                  if (mc.player != null && this.switchedSlot >= 0 && mc.player.getInventory().getSelectedSlot() == this.switchedSlot) {
                     mc.player.getInventory().setSelectedSlot(this.originalSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Scaffolding requires a connected living player");
      }
   }

   record ScaffoldPlan(List<AiScaffoldWalk.Leg> legs, boolean follow, int blocks, AiScaffoldWalk.BridgeMode mode, boolean verify, int timeoutSeconds) {
      static AiScaffoldWalk.ScaffoldPlan parse(JsonObject args) {
         boolean hasDirection = args.has("direction");
         boolean follow = AiJson.getBoolean(args, "follow", false);
         boolean tower = AiJson.getBoolean(args, "tower", false);
         boolean hasLegs = args.has("legs");
         int kinds = (hasDirection ? 1 : 0) + (follow ? 1 : 0) + (tower ? 1 : 0) + (hasLegs ? 1 : 0);
         if (kinds != 1) {
            throw new IllegalArgumentException("Pass exactly one of direction, follow:true, tower:true or legs");
         }

         int total = Math.max(1, Math.min(256, AiJson.getInt(args, "blocks", 32)));
         List<AiScaffoldWalk.Leg> legs = new ArrayList<>();
         if (hasLegs) {
            JsonArray raw = args.getAsJsonArray("legs");
            if (raw == null || raw.isEmpty()) {
               throw new IllegalArgumentException("legs must be a non-empty array");
            }

            for (JsonElement element : raw) {
               if (!element.isJsonObject()) {
                  throw new IllegalArgumentException("each leg must be an object");
               }

               JsonObject leg = element.getAsJsonObject();
               boolean legTower = AiJson.getBoolean(leg, "tower", false);
               int legBlocks = Math.max(1, Math.min(256, AiJson.getInt(leg, "blocks", 1)));
               if (!legTower && !leg.has("direction")) {
                  throw new IllegalArgumentException("a bridge leg needs direction (yaw degrees)");
               }

               legs.add(new AiScaffoldWalk.Leg(legTower, legTower ? 0.0 : AiJson.getDouble(leg, "direction", 0.0), legBlocks));
            }
         } else if (tower) {
            legs.add(new AiScaffoldWalk.Leg(true, 0.0, total));
         } else {
            legs.add(new AiScaffoldWalk.Leg(false, hasDirection ? AiJson.getDouble(args, "direction", 0.0) : 0.0, total));
         }

         AiScaffoldWalk.BridgeMode var10000;
         switch (AiJson.getString(args, "mode", "walk").toLowerCase(Locale.ROOT)) {
            case "walk":
               var10000 = AiScaffoldWalk.BridgeMode.WALK;
               break;
            case "sprint":
               var10000 = AiScaffoldWalk.BridgeMode.SPRINT;
               break;
            case "telly":
               var10000 = AiScaffoldWalk.BridgeMode.TELLY;
               break;
            default:
               throw new IllegalArgumentException("mode must be walk, sprint or telly");
         }

         AiScaffoldWalk.BridgeMode mode = var10000;
         return new AiScaffoldWalk.ScaffoldPlan(
            List.copyOf(legs),
            follow,
            total,
            mode,
            AiJson.getBoolean(args, "verify", false),
            Math.max(1, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 60)))
         );
      }
   }

   record Leg(boolean tower, double direction, int blocks) {
      double dirX() {
         return -Math.sin(Math.toRadians(this.direction));
      }

      double dirZ() {
         return Math.cos(Math.toRadians(this.direction));
      }
   }

   enum BridgeMode {
      WALK,
      SPRINT,
      TELLY;
   }
}
