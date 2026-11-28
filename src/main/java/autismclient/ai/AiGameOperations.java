package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiGameOperations {
   private static final int init = 40;

   private AiGameOperations() {
   }

   static AiOperation.Result result(AiOperation.Status s, String message, JsonObject data) {
      return new AiOperation.Result(s, message, data.toString(), List.of());
   }

   static AiOperation placeItem(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         String spec = AiJson.getString(args, "item", "").trim();
         Item item = null;
         if (spec.isEmpty()) {
            Item held = mc.player.getMainHandItem().getItem();
            if (held instanceof BlockItem) {
               item = held;
            }
         } else {
            Item fallback = null;

            for (Item candidate : AiItems.resolveItem(spec).items()) {
               if (candidate instanceof BlockItem) {
                  if (fallback == null) {
                     fallback = candidate;
                  }

                  if (AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(candidate).toString()) > 0) {
                     item = candidate;
                     break;
                  }
               }
            }

            if (item == null) {
               item = fallback;
            }
         }

         if (item == null) {
            return immediate(AiOperation.Status.FAILED, "Name a block or station to put down (item)", new JsonObject());
         }

         final String id = BuiltInRegistries.ITEM.getKey(item).toString();
         if (AiGameState.countInventoryItem(id) <= 0) {
            return immediate(
               AiOperation.Status.FAILED, "No " + id + " in the bag; obtain it first", AiJson.objectOf("missing", AiJson.objectOf(id, 1))
            );
         }

         final AiStationPlacer placer = new AiStationPlacer(item, BuiltInRegistries.ITEM.getKey(item).getPath().replace('_', ' '));
         final long deadline = System.nanoTime() + Math.max(5, Math.min(120, AiJson.getInt(args, "timeoutSeconds", 30))) * 1000000000L;
         return new AiOperation() {
            boolean cancelled;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > deadline) {
                     placer.cancel();
                     return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "No spot for " + id + " before the timeout", new JsonObject());
                  } else {
                     AiStationPlacer.PlaceStatus status = placer.tick();
                     if (status == AiStationPlacer.PlaceStatus.PLACED) {
                        BlockPos at = placer.getPlaced();
                        return AiGameOperations.result(
                           AiOperation.Status.SUCCEEDED,
                           "Placed " + id + " at " + at.toShortString(),
                           AiJson.objectOf("item", id, "x", at.getX(), "y", at.getY(), "z", at.getZ())
                        );
                     } else {
                        return status == AiStationPlacer.PlaceStatus.FAILED
                           ? AiGameOperations.result(AiOperation.Status.FAILED, "Could not place " + id + ": " + placer.getFailureReason(), new JsonObject())
                           : AiGameOperations.result(AiOperation.Status.RUNNING, placer.getStatus(), new JsonObject());
                     }
                  }
               } else {
                  return AiGameOperations.result(AiOperation.Status.CANCELLED, "Placement cancelled", new JsonObject());
               }
            }

            @Override
            public boolean canInterruptNow() {
               return placer.canInterrupt();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return placer.needsReconciliation();
            }

            @Override
            public void cancel() {
               this.cancelled = true;
               placer.cancel();
            }
         };
      } else {
         return immediate(AiOperation.Status.FAILED, "No live player", new JsonObject());
      }
   }

   static AiOperation immediate(final AiOperation.Status status, final String message, final JsonObject data) {
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            return AiGameOperations.result(status, message, data);
         }

         @Override
         public void cancel() {
         }
      };
   }

   static AiOperation immediateFailure(AiOperation.Status status, final AiFailure failure, final String message, final JsonObject data) {
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            return AiOperation.Result.failed(failure, message, data.toString());
         }

         @Override
         public void cancel() {
         }
      };
   }

   static BlockPos requireBlockPos(JsonObject args) {
      if (args.has("x") && args.has("y") && args.has("z")) {
         return new BlockPos(AiJson.getInt(args, "x", 0), AiJson.getInt(args, "y", 0), AiJson.getInt(args, "z", 0));
      } else {
         throw new IllegalArgumentException("Explicit x,y,z coordinates required");
      }
   }

   static void aimAndStep(autismclient.ai.engine.AiAim aim, Vec3 target) {
      if (Minecraft.getInstance().player != null) {
         aim.aimAt(target);
         aim.tick();
      }
   }

   static void snapLookAt(Vec3 target) {
      LocalPlayer p = Minecraft.getInstance().player;
      if (p != null) {
         Vec3 d = target.subtract(p.getEyePosition());
         p.setYRot((float)(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0));
         p.setXRot((float)(-Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z)))));
      }
   }

   private static BlockHitResult clipOutline(Minecraft mc, Vec3 point) {
      BlockHitResult hit = mc.level.clip(new ClipContext(mc.player.getEyePosition(), point, Block.OUTLINE, Fluid.NONE, mc.player));
      return hit != null && hit.getType() != Type.MISS ? hit : null;
   }

   static AiOperation blockAction(String kind, JsonObject args) {
      return blockAction(kind, args, Set.of());
   }

   static AiOperation blockAction(final String kind, final JsonObject args, final Set<Long> protectedCells) {
      final BlockPos target = requireBlockPos(args);
      return kind.equals("break")
         ? autismclient.ai.engine.AiBreakBlock.breakBlock(
            target, AiJson.getInt(args, "timeoutSeconds", 30), AiJson.getBoolean(args, "requireDrops", true), protectedCells
         )
         : new AiActedOperation() {
            AiOperation approachOp;
            AiOperation interactOp;
            boolean interactionStarted;

            @Override
            public boolean hasActed() {
               return this.interactOp instanceof AiActedOperation inner && inner.hasActed();
            }

            @Override
            public AiOperation.Result tick() {
               if (this.interactionStarted) {
                  return AiGameOperations.result(AiOperation.Status.CANCELLED, "Interaction cancelled", new JsonObject());
               }

               Minecraft mc = Minecraft.getInstance();
               if (mc.player == null || mc.level == null) {
                  return AiGameOperations.result(AiOperation.Status.CANCELLED, "World unavailable", new JsonObject());
               }

               if (this.interactOp != null) {
                  return this.interactOp.tick();
               }

               Direction face = args.has("face") ? Direction.valueOf(AiJson.getString(args, "face", "UP").toUpperCase(Locale.ROOT)) : null;
               if (this.approachOp == null && autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, target, face) == null) {
                  this.approachOp = AiGoto.approachBlockToInteractCompat(target, 30, protectedCells, true, face);
               }

               if (this.approachOp != null) {
                  AiOperation.Result r = this.approachOp.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.approachOp.cancel();
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return r;
                  }
               }

               this.interactOp = AiGameOperations.performBlockInteraction(kind, args);
               return this.interactOp.tick();
            }

            @Override
            public void cancel() {
               this.interactionStarted = true;
               if (this.approachOp != null) {
                  this.approachOp.cancel();
               }

               if (this.interactOp != null) {
                  this.interactOp.cancel();
               }
            }
         };
   }

   private static AiOperation performBlockInteraction(final String kind, final JsonObject args) {
      return new AiActedOperation() {
         final Minecraft client = Minecraft.getInstance();
         final BlockPos target = AiGameOperations.requireBlockPos(args);
         final long deadline = System.nanoTime() + 30000000000L;
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         BlockState initialState;
         BlockState placeCellState;
         net.minecraft.world.level.block.Block placedBlock;
         BlockPos placeCell;
         long baselineRevision;
         long revision;
         int initialContainerId;
         int ticksSinceClick;
         int changedTicks;
         int aimTicks;
         boolean cancelled;
         boolean clicked;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && this.client.player != null && this.client.level != null) {
               if (System.nanoTime() > this.deadline) {
                  this.cancel();
                  return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "No verified block interaction before timeout", new JsonObject());
               }

               if (!AiTerrain.isLoaded(this.client.level, this.target)) {
                  return AiGameOperations.result(AiOperation.Status.FAILED, "Target is in unloaded terrain", new JsonObject());
               }

               if (this.client.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.target))
                  > Math.pow(this.client.player.blockInteractionRange() + 0.9, 2.0)) {
                  return AiGameOperations.result(AiOperation.Status.FAILED, "Target outside reach; navigate closer", new JsonObject());
               }

               if (this.initialState == null) {
                  this.initialState = this.client.level.getBlockState(this.target);
                  this.initialContainerId = this.client.player.containerMenu.containerId;
                  this.baselineRevision = AiServerEvidence.blockRevision(this.target);
                  this.revision = AiServerEvidence.serverMessageRevision();
               }

               Direction desiredFace = args.has("face") ? Direction.valueOf(AiJson.getString(args, "face", "UP").toUpperCase(Locale.ROOT)) : null;
               double aimY = args.has("aimY") ? AiJson.getDouble(args, "aimY", 0.5) : -1.0;
               Vec3 aimPoint = aimY >= 0.0
                  ? new Vec3(this.target.getX() + 0.5, this.target.getY() + aimY, this.target.getZ() + 0.5)
                  : (
                     desiredFace != null
                        ? Vec3.atCenterOf(this.target)
                           .add(desiredFace.getStepX() * 0.499, desiredFace.getStepY() * 0.499, desiredFace.getStepZ() * 0.499)
                        : (kind.equals("place") ? Vec3.atCenterOf(this.target).add(0.0, 0.499, 0.0) : Vec3.atCenterOf(this.target))
                  );
               BlockHitResult reachableHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.client, this.target);
               if (kind.equals("interact") && aimY < 0.0 && desiredFace == null && reachableHit != null) {
                  aimPoint = reachableHit.getLocation();
               }

               BlockHitResult hit = kind.equals("interact") && aimY < 0.0 && desiredFace == null ? reachableHit : AiGameOperations.clipOutline(this.client, aimPoint);
               if (!this.clicked && !this.aim.isSettled()) {
                  AiGameOperations.aimAndStep(this.aim, aimPoint);
                  if (++this.aimTicks <= 40) {
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Turning toward " + this.target.toShortString(), new JsonObject());
                  }
               }

               if (!this.clicked
                  && (kind.equals("place") || desiredFace != null)
                  && (hit == null || !hit.getBlockPos().equals(this.target) || desiredFace != null && hit.getDirection() != desiredFace)) {
                  BlockHitResult solved = autismclient.ai.engine.AiBlockInteraction.findFaceHit(
                     this.client, this.target, desiredFace == null ? Direction.UP : desiredFace
                  );
                  if (solved == null) {
                     return AiGameOperations.result(AiOperation.Status.FAILED, "Target is occluded", new JsonObject());
                  }

                  aimPoint = solved.getLocation();
                  hit = solved;
                  AiGameOperations.aimAndStep(this.aim, aimPoint);
               } else if (!this.clicked) {
                  AiGameOperations.aimAndStep(this.aim, aimPoint);
               }

               if (this.clicked || hit != null && hit.getBlockPos().equals(this.target)) {
                  if (!this.clicked
                     && this.client.player.getEyePosition().distanceToSqr(hit.getLocation())
                        > Math.pow(this.client.player.blockInteractionRange(), 2.0)) {
                     return AiGameOperations.result(AiOperation.Status.FAILED, "Target outside interaction reach", new JsonObject());
                  }

                  if (!this.clicked && desiredFace != null && hit.getDirection() != desiredFace) {
                     return AiGameOperations.result(AiOperation.Status.FAILED, "Requested placement face is occluded", new JsonObject());
                  }

                  if (!this.clicked) {
                     int slot = AiJson.getInt(args, "slot", -1);
                     if (slot < -1 || slot > 8) {
                        throw new IllegalArgumentException("Hotbar slot must be 0..8");
                     }

                     if (slot >= 0 && slot <= 8) {
                        this.client.player.getInventory().setSelectedSlot(slot);
                     }

                     if (kind.equals("place")) {
                        this.placedBlock = this.client.player.getMainHandItem().getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : null;
                        if (this.placedBlock == null) {
                           return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, "Placement requires a block item in the selected hand", "{}");
                        }

                        this.placeCell = this.target.relative(hit.getDirection());
                        this.placeCellState = this.client.level.getBlockState(this.placeCell);
                        this.baselineRevision = AiServerEvidence.blockRevision(this.placeCell);
                     }

                     this.client.gameMode.useItemOn(this.client.player, InteractionHand.MAIN_HAND, hit);
                     this.clicked = true;
                  }

                  this.ticksSinceClick++;
                  boolean changed = kind.equals("place")
                     ? this.placeCell != null
                        && !this.client.level.getBlockState(this.placeCell).equals(this.placeCellState)
                        && (this.placedBlock == null || this.client.level.getBlockState(this.placeCell).is(this.placedBlock))
                     : !this.client.level.getBlockState(this.target).equals(this.initialState);
                  boolean serverApplied = AiServerEvidence.blockRevision(kind.equals("place") && this.placeCell != null ? this.placeCell : this.target)
                     > this.baselineRevision;
                  boolean menuChanged = !kind.equals("place") && this.client.player.containerMenu.containerId != this.initialContainerId;
                  if (!changed && !menuChanged) {
                     this.changedTicks = 0;
                  } else {
                     this.changedTicks++;
                  }

                  if (this.changedTicks >= 5 && (changed && serverApplied || menuChanged)) {
                     return AiGameOperations.result(
                        AiOperation.Status.SUCCEEDED,
                        "Observed block/menu state changed",
                        AiJson.objectOf(
                           "block",
                           this.client.level.getBlockState(this.target).toString(),
                           "containerId",
                           this.client.player.containerMenu.containerId
                        )
                     );
                  } else if (!kind.equals("place") && this.client.player.isSleeping()) {
                     return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Now sleeping", AiJson.objectOf("sleeping", true));
                  } else {
                     String reply = !kind.equals("place") && this.ticksSinceClick >= 5 && this.changedTicks == 0 ? AiGameOperations.latestServerReplyAfter(this.revision) : null;
                     if (reply != null) {
                        return AiGameOperations.result(
                           AiOperation.Status.SUCCEEDED,
                           "The server answered the click: " + reply,
                           AiJson.objectOf("serverMessage", reply, "block", this.client.level.getBlockState(this.target).toString())
                        );
                     } else {
                        return this.ticksSinceClick > 60
                           ? AiGameOperations.result(
                              AiOperation.Status.UNCERTAIN, "Click sent; no block change, menu or server reply followed", new JsonObject()
                           )
                           : AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting for observed interaction outcome", new JsonObject());
                     }
                  }
               } else {
                  return AiGameOperations.result(AiOperation.Status.FAILED, "Target is occluded", new JsonObject());
               }
            } else {
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Interaction cancelled", new JsonObject());
            }
         }

         @Override
         public void cancel() {
            this.cancelled = true;
         }

         @Override
         public boolean hasActed() {
            return this.clicked;
         }
      };
   }

   private static String latestServerReplyAfter(long revision) {
      String reply = null;

      for (Map<String, Object> m : AiServerEvidence.recentServerMessages(8)) {
         if (((Number)m.get("revision")).longValue() > revision) {
            reply = String.valueOf(m.get("text"));
         }
      }

      return reply != null && !reply.isBlank() ? reply : null;
   }

   private static ContainerInput parseClickAction(String name) {
      try {
         return ContainerInput.valueOf(name.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException bad) {
         throw new IllegalArgumentException("Unknown click action '" + name + "'; valid actions: " + Arrays.toString(ContainerInput.values()));
      }
   }

   static AiOperation clickContainerSlot(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         int container = AiJson.getInt(args, "containerId", -1);
         int slot = AiJson.getInt(args, "slot", -1);
         if (container != mc.player.containerMenu.containerId) {
            throw new IllegalArgumentException("Stale container; observe again");
         }

         if (slot >= 0 && slot < mc.player.containerMenu.slots.size()) {
            ContainerInput type = parseClickAction(AiJson.getString(args, "click", "PICKUP"));
            int button = AiJson.getInt(args, "button", 0);
            if (button >= 0 && button <= 8) {
               AiOperation expected = args.has("expected") ? new AiExpectations(args) : null;
               AiContainerTransactions.sendPredictedClick(container, slot, button, type);
               return expected != null
                  ? expected
                  : immediate(
                     AiOperation.Status.UNCERTAIN,
                     "Inventory click sent; reobserve slots before the next step",
                     AiJson.objectOf("containerId", container, "slot", slot)
                  );
            } else {
               throw new IllegalArgumentException("Invalid click button");
            }
         } else {
            throw new IllegalArgumentException("Invalid container slot");
         }
      } else {
         throw new IllegalArgumentException("Inventory clicks require a connected living player");
      }
   }

   static AiOperation farmWheat(JsonObject args) {
      return farmCrop(args, "minecraft:wheat", "minecraft:wheat");
   }

   static AiOperation farmCrop(JsonObject args, String cropBlock, String cropItem) {
      return AiFarm.farmCrop(args, cropBlock, cropItem);
   }

   static AiOperation explore(final JsonObject args, final boolean village) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         return new AiOperation() {
            final Minecraft client = Minecraft.getInstance();
            final BlockPos origin = this.client.player.blockPosition();
            final int maxDistance = Math.max(64, Math.min(16000, AiJson.getInt(args, "maxDistance", 2048)));
            final long deadline = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 1800))) * 1000000000L;
            final Set<String> visitedRegions = new HashSet<>();
            final Set<String> failedRegions = new HashSet<>();
            String arrivedRegion = "";
            String regionCell = "";
            BlockPos currentGoal;
            AiOperation travelOp;
            BlockPos surveyCenter = this.origin;
            BlockPos villageCandidate;
            int scanIndex;
            int beds;
            int bells;
            int paths;
            int farmland;
            int doors;
            int buildingBlocks;
            int spiralStep;
            int failedTravels;
            boolean cancelled;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && this.client.player != null && this.client.level != null) {
                  if (this.client.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.finish(
                        AiOperation.Status.FAILED,
                        AiGuard.formatLowHealthAbort("exploration", this.client.player.getHealth(), this.client.player.getMaxHealth())
                     );
                  }

                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Exploration deadline reached");
                  }

                  if (this.travelOp != null) {
                     AiOperation.Result r = this.travelOp.tick();
                     if (!r.terminal()) {
                        return r;
                     }

                     this.travelOp = null;
                     if (this.villageCandidate != null) {
                        return this.finish(
                           r.status(),
                           r.status() == AiOperation.Status.SUCCEEDED ? "Arrived at village evidence cluster" : "Village evidence found, but arrival failed"
                        );
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && ++this.failedTravels >= 4) {
                        return this.finish(AiOperation.Status.FAILED, "Four exploration frontiers were unreachable");
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && this.currentGoal != null) {
                        this.failedRegions.add(AiRegions.cellKey(this.currentGoal.getX(), this.currentGoal.getZ()));
                     }

                     this.arrivedRegion = r.status() == AiOperation.Status.SUCCEEDED ? this.regionCell : "";
                     this.surveyCenter = this.client.player.blockPosition();
                     this.scanIndex = 0;
                     this.beds = this.bells = this.paths = this.farmland = this.doors = this.buildingBlocks = 0;
                  }

                  int width = 33;
                  int height = 13;
                  int total = width * width * height;

                  for (int budget = 0; budget < 192 && this.scanIndex < total; this.scanIndex++) {
                     int x = this.scanIndex % width - 16;
                     int z = this.scanIndex / width % width - 16;
                     int y = this.scanIndex / (width * width) - 5;
                     BlockPos p = this.surveyCenter.offset(x, y, z);
                     if (AiTerrain.isLoaded(this.client.level, p)) {
                        net.minecraft.world.level.block.Block b = this.client.level.getBlockState(p).getBlock();
                        if (b instanceof BedBlock && this.client.level.getBlockState(p).getValue(BedBlock.PART) == BedPart.HEAD) {
                           this.beds++;
                        }

                        if (b == Blocks.BELL) {
                           this.bells++;
                        }

                        if (b == Blocks.DIRT_PATH) {
                           this.paths++;
                        }

                        if (b == Blocks.FARMLAND) {
                           this.farmland++;
                        }

                        if (b instanceof DoorBlock && this.client.level.getBlockState(p).getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                           this.doors++;
                        }

                        if (this.client.level.getBlockState(p).is(BlockTags.PLANKS) || b instanceof StairBlock || b == Blocks.COBBLESTONE) {
                           this.buildingBlocks++;
                        }
                     }

                     budget++;
                  }

                  if (this.scanIndex < total) {
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Surveying loaded terrain", AiJson.objectOf("visited", this.visitedRegions.size())
                     );
                  }

                  int villagers = 0;

                  for (Entity e : this.client.level.entitiesForRendering()) {
                     if (e instanceof Villager && e.distanceToSqr(this.client.player) < 1024.0) {
                        villagers++;
                     }
                  }

                  String regionCell = AiRegions.cellKey(this.surveyCenter.getX(), this.surveyCenter.getZ());
                  AiService service = AiService.getExistingInstance();
                  if (!regionCell.equals(this.regionCell) && service != null) {
                     service.recordSurveyRegion(this.surveyCenter, this.beds, this.bells, this.paths, this.farmland, villagers, this.arrivedRegion);
                  }

                  this.regionCell = regionCell;
                  if (village
                     && this.beds >= 3
                     && this.paths >= 8
                     && (this.bells > 0 || villagers >= 2)
                     && this.farmland >= 4
                     && this.doors >= 2
                     && this.buildingBlocks >= 16) {
                     this.villageCandidate = this.surveyCenter;
                     this.travelOp = AiGoto.travelToCompat(this.villageCandidate, 3.0, false, false, false, 60);
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING,
                        "Village evidence cluster found; checking arrival",
                        AiJson.objectOf(
                           "beds",
                           this.beds,
                           "bells",
                           this.bells,
                           "paths",
                           this.paths,
                           "farmland",
                           this.farmland,
                           "villagers",
                           villagers,
                           "doors",
                           this.doors,
                           "buildingBlocks",
                           this.buildingBlocks
                        )
                     );
                  }

                  this.visitedRegions.add(regionCell);
                  if (!village && this.visitedRegions.size() >= Math.max(1, AiJson.getInt(args, "regions", 8))) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Requested regions explored");
                  }

                  double angle = this.spiralStep * 2.399963229728653;
                  int distance = 48 + this.spiralStep / 8 * 48;
                  this.spiralStep++;
                  if (distance > this.maxDistance) {
                     return this.finish(AiOperation.Status.FAILED, "Search radius exhausted without verified village evidence");
                  }

                  BlockPos next = new BlockPos(
                     this.origin.getX() + (int)(Math.cos(angle) * distance),
                     this.client.player.blockPosition().getY(),
                     this.origin.getZ() + (int)(Math.sin(angle) * distance)
                  );
                  if (service != null) {
                     HashSet<String> excluded = new HashSet<>(this.visitedRegions);
                     excluded.addAll(this.failedRegions);
                     int[] frontier = AiRegions.frontier(service.getDimensionRegions(), excluded, this.origin.getX(), this.origin.getZ(), this.maxDistance)
                        .stream()
                        .filter(px -> !this.failedRegions.contains(AiRegions.cellKey(px[0], px[2])))
                        .min(Comparator.comparingDouble(px -> this.client.player.position().distanceToSqr(px[0], px[1], px[2])))
                        .orElse(null);
                     if (frontier != null) {
                        next = new BlockPos(frontier[0], frontier[1], frontier[2]);
                     }
                  }

                  this.currentGoal = next;
                  this.travelOp = AiTravelFar.travel(next, 4.0, true, false, false, false, 180);
                  return AiGameOperations.result(
                     AiOperation.Status.RUNNING, "Exploring a new frontier", AiJson.objectOf("target", next.toShortString())
                  );
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Exploration cancelled");
               }
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(
                  status,
                  message,
                  AiJson.objectOf(
                     "visitedRegions",
                     this.visitedRegions.size(),
                     "villageCandidate",
                     this.villageCandidate == null ? null : this.villageCandidate.toShortString(),
                     "beds",
                     this.beds,
                     "bells",
                     this.bells,
                     "paths",
                     this.paths,
                     "farmland",
                     this.farmland,
                     "doors",
                     this.doors,
                     "buildingBlocks",
                     this.buildingBlocks
                  )
               );
            }

            @Override
            public void cancel() {
               this.cancelled = true;
               if (this.travelOp != null) {
                  this.travelOp.cancel();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Exploration requires a connected living player");
      }
   }
}
