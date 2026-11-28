package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiDimension {
   private static final int PORTAL_WAIT_TICKS = 400;
   private static final int update = 40;
   private static final int sync = 48;
   static final int[] FRAME_DY_ORDER = new int[]{1, 0, 2, -1};
   static volatile BlockPos frameOrigin;
   static volatile BlockPos lastPortalSite;
   static volatile Direction frameAxis;
   static volatile String frameWorldKey = "";
   static volatile String frameDimension = "";
   static volatile Object frameConnection;
   static final Set<Long> REJECTED_FRAME_SITES = ConcurrentHashMap.newKeySet();
   static final int init_I = 20;
   static final int load_I = 3;
   private static final int flush = 120;
   private static final int close = 40;
   static final int read = 10;
   static final Set<String> GOLD_ARMOR = Set.of("minecraft:golden_helmet", "minecraft:golden_chestplate", "minecraft:golden_leggings", "minecraft:golden_boots");
   private static final Map<String, Block> PORTAL_BLOCKS = new LinkedHashMap<>();

   private AiDimension() {
   }

   static void refreshWorld() {
      Minecraft mc = Minecraft.getInstance();
      if (!frameWorldKey.equals(AiWorldIdentity.currentWorldKey(mc)) || !frameDimension.equals(currentDimension()) || frameConnection != mc.getConnection()) {
         frameOrigin = null;
         frameAxis = null;
         lastPortalSite = null;
         REJECTED_FRAME_SITES.clear();
      }
   }

   static Set<Long> frameCells(BlockPos origin, Direction axis) {
      Set<Long> cells = new HashSet<>();
      if (origin != null && axis != null) {
         for (BlockPos cell : frameBlocks(origin, axis)) {
            cells.add(cell.asLong());
         }

         for (BlockPos cell : portalInterior(origin, axis)) {
            cells.add(cell.asLong());
         }

         return cells;
      } else {
         return cells;
      }
   }

   static int framedObsidian() {
      refreshWorld();
      Minecraft mc = Minecraft.getInstance();
      BlockPos origin = frameOrigin;
      Direction axis = frameAxis;
      if (mc.level != null && origin != null && axis != null && frameWorldKey.equals(AiWorldIdentity.currentWorldKey(mc)) && frameDimension.equals(currentDimension()) && AiTerrain.isLoaded(mc.level, origin)) {
         if (mc.level.getBlockState(origin).is(Blocks.NETHER_PORTAL)) {
            return 0;
         }

         int n = 0;

         for (BlockPos cell : frameBlocks(origin, axis)) {
            if (mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
               n++;
            }
         }

         return n;
      } else {
         return 0;
      }
   }

   static boolean wearsGold(List<String> armorItemIds) {
      for (String id : armorItemIds) {
         if (GOLD_ARMOR.contains(id)) {
            return true;
         }
      }

      return false;
   }

   static AiOperation changeDimension(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiDimension.DimensionAction action = parseAction(AiJson.getString(args, "action", "enter"));
         refreshWorld();
         final String target = normalizeDimension(AiJson.getString(args, "target", "minecraft:the_nether"));
         final boolean build = AiJson.getBoolean(args, "build", false);
         final int timeoutSeconds = Math.max(10, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 300)));
         final Block portalBlock = PORTAL_BLOCKS.get(target);
         if (portalBlock == null) {
            throw new IllegalArgumentException("Unknown dimension '" + target + "'; known: " + String.join(", ", PORTAL_BLOCKS.keySet()));
         } else {
            return new AiOperation() {
               final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
               final String startDimension = AiDimension.currentDimension();
               final String startWorldKey = AiWorldIdentity.currentWorldKey(mc);
               final Object startConnection = mc.getConnection();
               final Object startLevel = mc.level;
               final Vec3 startPos = mc.player.position();
               final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
               final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
               final AiDimension.ChildTracker children = new AiDimension.ChildTracker();
               final AiDimension.IgnitionReceipt ignitionReceipt = new AiDimension.IgnitionReceipt();
               final List<BlockPos> frameCells = new ArrayList<>();
               AiDimension.FramePlacementOp framePlacement;
               Block placingBlock;
               JsonObject lastPlacementReceipt;
               BlockPos ignitionPos;
               BlockState ignitionBeforeState;
               AiOperation portalTravel;
               BlockPos portalPos;
               JsonObject entranceEvidence = new JsonObject();
               BlockPos site;
               final Set<Long> rejectedFrameSites;
               Direction buildAxis;
               int frameCursor;
               int placeTicks;
               int portalWaitTicks;
               int walkInTicks;
               int stuckTicks;
               boolean portalBottomRetried;
               BlockPos feetNow;
               final autismclient.ai.engine.AiAim portalAim;
               int portalSearchCooldown;
               int arrivalWaitTicks;
               int lightWaitTicks;
               boolean building;
               boolean portalLit;
               boolean cancelled;
               final ArrayDeque<BlockPos> digQueue;
               AiOperation siteDigTravel;
               BlockPos origin;
               Direction axis;
               boolean siteDigTried;
               boolean originStandTried;
               int siteClearFailures;
               boolean unusedFlag;
               final Set<BlockPos> failedCells;
               AiOperation frameApproach;
               boolean interiorClearTried;
               final Set<BlockPos> frameCornerCells;
               BlockPos scaffoldPos;
               final Set<BlockPos> scaffoldTried;
               int bodyOverlapRetries;
               int frameRepairs;
               int lightingClicks;
               int lightingAimTicks;
               int lightingStanceMoves;
               int lightingFaceIndex;

               {
                  this.rejectedFrameSites = AiDimension.REJECTED_FRAME_SITES;
                  this.buildAxis = Direction.EAST;
                  this.portalAim = new autismclient.ai.engine.AiAim();
                  this.digQueue = new ArrayDeque<>();
                  this.failedCells = new HashSet<>();
                  this.frameCornerCells = new HashSet<>();
                  this.scaffoldTried = new HashSet<>();
               }

               BlockPos portalBottom(BlockPos portal) {
                  BlockPos bottom = portal;

                  while (mc.level.getBlockState(bottom.below()).getBlock() == mc.level.getBlockState(portal).getBlock()) {
                     bottom = bottom.below();
                  }

                  BlockState state = mc.level.getBlockState(bottom);
                  boolean alongZ = state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS) && state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Axis.Z;
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (int side : new int[]{-1, 1}) {
                     BlockPos stand = alongZ ? bottom.offset(side, 0, 0) : bottom.offset(0, 0, side);
                     if (this.isPlaceable(stand)
                        && this.isPlaceable(stand.above())
                        && !mc.level.getBlockState(stand.below()).getCollisionShape(mc.level, stand.below()).isEmpty()) {
                        double distance = stand.distSqr(mc.player.blockPosition());
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = stand;
                        }
                     }
                  }

                  return best;
               }

               AiOperation travelProtectingPortal(BlockPos targetCell, double radius, int seconds) {
                  HashSet<Long> keep = new HashSet<>();

                  for (BlockPos cell : BlockPos.betweenClosed(this.portalPos.offset(-3, -5, -3), this.portalPos.offset(3, 5, 3))) {
                     Block block = mc.level.getBlockState(cell).getBlock();
                     if (block == portalBlock || block == Blocks.OBSIDIAN || block == Blocks.END_PORTAL_FRAME) {
                        keep.add(cell.asLong());
                     }
                  }

                  return AiRunDiagnostics.trace(
                     "dimension",
                     "approach portal " + this.portalPos.toShortString(),
                     AiGoto.travelToCompat(
                        targetCell,
                        radius,
                        true,
                        true,
                        true,
                        seconds,
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                        true,
                        2.0,
                        AiGoto.protectingCellsCompat(autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(targetCell, radius), keep)
                     )
                  );
               }

               @Override
               public AiOperation.Result tick() {
                  if (!this.cancelled && mc.player != null && mc.level != null) {
                     if (!this.startWorldKey.equals(AiWorldIdentity.currentWorldKey(mc)) || this.startConnection != mc.getConnection()) {
                        return this.finish(AiOperation.Status.CANCELLED, "World or connection identity changed during portal travel");
                     }

                     if (!mc.player.isAlive()) {
                        return this.finish(AiOperation.Status.FAILED, "Player died during portal travel");
                     }

                     if (System.nanoTime() > this.deadline) {
                        return this.finish(AiOperation.Status.TIMED_OUT, "Still in " + AiDimension.currentDimension() + " at the deadline");
                     }

                     if (this.children.hasUnresolved()) {
                        return this.finish(AiOperation.Status.UNCERTAIN, "Portal work has pending changes that need observation");
                     }

                     if (action == AiDimension.DimensionAction.FIND) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Read portals in loaded terrain");
                     }

                     if (!AiDimension.currentDimension().equals(this.startDimension)) {
                        if (this.walkInTicks > 0) {
                           this.walkInTicks = 0;
                           mc.options.keyUp.setDown(false);
                           mc.options.keyJump.setDown(false);
                           this.portalAim.clear();
                        }

                        if (++this.arrivalWaitTicks < 40) {
                           return this.running("Arrived in " + AiDimension.currentDimension() + "; waiting for the world");
                        } else {
                           String hazard = this.arrivalHazard();
                           AiPortalLinks.recordLink(this.startDimension, this.portalPos, AiDimension.currentDimension(), mc.player.blockPosition(), hazard == null, this.entranceEvidence);
                           if (!AiDimension.currentDimension().equals(target)) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "Portal led to " + AiDimension.currentDimension() + " rather than " + target);
                           } else {
                              return hazard != null
                                 ? this.finishWith(AiOperation.Status.UNCERTAIN, "Arrived in " + target + " but " + hazard, this.netherArrivalEvidence())
                                 : this.finishWith(AiOperation.Status.SUCCEEDED, "Arrived in " + target, this.netherArrivalEvidence());
                           }
                        }
                     } else {
                        this.arrivalWaitTicks = 0;
                        if (AiDimension.currentDimension().equals(target)) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "Already in " + target);
                        }

                        if (this.siteDigTravel != null || !this.digQueue.isEmpty()) {
                           AiOperation.Result dug = this.tickSiteDig();
                           if (dug != null) {
                              return dug;
                           }
                        }

                        if (this.building) {
                           AiOperation.Result result = this.tickBuild();
                           if (result != null) {
                              return result;
                           }
                        }

                        if ((this.portalPos == null || mc.level.getBlockState(this.portalPos).getBlock() != portalBlock) && this.portalSearchCooldown-- <= 0) {
                           this.portalPos = this.findNearestBlock(portalBlock);
                           this.portalSearchCooldown = 40;
                        }

                        if (this.portalPos == null && this.portalSearchCooldown < 40) {
                           return this.running("Looking for a portal");
                        }

                        if (this.portalPos != null) {
                           BlockPos bottom = this.portalPos;

                           while (mc.level.getBlockState(bottom.below()).getBlock() == portalBlock) {
                              bottom = bottom.below();
                           }

                           if (mc.player.blockPosition().distSqr(bottom) > 2.0) {
                              if (this.portalTravel == null) {
                                 BlockPos feetNow = mc.player.blockPosition();
                                 this.stuckTicks = feetNow.equals(this.feetNow) ? this.stuckTicks + 1 : 0;
                                 this.feetNow = feetNow;
                                 BlockPos stance = this.stuckTicks >= 4 ? this.portalBottom(this.portalPos) : null;
                                 if (stance != null) {
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: the walk into the portal restarted {} times from {}; to the stance {}",
                                          this.stuckTicks,
                                          feetNow.toShortString(),
                                          stance.toShortString()
                                       );
                                    this.stuckTicks = 0;
                                    this.portalTravel = this.travelProtectingPortal(stance, 0.5, Math.min(60, this.secondsLeft()));
                                 } else {
                                    this.portalTravel = this.travelProtectingPortal(bottom, 1.6, Math.min(180, this.secondsLeft()));
                                 }
                              }

                              AiOperation.Result legResult = this.children.tickChild(this.portalTravel);
                              if (!legResult.terminal()) {
                                 return this.running("Walking into the portal");
                              }

                              this.children.cancelChild(this.portalTravel);
                              this.portalTravel = null;
                              if (legResult.status() == AiOperation.Status.UNCERTAIN || legResult.status() == AiOperation.Status.CANCELLED) {
                                 return this.finish(legResult.status(), "Portal approach interrupted: " + legResult.message());
                              }

                              if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                                 BlockPos stance = this.portalBottomRetried ? null : this.portalBottom(this.portalPos);
                                 this.portalBottomRetried = true;
                                 if (stance != null) {
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: no route into the portal from {}; to the stance {}",
                                          mc.player.blockPosition().toShortString(),
                                          stance.toShortString()
                                       );
                                    this.portalTravel = this.travelProtectingPortal(stance, 0.5, Math.min(60, this.secondsLeft()));
                                    return this.running("To a stance by the portal");
                                 } else {
                                    return this.finish(AiOperation.Status.FAILED, "No route into the portal: " + legResult.message());
                                 }
                              } else {
                                 return this.running("At the portal");
                              }
                           } else {
                              if (this.portalTravel != null) {
                                 this.children.cancelChild(this.portalTravel);
                                 this.portalTravel = null;
                                 if (this.children.hasUnresolved()) {
                                    return this.finish(AiOperation.Status.UNCERTAIN, "Portal approach needs observation before stepping in");
                                 }
                              }

                              BlockPos feet = mc.player.blockPosition();
                              boolean inside = mc.level.getBlockState(feet).getBlock() == portalBlock
                                 || mc.level.getBlockState(feet.above()).getBlock() == portalBlock;
                              if (inside && this.entranceEvidence.isEmpty()) {
                                 this.entranceEvidence = AiPortalLinks.entranceEvidence(this.portalPos);
                              }

                              if (!inside) {
                                 if (++this.walkInTicks > 60) {
                                    this.walkInTicks = 0;
                                    mc.options.keyUp.setDown(false);
                                    mc.options.keyJump.setDown(false);
                                    this.portalPos = null;
                                    return this.running("Lost the portal's plane; looking again");
                                 } else {
                                    this.portalAim.aimAt(Vec3.atCenterOf(bottom));
                                    this.portalAim.tick();
                                    mc.options.keyUp.setDown(true);
                                    mc.options.keyJump.setDown(mc.player.horizontalCollision && mc.player.onGround());
                                    this.portalWaitTicks = 0;
                                    return this.running("Stepping into the portal");
                                 }
                              } else {
                                 if (this.walkInTicks > 0) {
                                    this.walkInTicks = 0;
                                    mc.options.keyUp.setDown(false);
                                    mc.options.keyJump.setDown(false);
                                    this.portalAim.clear();
                                 }

                                 if (this.portalWaitTicks > 200 && this.portalWaitTicks % 20 < 2 && mc.player.onGround()) {
                                    mc.options.keyJump.setDown(true);
                                    return this.running("In the portal, waiting for the transfer (" + this.portalWaitTicks / 20 + "s); hopping through the plane");
                                 } else {
                                    mc.options.keyJump.setDown(false);
                                    if (++this.portalWaitTicks > 400) {
                                       BlockState planeState = mc.level.getBlockState(feet);
                                       AutismClientAddon.LOG
                                          .info(
                                             "[AI] dimension: stood in the portal without transferring at {} (blockAtFeet {}, above {}, below {}, portalBlock {}, axis {}, playerY {}, bottom {}, distance {})",
                                             feet.toShortString(),
                                             planeState.getBlock(),
                                             mc.level.getBlockState(feet.above()).getBlock(),
                                             mc.level.getBlockState(feet.below()).getBlock(),
                                             portalBlock,
                                             planeState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
                                                ? planeState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                                                : "?",
                                             mc.player.getY(),
                                             bottom.toShortString(),
                                             (int)Math.sqrt(mc.player.blockPosition().distSqr(bottom))
                                          );
                                       return this.finish(AiOperation.Status.FAILED, "Stood in the portal for 20s without transferring");
                                    } else {
                                       return this.running("In the portal, waiting for the transfer (" + this.portalWaitTicks / 20 + "s)");
                                    }
                                 }
                              }
                           }
                        } else {
                           if (!build) {
                              return this.finish(
                                 AiOperation.Status.FAILED,
                                 "No " + BuiltInRegistries.BLOCK.getKey(portalBlock) + " within 48 blocks; set build:true to construct one"
                              );
                           }

                           if (portalBlock != Blocks.NETHER_PORTAL) {
                              return this.finish(AiOperation.Status.FAILED, "Only nether portals can be constructed; an end portal must be found");
                           }

                           AiOperation.Result prepared = this.prepareBuild();
                           return prepared != null ? prepared : this.running("Building a nether portal");
                        }
                     }
                  } else {
                     return this.finish(AiOperation.Status.CANCELLED, "Dimension travel cancelled");
                  }
               }

               String arrivalHazard() {
                  return AiPortalLinks.arrivalHazard();
               }

               JsonObject netherArrivalEvidence() {
                  if (!"minecraft:the_nether".equals(AiDimension.currentDimension())) {
                     return null;
                  }

                  List<String> armor = new ArrayList<>();

                  for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                     ItemStack stack = mc.player.getItemBySlot(slot);
                     if (!stack.isEmpty()) {
                        armor.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                     }
                  }

                  return AiDimension.wearsGold(armor) ? null : AiJson.objectOf("warning", "No gold armor equipped; piglins attack on sight in the nether");
               }

               AiOperation.Result prepareBuild() {
                  int obsidian = AiGameState.countInventoryItem("minecraft:obsidian");
                  BlockPos reused = this.findReusableFrame();
                  int needed = reused == null ? 10 : 10 - this.obsidianInFrame(reused, this.buildAxis);
                  if (obsidian < needed) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:obsidian", needed - obsidian);
                     return this.finishWith(
                        AiOperation.Status.FAILED,
                        "A nether portal needs " + needed + " more obsidian; holding " + obsidian,
                        AiJson.objectOf("missing", missing)
                     );
                  }

                  if (AiGameState.countInventoryItem("minecraft:flint_and_steel") <= 0 && AiGameState.countInventoryItem("minecraft:fire_charge") <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty("minecraft:flint_and_steel", 1);
                     return this.finishWith(AiOperation.Status.FAILED, "Nothing to light the portal with", AiJson.objectOf("missing", missing));
                  }

                  BlockPos site = this.findReusableFrame();
                  if (site == null && this.origin != null && this.frameFits(this.origin, this.axis, true) && !this.originStandTried) {
                     this.originStandTried = true;
                     BlockPos stand = this.findFrameStand(this.origin, this.axis);
                     if (stand != null) {
                        this.siteDigTravel = AiGoto.travelToCompat(stand, 0.5, false, true, false, 20);
                        return this.running("Stepping out of the dug portal site");
                     }
                  }

                  if (site == null && !this.siteDigTried) {
                     this.siteDigTried = true;
                     List<BlockPos> digs = this.findDiggableSite();
                     if (digs != null) {
                        this.digQueue.addAll(digs);
                        AutismClientAddon.LOG
                           .info("[AI] dimension: digging {} blocks out for a portal site at {}", digs.size(), this.origin.toShortString());
                        return this.running("Clearing a site for the portal frame");
                     }
                  }

                  if (site == null) {
                     return this.finish(AiOperation.Status.FAILED, "No clear 4x5 space near the player for a portal frame");
                  }

                  this.site = site;
                  AiDimension.frameOrigin = site.immutable();
                  AiDimension.frameAxis = this.buildAxis;
                  AiDimension.frameWorldKey = this.startWorldKey;
                  AiDimension.frameDimension = this.startDimension;
                  AiDimension.frameConnection = mc.getConnection();
                  this.frameCells.clear();
                  this.frameCells.addAll(AiDimension.frameBlocks(site, this.buildAxis));
                  this.frameCornerCells.clear();

                  for (BlockPos corner : AiDimension.frameCorners(site, this.buildAxis)) {
                     if (this.isPlaceable(corner)) {
                        this.frameCornerCells.add(corner.immutable());
                     }
                  }

                  this.frameCells.addAll(this.frameCornerCells);
                  int top = site.getY() + 3;
                  this.frameCells
                     .sort(Comparator.<BlockPos>comparingInt(c -> c.getY()).thenComparingInt(c -> this.frameCornerCells.contains(c) == (c.getY() == top) ? 0 : 1));
                  this.frameCursor = 0;
                  this.unusedFlag = false;
                  this.building = true;

                  for (BlockPos cell : AiDimension.portalInterior(site, this.buildAxis)) {
                     if (!this.isOpenInterior(cell)) {
                        this.digQueue.add(cell.immutable());
                     }
                  }

                  if (!this.digQueue.isEmpty()) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] dimension: {} blocks in the frame's interior at {}; digging them out first", this.digQueue.size(), site.toShortString()
                        );
                     return this.running("Clearing the frame's interior");
                  } else {
                     return null;
                  }
               }

               AiOperation.Result tickBuild() {
                  if (this.frameApproach != null) {
                     AiOperation.Result r = this.children.tickChild(this.frameApproach);
                     if (!r.terminal()) {
                        return this.running("Moving to reach a frame block");
                     }

                     this.children.cancelChild(this.frameApproach);
                     this.frameApproach = null;
                     if (r.status() == AiOperation.Status.UNCERTAIN || r.status() == AiOperation.Status.CANCELLED) {
                        return this.finish(r.status(), "Frame approach interrupted: " + r.message());
                     }
                  }

                  if (this.framePlacement != null
                     && (
                        this.framePlacement.requiresReconciliationAfterEmergency()
                           || this.placer.targetPos() != null
                              && mc.level.getBlockState(this.placer.targetPos()).getBlock() == this.placingBlock
                     )) {
                     AiOperation.Result r = this.children.tickChild(this.framePlacement);
                     if (!r.terminal()) {
                        return this.running("Waiting for the portal frame placement acknowledgement");
                     }

                     this.cancelPlacement();
                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.finish(r.status(), "Portal frame placement interrupted: " + r.message());
                     }
                  }

                  if (this.frameCursor < this.frameCells.size()) {
                     BlockPos cell = this.frameCells.get(this.frameCursor);
                     if (this.frameCornerCells.contains(cell)) {
                        AiOperation.Result out = this.stepOutOfCell(cell);
                        if (out != null) {
                           return out;
                        }

                        Item block = AiShield.bestBlock(mc);
                        if (block != null && this.canPlaceNow(cell)) {
                           this.startPlacing(cell, block);
                           AiOperation.Result placed = this.children.tickChild(this.framePlacement);
                           if (placed.status() == AiOperation.Status.UNCERTAIN) {
                              return this.finish(placed.status(), placed.message());
                           }

                           autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placeStatusOf(placed);
                           if ((status == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED || this.placeTicks + 1 > 120) && this.failedCells.add(cell)) {
                              this.cancelPlacement();
                              this.placeTicks = 0;
                              this.frameApproach = AiGoto.approachToPlaceCompat(
                                 cell, block, 20, null, false, AiDimension.frameCells(this.site, this.buildAxis), this.frameAndCornerCells()
                              );
                              return this.running("Moving to reach the frame's corner " + cell.toShortString());
                           }

                           if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED
                              || status == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
                              || ++this.placeTicks > 120) {
                              this.cancelPlacement();
                              this.frameCursor++;
                              this.placeTicks = 0;
                           }

                           return this.running("Placing the frame's corner " + cell.toShortString());
                        } else {
                           this.cancelPlacement();
                           this.frameCursor++;
                           this.placeTicks = 0;
                           return this.running("Corner " + cell.toShortString() + " skipped");
                        }
                     } else {
                        if (this.scaffoldPos == null && !mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && !this.hasSolidNeighbour(cell) && this.scaffoldTried.add(cell)) {
                           this.scaffoldPos = this.findScaffoldFor(cell);
                        }

                        if (this.scaffoldPos != null) {
                           AiOperation.Result out = this.stepOutOfCell(this.scaffoldPos);
                           if (out != null) {
                              return out;
                           }

                           Item block = AiShield.bestBlock(mc);
                           if (block != null && this.canPlaceNow(this.scaffoldPos)) {
                              this.startPlacing(this.scaffoldPos, block);
                              AiOperation.Result placed = this.children.tickChild(this.framePlacement);
                              if (placed.status() == AiOperation.Status.UNCERTAIN) {
                                 return this.finish(placed.status(), placed.message());
                              }

                              autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placeStatusOf(placed);
                              if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED
                                 || status == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
                                 || ++this.placeTicks > 120) {
                                 this.cancelPlacement();
                                 this.scaffoldPos = null;
                                 this.placeTicks = 0;
                              }

                              return this.running("Placing a support for frame block " + (this.frameCursor + 1));
                           }

                           this.cancelPlacement();
                           this.scaffoldPos = null;
                        }

                        if (mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                           this.cancelPlacement();
                           this.frameCursor++;
                           this.placeTicks = 0;
                           return this.running("Frame " + this.frameCursor + "/" + this.frameCells.size());
                        }

                        AiOperation.Result out = this.stepOutOfCell(cell);
                        if (out != null) {
                           return out;
                        }

                        this.startPlacing(cell, Items.OBSIDIAN);
                        AiOperation.Result placed = this.children.tickChild(this.framePlacement);
                        if (placed.status() == AiOperation.Status.UNCERTAIN) {
                           return this.finish(placed.status(), placed.message());
                        }

                        autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placeStatusOf(placed);
                        if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                           this.cancelPlacement();
                           this.frameCursor++;
                           this.placeTicks = 0;
                           return this.running("Frame " + this.frameCursor + "/" + this.frameCells.size());
                        }

                        if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.placeTicks <= 120) {
                           return this.running("Placing frame block " + (this.frameCursor + 1) + "/" + this.frameCells.size());
                        }

                        if (this.failedCells.add(cell)) {
                           this.cancelPlacement();
                           this.placeTicks = 0;
                           this.frameApproach = AiGoto.approachToPlaceCompat(
                              cell, Items.OBSIDIAN, 20, null, false, AiDimension.frameCells(this.site, this.buildAxis), this.frameAndCornerCells()
                           );
                           return this.running("Moving to reach frame block " + (this.frameCursor + 1));
                        }

                        this.building = false;
                        if (this.site != null) {
                           this.rejectedFrameSites.add(this.site.asLong());
                        }

                        return this.finish(AiOperation.Status.FAILED, "Could not place the portal frame: " + this.placer.failureReason());
                     }
                  } else {
                     this.observeIgnition();
                     if (this.ignitionReceipt.isPending()) {
                        return ++this.lightWaitTicks > 120
                           ? this.finish(AiOperation.Status.TIMED_OUT, "Portal lighting use was not acknowledged by the server")
                           : this.running("Waiting for the portal lighting acknowledgement");
                     }

                     if (!mc.level.getBlockState(this.site).is(Blocks.NETHER_PORTAL)
                        || this.ignitionPos != null
                           && !this.ignitionReceipt.isPortalConfirmed()
                           && AiServerEvidence.blockRevision(this.site) <= this.ignitionReceipt.getRevision()) {
                        if (this.lightWaitTicks % 20 == 0 && this.frameRepairs < 3) {
                           List<BlockPos> missing = this.missingFrameCells();
                           if (!missing.isEmpty()) {
                              this.frameRepairs++;
                              this.failedCells.removeAll(missing);
                              this.scaffoldTried.removeAll(missing);
                              int first = this.frameCells.size();

                              for (BlockPos cell : missing) {
                                 int i = this.frameCells.indexOf(cell);
                                 if (i >= 0) {
                                    first = Math.min(first, i);
                                 }
                              }

                              AutismClientAddon.LOG.info("[AI] dimension: frame cells {} hold no obsidian; placing them again", missing);
                              if (first < this.frameCells.size()) {
                                 this.frameCursor = first;
                                 this.lightWaitTicks = 0;
                                 return this.running("Placing the frame's missing blocks");
                              }
                           }
                        }

                        if (++this.lightWaitTicks > 120) {
                           if (!this.interiorClearTried) {
                              this.interiorClearTried = true;

                              for (BlockPos cell : AiDimension.portalInterior(this.site, this.buildAxis)) {
                                 if (!this.isOpenInterior(cell)) {
                                    this.digQueue.add(cell.immutable());
                                 }
                              }

                              if (!this.digQueue.isEmpty()) {
                                 this.lightWaitTicks = 0;
                                 AutismClientAddon.LOG
                                    .info("[AI] dimension: {} blocks in the portal's interior; digging them out", this.digQueue.size());
                                 return this.running("Clearing the portal's interior");
                              }
                           }

                           this.building = false;
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] dimension: the portal at {} did not light after {} clicks:{}",
                                 this.site.toShortString(),
                                 this.lightingClicks,
                                 this.describeFrame()
                              );
                           return this.finish(AiOperation.Status.FAILED, "The frame is complete but the portal did not light");
                        } else {
                           if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:flint_and_steel")) < 0 && AiItems.equipHotbar(AiItems.resolveItem("minecraft:fire_charge")) < 0) {
                              this.building = false;
                              return this.finish(AiOperation.Status.FAILED, "Lost the lighter before the portal was lit");
                           }

                           BlockPos[] blocks = new BlockPos[]{
                              this.site.below(),
                              this.site.relative(this.buildAxis).below(),
                              this.site.relative(this.buildAxis.getOpposite()),
                              this.site.relative(this.buildAxis, 2)
                           };
                           Direction[] faces = new Direction[]{Direction.UP, Direction.UP, this.buildAxis, this.buildAxis.getOpposite()};
                           int k = Math.floorMod(this.lightingFaceIndex, 4);
                           BlockPos floor = blocks[k];
                           this.aim.aimAt(autismclient.ai.engine.AiAim.aimPointForBlockFace(floor, faces[k]));
                           this.aim.tick();
                           BlockHitResult seen = autismclient.ai.engine.AiAim.raycastLookBlock();
                           boolean onFace = seen != null && seen.getBlockPos().equals(floor) && seen.getDirection() == faces[k];
                           if (!onFace) {
                              if (this.lightingAimTicks % 10 == 9) {
                                 this.lightingFaceIndex++;
                              }

                              if (++this.lightingAimTicks > 40 && this.lightingStanceMoves < 3) {
                                 BlockPos stand = this.findLightingStand();
                                 if (stand == null && this.lightingAimTicks % 100 == 41) {
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: no view of the portal's floor or jambs from {}, and no stance to light from",
                                          mc.player.blockPosition().toShortString()
                                       );
                                 }

                                 if (stand != null) {
                                    this.lightingStanceMoves++;
                                    this.lightingAimTicks = 0;
                                    this.lightWaitTicks = 0;
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] dimension: no view of the portal's floor from {}; lighting from {}",
                                          mc.player.blockPosition().toShortString(),
                                          stand.toShortString()
                                       );
                                    this.frameApproach = AiGoto.travelToCompat(stand, 0.4, false, false, false, 15);
                                    return this.running("Moving in front of the frame to light it");
                                 }
                              }

                              return this.running("Turning to light the portal");
                           } else {
                              BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                              if (hit == null) {
                                 return this.running("Turning to light the portal");
                              }

                              this.ignitionPos = hit.getBlockPos().relative(hit.getDirection()).immutable();
                              this.ignitionBeforeState = mc.level.getBlockState(this.ignitionPos);
                              BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)mc.level).autism$getBlockPredictionHandler();
                              this.ignitionReceipt.beginUse(prediction.currentSequence(), AiServerEvidence.blockRevision(this.ignitionPos));
                              InteractionResult used = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                              this.ignitionReceipt.markSent(prediction.currentSequence());
                              Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                              if (this.lightingClicks++ % 10 == 0) {
                                 AutismClientAddon.LOG
                                    .info(
                                       "[AI] dimension: lighting click {} on {} {} -> {}",
                                       this.lightingClicks,
                                       hit.getBlockPos().toShortString(),
                                       hit.getDirection(),
                                       used
                                    );
                              }

                              if (!this.ignitionReceipt.isPending()) {
                                 this.lightingFaceIndex++;
                              }

                              return this.running("Lighting the portal");
                           }
                        }
                     } else {
                        this.building = false;
                        this.portalLit = true;
                        this.portalPos = this.site;
                        AiDimension.lastPortalSite = this.site.immutable();
                        return this.running("Portal lit");
                     }
                  }
               }

               void observeIgnition() {
                  if (this.ignitionReceipt.isPending() && this.ignitionPos != null && mc.level == this.startLevel) {
                     BlockState state = mc.level.getBlockState(this.ignitionPos);
                     this.ignitionReceipt
                        .observe(
                           state.is(Blocks.NETHER_PORTAL),
                           state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE),
                           state == this.ignitionBeforeState,
                           AiServerEvidence.blockRevision(this.ignitionPos),
                           autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(mc.level, this.ignitionReceipt.getSentSequence())
                        );
                     if (!this.ignitionReceipt.isPending() && !this.ignitionReceipt.isPortalConfirmed()) {
                        this.lightingFaceIndex++;
                     }
                  }
               }

               void startPlacing(BlockPos cell, Item item) {
                  if (this.framePlacement == null || !cell.equals(this.placer.targetPos())) {
                     this.cancelPlacement();
                     long before = AiServerEvidence.blockRevision(cell);
                     this.placer.start(cell, item);
                     this.placingBlock = item instanceof BlockItem block ? block.getBlock() : null;
                     this.framePlacement = new AiDimension.FramePlacementOp(
                        this.placer::tick,
                        this.placer::needsReconciliation,
                        () -> AiJson.objectOf(
                           "cell",
                           cell.toShortString(),
                           "item",
                           BuiltInRegistries.ITEM.getKey(item).toString(),
                           "blockRevisionBefore",
                           before,
                           "blockRevisionNow",
                           AiServerEvidence.blockRevision(cell),
                           "state",
                           this.placer.statusSummary(),
                           "pending",
                           this.placer.needsReconciliation()
                        ),
                        this.placer::failureReason,
                        this.placer::reset
                     );
                     this.placeTicks = 0;
                  }
               }

               autismclient.ai.engine.AiPlacer.PlaceStatus placeStatusOf(AiOperation.Result result) {
                  return result.status() == AiOperation.Status.RUNNING
                     ? autismclient.ai.engine.AiPlacer.PlaceStatus.WORKING
                     : (
                        result.status() == AiOperation.Status.SUCCEEDED
                           ? autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED
                           : autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
                     );
               }

               void cancelPlacement() {
                  if (this.framePlacement != null) {
                     this.lastPlacementReceipt = this.framePlacement.receiptJson();
                  }

                  this.children.cancelChild(this.framePlacement);
                  this.framePlacement = null;
                  this.placingBlock = null;
               }

               int obsidianInFrame(BlockPos origin, Direction axis) {
                  int cnt = 0;

                  for (BlockPos cell : AiDimension.frameBlocks(origin, axis)) {
                     if (mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                        cnt++;
                     }
                  }

                  return cnt;
               }

               BlockPos findReusableFrame() {
                  BlockPos reuse = null;
                  Direction reuseAxis = null;
                  int most = 0;
                  BlockPos at = mc.player.blockPosition();

                  for (int radius = 0; radius <= 8; radius++) {
                     for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                           if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                              for (int dy = -3; dy <= 3; dy++) {
                                 for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                                    BlockPos candidate = at.offset(dx, dy, dz);
                                    if (!this.rejectedFrameSites.contains(candidate.asLong())) {
                                       int n = this.obsidianInFrame(candidate, axis);
                                       if (n > most && this.canBuildFrame(candidate, axis)) {
                                          most = n;
                                          reuse = candidate;
                                          reuseAxis = axis;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (reuse != null) {
                     this.buildAxis = reuseAxis;
                     return reuse;
                  } else {
                     return this.searchFrameOrigin();
                  }
               }

               boolean canBuildFrame(BlockPos origin, Direction axis) {
                  for (BlockPos cell : AiDimension.frameBlocks(origin, axis)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && !this.isPlaceable(cell)) {
                        return false;
                     }
                  }

                  Set<BlockPos> site = new HashSet<>(AiDimension.frameBlocks(origin, axis));
                  site.addAll(AiDimension.portalInterior(origin, axis));

                  for (BlockPos cell : AiDimension.frameBlocks(origin, axis)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && !this.hasSolidNeighbour(cell)) {
                        boolean supportable = false;

                        for (Direction d : Direction.values()) {
                           BlockPos side = cell.relative(d);
                           if (!site.contains(side) && this.isPlaceable(side) && this.hasSolidNeighbour(side)) {
                              supportable = true;
                              break;
                           }
                        }

                        if (!supportable && !site.contains(cell.below())) {
                           return false;
                        }
                     }
                  }

                  for (BlockPos cell : AiDimension.portalInterior(origin, axis)) {
                     if (!this.isPlaceable(cell) && !this.isClearableForFrame(cell)) {
                        return false;
                     }
                  }

                  return true;
               }

               BlockPos searchFrameOrigin() {
                  BlockPos feet = mc.player.blockPosition();

                  for (int radius = 2; radius <= 8; radius++) {
                     for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                           if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                              for (int dy : AiDimension.FRAME_DY_ORDER) {
                                 for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                                    BlockPos candidate = feet.offset(dx, dy, dz);
                                    if (!this.rejectedFrameSites.contains(candidate.asLong()) && this.frameFitsNow(candidate, axis)) {
                                       this.buildAxis = axis;
                                       return candidate;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  return null;
               }

               boolean frameFitsNow(BlockPos origin, Direction axis) {
                  return this.frameFits(origin, axis, false);
               }

               boolean frameFits(BlockPos origin, Direction axis, boolean ignorePlayer) {
                  for (BlockPos cell : AiDimension.frameBlocks(origin, axis)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN) && (ignorePlayer ? !this.isPlaceable(cell) : !this.canPlaceNow(cell))) {
                        return false;
                     }
                  }

                  for (BlockPos cell : AiDimension.portalInterior(origin, axis)) {
                     if (ignorePlayer ? !this.isPlaceable(cell) : !this.canPlaceNow(cell)) {
                        return false;
                     }
                  }

                  for (BlockPos support : AiDimension.frameFloorSupports(origin, axis)) {
                     if (!AiTerrain.isLoaded(mc.level, support)) {
                        return false;
                     }

                     if (mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                        return false;
                     }
                  }

                  return true;
               }

               boolean isPlaceable(BlockPos cell) {
                  if (!AiTerrain.isLoaded(mc.level, cell)) {
                     return false;
                  }

                  BlockState state = mc.level.getBlockState(cell);
                  return state.canBeReplaced() && state.getFluidState().isEmpty();
               }

               boolean isOpenInterior(BlockPos cell) {
                  BlockState state = mc.level.getBlockState(cell);
                  return state.isAir() || state.is(BlockTags.FIRE) || state.is(Blocks.NETHER_PORTAL);
               }

               boolean isClearableForFrame(BlockPos cell) {
                  BlockState state = mc.level.getBlockState(cell);
                  if (state.getFluidState().isEmpty()
                     && !state.hasBlockEntity()
                     && !state.is(Blocks.OBSIDIAN)
                     && !state.is(Blocks.BEDROCK)
                     && !(state.getBlock() instanceof FallingBlock)) {
                     float hardness = state.getDestroySpeed(mc.level, cell);
                     if (!(hardness < 0.0F) && !(hardness > 5.0F)) {
                        if (state.requiresCorrectToolForDrops()) {
                           boolean tool = false;

                           for (int slot = 0; slot < 36 && !tool; slot++) {
                              tool = mc.player.getInventory().getItem(slot).isCorrectToolForDrops(state);
                           }

                           if (!tool) {
                              return false;
                           }
                        }

                        for (Direction d : Direction.values()) {
                           FluidState fluid = mc.level.getFluidState(cell.relative(d));
                           if (fluid.is(FluidTags.LAVA)) {
                              return false;
                           }

                           if (fluid.is(FluidTags.WATER) && d != Direction.DOWN) {
                              return false;
                           }
                        }

                        return true;
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               }

               List<BlockPos> findDiggableSite() {
                  BlockPos feet = mc.player.blockPosition();
                  List<BlockPos> best = null;
                  double bestScore = 1.7976931348623157E308;

                  for (int radius = 2; radius <= 6; radius++) {
                     for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                           if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                              for (int dy : AiDimension.FRAME_DY_ORDER) {
                                 for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                                    BlockPos origin = feet.offset(dx, dy, dz);
                                    List<BlockPos> digs = new ArrayList<>();
                                    boolean ok = true;
                                    List<BlockPos> cells = new ArrayList<>(AiDimension.frameBlocks(origin, axis));
                                    cells.addAll(AiDimension.portalInterior(origin, axis));

                                    for (BlockPos cell : cells) {
                                       if (!AiTerrain.isLoaded(mc.level, cell)) {
                                          ok = false;
                                          break;
                                       }

                                       if (!this.isPlaceable(cell)) {
                                          if (!this.isClearableForFrame(cell)) {
                                             ok = false;
                                             break;
                                          }

                                          digs.add(cell.immutable());
                                       }
                                    }

                                    if (ok && digs.size() <= 20) {
                                       for (BlockPos support : AiDimension.frameFloorSupports(origin, axis)) {
                                          if (mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                                             ok = false;
                                             break;
                                          }
                                       }

                                       if (ok) {
                                          double score = digs.size() * 10.0 + origin.distSqr(feet);
                                          if (score < bestScore) {
                                             bestScore = score;
                                             best = digs;
                                             this.origin = origin;
                                             this.axis = axis;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (best != null) {
                     best.sort(Comparator.comparingInt(c -> -c.getY()));
                  }

                  return best;
               }

               AiOperation.Result tickSiteDig() {
                  if (this.siteDigTravel != null) {
                     AiOperation.Result r = this.children.tickChild(this.siteDigTravel);
                     if (!r.terminal()) {
                        return this.running(
                           this.digQueue.isEmpty()
                              ? "Stepping out of the dug portal site"
                              : "Clearing a site for the portal (" + (this.digQueue.size() + 1) + " left)"
                        );
                     }

                     this.children.cancelChild(this.siteDigTravel);
                     this.siteDigTravel = null;
                     if (r.status() == AiOperation.Status.UNCERTAIN || r.status() == AiOperation.Status.CANCELLED) {
                        return this.finish(r.status(), "Portal excavation interrupted: " + r.message());
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && !this.digQueue.isEmpty() && ++this.siteClearFailures > 3) {
                        this.digQueue.clear();
                        return this.finish(AiOperation.Status.FAILED, "Could not clear a portal site: " + r.message());
                     }
                  }

                  while (!this.digQueue.isEmpty() && this.isOpenInterior(this.digQueue.peek())) {
                     this.digQueue.poll();
                  }

                  if (this.digQueue.isEmpty()) {
                     return null;
                  }

                  BlockPos cell = this.digQueue.poll();
                  this.siteDigTravel = autismclient.ai.engine.AiBreakBlock.breakBlock(cell, 20, false);
                  return this.running("Clearing a site for the portal at " + cell.toShortString());
               }

               BlockPos findFrameStand(BlockPos origin, Direction axis) {
                  Set<BlockPos> site = new HashSet<>(AiDimension.frameBlocks(origin, axis));
                  site.addAll(AiDimension.portalInterior(origin, axis));
                  site.addAll(AiDimension.frameCorners(origin, axis));
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos cell : site) {
                     for (int dx = -3; dx <= 3; dx++) {
                        for (int dy = -2; dy <= 2; dy++) {
                           for (int dz = -3; dz <= 3; dz++) {
                              BlockPos stand = cell.offset(dx, dy, dz);
                              if (!site.contains(stand) && !site.contains(stand.above()) && this.isPlaceable(stand) && this.isPlaceable(stand.above())) {
                                 BlockPos floor = stand.below();
                                 if (!mc.level.getBlockState(floor).getCollisionShape(mc.level, floor).isEmpty() && !site.contains(floor)) {
                                    double distance = stand.distSqr(mc.player.blockPosition());
                                    if (distance < bestDistance) {
                                       bestDistance = distance;
                                       best = stand.immutable();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  return best;
               }

               BlockPos findScaffoldFor(BlockPos cell) {
                  Set<BlockPos> site = new HashSet<>(AiDimension.frameBlocks(this.site, this.buildAxis));
                  site.addAll(AiDimension.portalInterior(this.site, this.buildAxis));
                  BlockPos below = cell.below();
                  if (!site.contains(below) && this.isPlaceable(below) && this.hasSolidNeighbour(below)) {
                     return below;
                  }

                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (Direction d : Direction.values()) {
                     BlockPos side = cell.relative(d);
                     if (d != Direction.DOWN && !site.contains(side) && this.canPlaceNow(side) && this.hasSolidNeighbour(side)) {
                        double distance = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(side));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = side;
                        }
                     }
                  }

                  return best;
               }

               boolean hasSolidNeighbour(BlockPos cell) {
                  for (Direction d : Direction.values()) {
                     BlockPos side = cell.relative(d);
                     if (!mc.level.getBlockState(side).getCollisionShape(mc.level, side).isEmpty()) {
                        return true;
                     }
                  }

                  return false;
               }

               AiOperation.Result stepOutOfCell(BlockPos cell) {
                  if (this.bodyOverlapRetries < 6 && mc.player.getBoundingBox().intersects(new AABB(cell))) {
                     BlockPos stand = this.findFrameStand(this.site, this.buildAxis);
                     if (stand == null) {
                        return null;
                     }

                     this.bodyOverlapRetries++;
                     this.cancelPlacement();
                     this.placeTicks = 0;
                     this.frameApproach = AiGoto.travelToCompat(stand, 0.5, false, true, false, 20);
                     return this.running("Stepping out of frame cell " + cell.toShortString());
                  } else {
                     return null;
                  }
               }

               Set<Long> frameAndCornerCells() {
                  Set<Long> cells = AiDimension.frameCells(this.site, this.buildAxis);

                  for (BlockPos corner : AiDimension.frameCorners(this.site, this.buildAxis)) {
                     cells.add(corner.asLong());
                  }

                  return cells;
               }

               BlockPos findLightingStand() {
                  Direction normal = this.buildAxis.getClockWise();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  BlockPos at = mc.player.blockPosition();

                  for (int side : new int[]{1, -1}) {
                     for (int step = 0; step < 2; step++) {
                        for (int out = 1; out <= 2; out++) {
                           BlockPos stand = this.site.relative(this.buildAxis, step).relative(normal, side * out);
                           if (!stand.equals(at) || this.lightingStanceMoves <= 0) {
                              BlockPos below = stand.below();
                              if (this.isPlaceable(stand)
                                 && this.isPlaceable(stand.above())
                                 && !mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                                 double distance = stand.distSqr(at) + (out - 1) * 2;
                                 if (distance < bestDistance) {
                                    bestDistance = distance;
                                    best = stand;
                                 }
                              }
                           }
                        }
                     }
                  }

                  return best;
               }

               List<BlockPos> missingFrameCells() {
                  List<BlockPos> missing = new ArrayList<>();

                  for (BlockPos cell : AiDimension.frameBlocks(this.site, this.buildAxis)) {
                     if (!mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                        missing.add(cell);
                     }
                  }

                  return missing;
               }

               String describeFrame() {
                  StringBuilder sb = new StringBuilder();

                  for (BlockPos cell : AiDimension.frameBlocks(this.site, this.buildAxis)) {
                     BlockState state = mc.level.getBlockState(cell);
                     if (!state.is(Blocks.OBSIDIAN)) {
                        sb.append(" frame ").append(cell.toShortString()).append('=').append(state.getBlock().getDescriptionId());
                     }
                  }

                  for (BlockPos cell : AiDimension.portalInterior(this.site, this.buildAxis)) {
                     BlockState state = mc.level.getBlockState(cell);
                     if (!state.isAir()) {
                        sb.append(" interior ").append(cell.toShortString()).append('=').append(state.getBlock().getDescriptionId());
                     }
                  }

                  return sb.length() == 0 ? " frame and interior as planned" : sb.toString();
               }

               boolean canPlaceNow(BlockPos cell) {
                  if (!AiTerrain.isLoaded(mc.level, cell)) {
                     return false;
                  }

                  BlockState state = mc.level.getBlockState(cell);
                  return state.canBeReplaced() && state.getFluidState().isEmpty() ? !mc.player.getBoundingBox().intersects(new AABB(cell)) : false;
               }

               BlockPos findNearestBlock(Block wanted) {
                  BlockPos origin = mc.player.blockPosition();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  int vertical = Math.min(48, 32);

                  for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-48, -vertical, -48), origin.offset(48, vertical, 48))) {
                     if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wanted) {
                        double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = pos.immutable();
                        }
                     }
                  }

                  return best;
               }

               int secondsLeft() {
                  return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
               }

               JsonObject statusJson() {
                  JsonObject out = AiJson.objectOf(
                     "action",
                     action.name().toLowerCase(Locale.ROOT),
                     "target",
                     target,
                     "dimension",
                     AiDimension.currentDimension(),
                     "obsidian",
                     AiGameState.countInventoryItem("minecraft:obsidian"),
                     "framePlaced",
                     this.frameCursor,
                     "frameBlocks",
                     this.frameCells.size(),
                     "lit",
                     this.portalLit,
                     "observedLinks",
                     AiPortalLinks.snapshotJson()
                  );
                  out.add("arrivalSearchPrediction", AiPortalLinks.predictArrival(this.startPos, this.startDimension, target));
                  if (this.portalPos != null) {
                     out.addProperty("portalAt", this.portalPos.toShortString());
                  }

                  this.children.addChildEvidence(out);
                  if (this.framePlacement != null) {
                     out.add("placementReceipt", this.framePlacement.receiptJson());
                  } else if (this.lastPlacementReceipt != null) {
                     out.add("placementReceipt", this.lastPlacementReceipt);
                  }

                  if (this.ignitionPos != null) {
                     JsonObject receipt = this.ignitionReceipt.toReceipt();
                     receipt.addProperty("cell", this.ignitionPos.toShortString());
                     out.add("lightingReceipt", receipt);
                  }

                  if (action == AiDimension.DimensionAction.FIND) {
                     out.add("portalsNearby", this.nearbyPortals());
                  }

                  return out;
               }

               JsonArray nearbyPortals() {
                  JsonArray found = new JsonArray();

                  for (Block candidate : List.of(Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY)) {
                     BlockPos nearest = this.findNearestBlock(candidate);
                     if (nearest != null) {
                        found.add(
                           AiJson.objectOf(
                              "block",
                              BuiltInRegistries.BLOCK.getKey(candidate).toString(),
                              "x",
                              nearest.getX(),
                              "y",
                              nearest.getY(),
                              "z",
                              nearest.getZ(),
                              "distance",
                              Math.round(Math.sqrt(mc.player.distanceToSqr(Vec3.atCenterOf(nearest))) * 10.0) / 10.0
                           )
                        );
                     }
                  }

                  return found;
               }

               AiOperation.Result running(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.statusJson().toString(), List.of());
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  return this.finishWith(status, message, null);
               }

               AiOperation.Result finishWith(AiOperation.Status status, String message, JsonObject extra) {
                  this.noteUnresolvedChildren();
                  JsonObject data = this.statusJson();
                  if (extra != null) {
                     for (Entry<String, JsonElement> entry : extra.entrySet()) {
                        data.add(entry.getKey(), entry.getValue());
                     }
                  }

                  AiOperation.Result result = this.children
                     .resultAtBoundary(
                        new AiOperation.Result(status, message, data.toString(), List.of()), this.ignitionReceipt.isPending()
                     );
                  this.cancel();
                  return result;
               }

               void noteUnresolvedChildren() {
                  this.observeIgnition();
                  this.children.noteUnresolvedAll(this.portalTravel, this.siteDigTravel, this.frameApproach, this.framePlacement);
               }

               @Override
               public boolean canInterruptNow() {
                  return !this.requiresReconciliationAfterEmergency()
                     && (this.portalTravel == null || this.portalTravel.canInterruptNow())
                     && (this.siteDigTravel == null || this.siteDigTravel.canInterruptNow())
                     && (this.frameApproach == null || this.frameApproach.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.children.hasUnresolved()
                     || this.ignitionReceipt.isPending()
                     || this.framePlacement != null && this.framePlacement.requiresReconciliationAfterEmergency()
                     || this.portalTravel != null && this.portalTravel.requiresReconciliationAfterEmergency()
                     || this.siteDigTravel != null && this.siteDigTravel.requiresReconciliationAfterEmergency()
                     || this.frameApproach != null && this.frameApproach.requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  if (!this.cancelled) {
                     this.noteUnresolvedChildren();
                     this.cancelled = true;
                     this.aim.clear();
                     this.portalAim.clear();
                     if (this.walkInTicks > 0 && mc.options != null) {
                        mc.options.keyUp.setDown(false);
                        mc.options.keyJump.setDown(false);
                     }

                     this.cancelPlacement();
                     if (this.siteDigTravel != null) {
                        this.children.cancelChild(this.siteDigTravel);
                        this.siteDigTravel = null;
                     }

                     if (this.frameApproach != null) {
                        this.children.cancelChild(this.frameApproach);
                        this.frameApproach = null;
                     }

                     if (this.portalTravel != null) {
                        this.children.cancelChild(this.portalTravel);
                        this.portalTravel = null;
                     }
                  }
               }
            };
         }
      } else {
         throw new IllegalArgumentException("Dimension travel requires a connected living player");
      }
   }

   static List<BlockPos> frameBlocks(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(10);

      for (int step = 0; step < 2; step++) {
         cells.add(origin.relative(axis, step).below());
         cells.add(origin.relative(axis, step).above(3));
      }

      for (int height = 0; height < 3; height++) {
         cells.add(origin.relative(axis, -1).above(height));
         cells.add(origin.relative(axis, 2).above(height));
      }

      return cells;
   }

   static List<BlockPos> frameCorners(BlockPos origin, Direction axis) {
      return List.of(origin.relative(axis, -1).below(), origin.relative(axis, 2).below(), origin.relative(axis, -1).above(3), origin.relative(axis, 2).above(3));
   }

   static List<BlockPos> frameFloorSupports(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(2);

      for (int step = 0; step < 2; step++) {
         cells.add(origin.relative(axis, step).below(2));
      }

      return cells;
   }

   static List<BlockPos> portalInterior(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(6);

      for (int step = 0; step < 2; step++) {
         for (int height = 0; height < 3; height++) {
            cells.add(origin.relative(axis, step).above(height));
         }
      }

      return cells;
   }

   static String normalizeDimension(String spec) {
      String clean = spec.trim().toLowerCase(Locale.ROOT);
      if (clean.equals("nether")) {
         clean = "the_nether";
      }

      if (clean.equals("end")) {
         clean = "the_end";
      }

      return clean.contains(":") ? clean : "minecraft:" + clean;
   }

   private static String currentDimension() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level == null ? "" : mc.level.dimension().identifier().toString();
   }

   private static AiDimension.DimensionAction parseAction(String text) {
      try {
         return AiDimension.DimensionAction.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be enter or find");
      }
   }

   static {
      PORTAL_BLOCKS.put("minecraft:the_nether", Blocks.NETHER_PORTAL);
      PORTAL_BLOCKS.put("minecraft:overworld", Blocks.NETHER_PORTAL);
      PORTAL_BLOCKS.put("minecraft:the_end", Blocks.END_PORTAL);
   }

   static final class FramePlacementOp implements AiOperation {
      private final Supplier<autismclient.ai.engine.AiPlacer.PlaceStatus> work;
      private final BooleanSupplier pending;
      private final Supplier<JsonObject> receipt;
      private final Supplier<String> failureReason;
      private final Runnable release;
      private boolean active = true;
      private boolean started;
      private int ackWaitTicks;

      FramePlacementOp(
         Supplier<autismclient.ai.engine.AiPlacer.PlaceStatus> work,
         BooleanSupplier pending,
         Supplier<JsonObject> receipt,
         Supplier<String> failure,
         Runnable release
      ) {
         this.work = work;
         this.pending = pending;
         this.receipt = receipt;
         this.failureReason = failure;
         this.release = release;
      }

      @Override
      public AiOperation.Result tick() {
         if (!this.active) {
            return new AiOperation.Result(AiOperation.Status.CANCELLED, "Portal frame placement cancelled", "{}", List.of());
         }

         if (this.started && this.pending.getAsBoolean()) {
            return new AiOperation.Result(
               ++this.ackWaitTicks > 120 ? AiOperation.Status.TIMED_OUT : AiOperation.Status.RUNNING,
               "Waiting for the portal frame placement acknowledgement",
               AiJson.objectOf("placementReceipt", this.receipt.get()).toString(),
               List.of()
            );
         }

         this.started = true;
         autismclient.ai.engine.AiPlacer.PlaceStatus status = this.work.get();
         return new AiOperation.Result(
            status == autismclient.ai.engine.AiPlacer.PlaceStatus.WORKING
               ? AiOperation.Status.RUNNING
               : (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED),
            status == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED ? this.failureReason.get() : "Portal frame placement",
            AiJson.objectOf("placementReceipt", this.receipt.get()).toString(),
            List.of()
         );
      }

      @Override
      public boolean canInterruptNow() {
         return !this.requiresReconciliationAfterEmergency();
      }

      @Override
      public boolean requiresReconciliationAfterEmergency() {
         return this.active && this.pending.getAsBoolean();
      }

      @Override
      public void cancel() {
         if (this.active) {
            this.active = false;
            this.release.run();
         }
      }

      JsonObject receiptJson() {
         return this.receipt.get();
      }
   }

   static final class IgnitionReceipt {
      private int sequence;
      private int sentSequence = -1;
      private long revision;
      private boolean pending;
      private String outcome = "unsent";

      void beginUse(int sequence, long revision) {
         if (this.pending) {
            throw new IllegalStateException("The previous portal lighting use is still unacknowledged");
         }

         this.sequence = sequence;
         this.revision = revision;
      }

      void markSent(int sequence) {
         this.sentSequence = sequence;
         this.pending = sequence > this.sequence;
         this.outcome = this.pending ? "awaiting-server" : "unsent";
      }

      void observe(boolean portal, boolean fire, boolean unchanged, long revision, boolean acknowledged) {
         if (this.pending) {
            if (portal && revision > this.revision) {
               this.pending = false;
               this.outcome = "portal-confirmed";
            } else if (acknowledged && revision > this.revision && unchanged && !fire && !portal) {
               this.pending = false;
               this.outcome = "server-refused";
            }
         }
      }

      boolean isPending() {
         return this.pending;
      }

      boolean isPortalConfirmed() {
         return this.outcome.equals("portal-confirmed");
      }

      long getRevision() {
         return this.revision;
      }

      int getSentSequence() {
         return this.sentSequence;
      }

      JsonObject toReceipt() {
         return AiJson.objectOf(
            "beforeSequence", this.sequence, "sentSequence", this.sentSequence, "blockRevisionBefore", this.revision, "pending", this.pending, "outcome", this.outcome
         );
      }
   }

   enum DimensionAction {
      ENTER,
      FIND;
   }

   static final class ChildTracker {
      private final IdentityHashMap<AiOperation, AiOperation.Result> lastResults = new IdentityHashMap<>();
      private AiOperation child;
      private AiOperation.Result actual;
      private AiOperation.Result unresolvedResult;
      private boolean unresolved;

      AiOperation.Result tickChild(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         AiOperation.Result actual = child.tick();
         if (actual.terminal()) {
            boolean unresolved = child.requiresReconciliationAfterEmergency()
               || actual.status() != AiOperation.Status.SUCCEEDED && before
               || needsReconciliation(actual);
            actual = AiGuardLayer.wrapPendingTermination(unresolved, actual);
            this.noteUnresolved(child, unresolved, actual);
         }

         this.lastResults.put(child, actual);
         this.actual = actual;
         return actual;
      }

      private static boolean needsReconciliation(AiOperation.Result result) {
         if (result == null) {
            return false;
         }

         JsonObject data = AiRunResources.parseEvidenceJson(result.dataJson());
         return AiJson.getBoolean(data, "interruptedActionNeedsReconciliation", false) || AiJson.getBoolean(data, "reobserveBeforeResuming", false);
      }

      private void noteUnresolved(AiOperation child, boolean unresolved, AiOperation.Result actual) {
         if (unresolved || needsReconciliation(actual)) {
            this.unresolved = true;
            if (this.child == null) {
               this.child = child;
            }

            if (child == this.child && actual != null) {
               this.unresolvedResult = actual;
            }
         }
      }

      void noteUnresolvedAll(AiOperation... children) {
         for (AiOperation child : children) {
            if (child != null) {
               this.noteUnresolved(child, child.requiresReconciliationAfterEmergency(), this.lastResults.get(child));
            }
         }
      }

      void cancelChild(AiOperation child) {
         if (child != null) {
            this.noteUnresolvedAll(child);
            child.cancel();
            this.lastResults.remove(child);
         }
      }

      boolean hasUnresolved() {
         return this.unresolved;
      }

      void addChildEvidence(JsonObject data) {
         if (this.actual != null) {
            data.add("dimensionChild", AiRunResources.compactEvidenceJson(this.actual.dataJson()));
            data.addProperty("dimensionChildStatus", this.actual.status().name());
         }

         if (this.unresolvedResult != null) {
            data.add("pendingChild", AiRunResources.compactEvidenceJson(this.unresolvedResult.dataJson()));
            data.addProperty("pendingChildStatus", this.unresolvedResult.status().name());
            data.addProperty("pendingChildFailure", this.unresolvedResult.failure().name());
         }
      }

      AiOperation.Result resultAtBoundary(AiOperation.Result boundary, boolean interactionPending) {
         JsonObject data = AiRunResources.parseEvidenceJson(boundary.dataJson());
         this.addChildEvidence(data);
         AiOperation.Result actual = this.actual != null ? this.actual : this.unresolvedResult;
         AiOperation.Result result = AiNetherRun.phaseResult(
            boundary.status(), boundary.message(), data, this.unresolved || interactionPending, actual
         );
         return new AiOperation.Result(
            result.status(),
            result.message(),
            result.dataJson(),
            actual == null ? boundary.evidence() : actual.evidence(),
            actual != null && actual.failure() != AiFailure.UNKNOWN ? actual.failure() : boundary.failure()
         );
      }
   }
}
