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
            boolean init;

            @Override
            public AiOperation.Result tick() {
               if (!this.init && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > deadline) {
                     placer.init_V();
                     return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "No spot for " + id + " before the timeout", new JsonObject());
                  } else {
                     AiStationPlacer.Inner_DataUtil status = placer.init_cn_DataUtil();
                     if (status == AiStationPlacer.Inner_DataUtil.PLACED) {
                        BlockPos at = placer.init_BlockPos();
                        return AiGameOperations.result(
                           AiOperation.Status.SUCCEEDED,
                           "Placed " + id + " at " + at.toShortString(),
                           AiJson.objectOf("item", id, "x", at.getX(), "y", at.getY(), "z", at.getZ())
                        );
                     } else {
                        return status == AiStationPlacer.Inner_DataUtil.FAILED
                           ? AiGameOperations.result(AiOperation.Status.FAILED, "Could not place " + id + ": " + placer.load_Str(), new JsonObject())
                           : AiGameOperations.result(AiOperation.Status.RUNNING, placer.init_Str(), new JsonObject());
                     }
                  }
               } else {
                  return AiGameOperations.result(AiOperation.Status.CANCELLED, "Placement cancelled", new JsonObject());
               }
            }

            @Override
            public boolean canInterruptNow() {
               return placer.init_Z();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return placer.load_Z();
            }

            @Override
            public void cancel() {
               this.init = true;
               placer.init_V();
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
         ? autismclient.ai.engine.AiBreakBlock.init(
            target, AiJson.getInt(args, "timeoutSeconds", 30), AiJson.getBoolean(args, "requireDrops", true), protectedCells
         )
         : new AiActedOperation() {
            AiOperation init_AiOperation;
            AiOperation load;
            boolean init_Z;

            @Override
            public boolean init() {
               return this.load instanceof AiActedOperation inner && inner.init();
            }

            @Override
            public AiOperation.Result tick() {
               if (this.init_Z) {
                  return AiGameOperations.result(AiOperation.Status.CANCELLED, "Interaction cancelled", new JsonObject());
               }

               Minecraft mc = Minecraft.getInstance();
               if (mc.player == null || mc.level == null) {
                  return AiGameOperations.result(AiOperation.Status.CANCELLED, "World unavailable", new JsonObject());
               }

               if (this.load != null) {
                  return this.load.tick();
               }

               Direction face = args.has("face") ? Direction.valueOf(AiJson.getString(args, "face", "UP").toUpperCase(Locale.ROOT)) : null;
               if (this.init_AiOperation == null && autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, target, face) == null) {
                  this.init_AiOperation = AiGoto.approachBlockToInteractCompat(target, 30, protectedCells, true, face);
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.init_AiOperation.cancel();
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return r;
                  }
               }

               this.load = AiGameOperations.performBlockInteraction(kind, args);
               return this.load.tick();
            }

            @Override
            public void cancel() {
               this.init_Z = true;
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
               }

               if (this.load != null) {
                  this.load.cancel();
               }
            }
         };
   }

   private static AiOperation performBlockInteraction(final String kind, final JsonObject args) {
      return new AiActedOperation() {
         final Minecraft init_Minecraft = Minecraft.getInstance();
         final BlockPos init_BlockPos = AiGameOperations.requireBlockPos(args);
         final long init_J = System.nanoTime() + 30000000000L;
         final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
         BlockState init_BlockState;
         BlockState load_BlockState;
         net.minecraft.world.level.block.Block init_Block;
         BlockPos load_BlockPos;
         long load_J;
         long read_J;
         int init_I;
         int load_I;
         int read_I;
         int write;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
               if (System.nanoTime() > this.init_J) {
                  this.cancel();
                  return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "No verified block interaction before timeout", new JsonObject());
               }

               if (!AiTerrain.isLoaded(this.init_Minecraft.level, this.init_BlockPos)) {
                  return AiGameOperations.result(AiOperation.Status.FAILED, "Target is in unloaded terrain", new JsonObject());
               }

               if (this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos))
                  > Math.pow(this.init_Minecraft.player.blockInteractionRange() + 0.9, 2.0)) {
                  return AiGameOperations.result(AiOperation.Status.FAILED, "Target outside reach; navigate closer", new JsonObject());
               }

               if (this.init_BlockState == null) {
                  this.init_BlockState = this.init_Minecraft.level.getBlockState(this.init_BlockPos);
                  this.init_I = this.init_Minecraft.player.containerMenu.containerId;
                  this.load_J = AiServerEvidence.blockRevision(this.init_BlockPos);
                  this.read_J = AiServerEvidence.serverMessageRevision();
               }

               Direction desiredFace = args.has("face") ? Direction.valueOf(AiJson.getString(args, "face", "UP").toUpperCase(Locale.ROOT)) : null;
               double aimY = args.has("aimY") ? AiJson.getDouble(args, "aimY", 0.5) : -1.0;
               Vec3 aimPoint = aimY >= 0.0
                  ? new Vec3(this.init_BlockPos.getX() + 0.5, this.init_BlockPos.getY() + aimY, this.init_BlockPos.getZ() + 0.5)
                  : (
                     desiredFace != null
                        ? Vec3.atCenterOf(this.init_BlockPos)
                           .add(desiredFace.getStepX() * 0.499, desiredFace.getStepY() * 0.499, desiredFace.getStepZ() * 0.499)
                        : (kind.equals("place") ? Vec3.atCenterOf(this.init_BlockPos).add(0.0, 0.499, 0.0) : Vec3.atCenterOf(this.init_BlockPos))
                  );
               BlockHitResult reachableHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.init_Minecraft, this.init_BlockPos);
               if (kind.equals("interact") && aimY < 0.0 && desiredFace == null && reachableHit != null) {
                  aimPoint = reachableHit.getLocation();
               }

               BlockHitResult hit = kind.equals("interact") && aimY < 0.0 && desiredFace == null ? reachableHit : AiGameOperations.clipOutline(this.init_Minecraft, aimPoint);
               if (!this.load_Z && !this.init_DataUtil.isSettled()) {
                  AiGameOperations.aimAndStep(this.init_DataUtil, aimPoint);
                  if (++this.write <= 40) {
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Turning toward " + this.init_BlockPos.toShortString(), new JsonObject());
                  }
               }

               if (!this.load_Z
                  && (kind.equals("place") || desiredFace != null)
                  && (hit == null || !hit.getBlockPos().equals(this.init_BlockPos) || desiredFace != null && hit.getDirection() != desiredFace)) {
                  BlockHitResult solved = autismclient.ai.engine.AiBlockInteraction.findFaceHit(
                     this.init_Minecraft, this.init_BlockPos, desiredFace == null ? Direction.UP : desiredFace
                  );
                  if (solved == null) {
                     return AiGameOperations.result(AiOperation.Status.FAILED, "Target is occluded", new JsonObject());
                  }

                  aimPoint = solved.getLocation();
                  hit = solved;
                  AiGameOperations.aimAndStep(this.init_DataUtil, aimPoint);
               } else if (!this.load_Z) {
                  AiGameOperations.aimAndStep(this.init_DataUtil, aimPoint);
               }

               if (this.load_Z || hit != null && hit.getBlockPos().equals(this.init_BlockPos)) {
                  if (!this.load_Z
                     && this.init_Minecraft.player.getEyePosition().distanceToSqr(hit.getLocation())
                        > Math.pow(this.init_Minecraft.player.blockInteractionRange(), 2.0)) {
                     return AiGameOperations.result(AiOperation.Status.FAILED, "Target outside interaction reach", new JsonObject());
                  }

                  if (!this.load_Z && desiredFace != null && hit.getDirection() != desiredFace) {
                     return AiGameOperations.result(AiOperation.Status.FAILED, "Requested placement face is occluded", new JsonObject());
                  }

                  if (!this.load_Z) {
                     int slot = AiJson.getInt(args, "slot", -1);
                     if (slot < -1 || slot > 8) {
                        throw new IllegalArgumentException("Hotbar slot must be 0..8");
                     }

                     if (slot >= 0 && slot <= 8) {
                        this.init_Minecraft.player.getInventory().setSelectedSlot(slot);
                     }

                     if (kind.equals("place")) {
                        this.init_Block = this.init_Minecraft.player.getMainHandItem().getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : null;
                        if (this.init_Block == null) {
                           return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, "Placement requires a block item in the selected hand", "{}");
                        }

                        this.load_BlockPos = this.init_BlockPos.relative(hit.getDirection());
                        this.load_BlockState = this.init_Minecraft.level.getBlockState(this.load_BlockPos);
                        this.load_J = AiServerEvidence.blockRevision(this.load_BlockPos);
                     }

                     this.init_Minecraft.gameMode.useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, hit);
                     this.load_Z = true;
                  }

                  this.load_I++;
                  boolean changed = kind.equals("place")
                     ? this.load_BlockPos != null
                        && !this.init_Minecraft.level.getBlockState(this.load_BlockPos).equals(this.load_BlockState)
                        && (this.init_Block == null || this.init_Minecraft.level.getBlockState(this.load_BlockPos).is(this.init_Block))
                     : !this.init_Minecraft.level.getBlockState(this.init_BlockPos).equals(this.init_BlockState);
                  boolean serverApplied = AiServerEvidence.blockRevision(kind.equals("place") && this.load_BlockPos != null ? this.load_BlockPos : this.init_BlockPos)
                     > this.load_J;
                  boolean menuChanged = !kind.equals("place") && this.init_Minecraft.player.containerMenu.containerId != this.init_I;
                  if (!changed && !menuChanged) {
                     this.read_I = 0;
                  } else {
                     this.read_I++;
                  }

                  if (this.read_I >= 5 && (changed && serverApplied || menuChanged)) {
                     return AiGameOperations.result(
                        AiOperation.Status.SUCCEEDED,
                        "Observed block/menu state changed",
                        AiJson.objectOf(
                           "block",
                           this.init_Minecraft.level.getBlockState(this.init_BlockPos).toString(),
                           "containerId",
                           this.init_Minecraft.player.containerMenu.containerId
                        )
                     );
                  } else if (!kind.equals("place") && this.init_Minecraft.player.isSleeping()) {
                     return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Now sleeping", AiJson.objectOf("sleeping", true));
                  } else {
                     String reply = !kind.equals("place") && this.load_I >= 5 && this.read_I == 0 ? AiGameOperations.latestServerReplyAfter(this.read_J) : null;
                     if (reply != null) {
                        return AiGameOperations.result(
                           AiOperation.Status.SUCCEEDED,
                           "The server answered the click: " + reply,
                           AiJson.objectOf("serverMessage", reply, "block", this.init_Minecraft.level.getBlockState(this.init_BlockPos).toString())
                        );
                     } else {
                        return this.load_I > 60
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
            this.init_Z = true;
         }

         @Override
         public boolean init() {
            return this.load_Z;
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
      return AiFarm.init(args, cropBlock, cropItem);
   }

   static AiOperation explore(final JsonObject args, final boolean village) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final BlockPos init_BlockPos = this.init_Minecraft.player.blockPosition();
            final int init_I = Math.max(64, Math.min(16000, AiJson.getInt(args, "maxDistance", 2048)));
            final long init_J = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 1800))) * 1000000000L;
            final Set<String> init_Set = new HashSet<>();
            final Set<String> load_Set = new HashSet<>();
            String init_Str = "";
            String load_Str = "";
            BlockPos load_BlockPos;
            AiOperation init_AiOperation;
            BlockPos read_BlockPos = this.init_BlockPos;
            BlockPos write_BlockPos;
            int load_I;
            int read_I;
            int write_I;
            int update;
            int sync;
            int flush;
            int close;
            int open;
            int poll;
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        AiGuard.formatLowHealthAbort("exploration", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth())
                     );
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Exploration deadline reached");
                  }

                  if (this.init_AiOperation != null) {
                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return r;
                     }

                     this.init_AiOperation = null;
                     if (this.write_BlockPos != null) {
                        return this.init(
                           r.status(),
                           r.status() == AiOperation.Status.SUCCEEDED ? "Arrived at village evidence cluster" : "Village evidence found, but arrival failed"
                        );
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && ++this.poll >= 4) {
                        return this.init(AiOperation.Status.FAILED, "Four exploration frontiers were unreachable");
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && this.load_BlockPos != null) {
                        this.load_Set.add(AiRegions.init(this.load_BlockPos.getX(), this.load_BlockPos.getZ()));
                     }

                     this.init_Str = r.status() == AiOperation.Status.SUCCEEDED ? this.load_Str : "";
                     this.read_BlockPos = this.init_Minecraft.player.blockPosition();
                     this.load_I = 0;
                     this.read_I = this.write_I = this.update = this.sync = this.flush = this.close = 0;
                  }

                  int width = 33;
                  int height = 13;
                  int total = width * width * height;

                  for (int budget = 0; budget < 192 && this.load_I < total; this.load_I++) {
                     int x = this.load_I % width - 16;
                     int z = this.load_I / width % width - 16;
                     int y = this.load_I / (width * width) - 5;
                     BlockPos p = this.read_BlockPos.offset(x, y, z);
                     if (AiTerrain.isLoaded(this.init_Minecraft.level, p)) {
                        net.minecraft.world.level.block.Block b = this.init_Minecraft.level.getBlockState(p).getBlock();
                        if (b instanceof BedBlock && this.init_Minecraft.level.getBlockState(p).getValue(BedBlock.PART) == BedPart.HEAD) {
                           this.read_I++;
                        }

                        if (b == Blocks.BELL) {
                           this.write_I++;
                        }

                        if (b == Blocks.DIRT_PATH) {
                           this.update++;
                        }

                        if (b == Blocks.FARMLAND) {
                           this.sync++;
                        }

                        if (b instanceof DoorBlock && this.init_Minecraft.level.getBlockState(p).getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                           this.flush++;
                        }

                        if (this.init_Minecraft.level.getBlockState(p).is(BlockTags.PLANKS) || b instanceof StairBlock || b == Blocks.COBBLESTONE) {
                           this.close++;
                        }
                     }

                     budget++;
                  }

                  if (this.load_I < total) {
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Surveying loaded terrain", AiJson.objectOf("visited", this.init_Set.size())
                     );
                  }

                  int villagers = 0;

                  for (Entity e : this.init_Minecraft.level.entitiesForRendering()) {
                     if (e instanceof Villager && e.distanceToSqr(this.init_Minecraft.player) < 1024.0) {
                        villagers++;
                     }
                  }

                  String regionCell = AiRegions.init(this.read_BlockPos.getX(), this.read_BlockPos.getZ());
                  AiService service = AiService.getExistingInstance();
                  if (!regionCell.equals(this.load_Str) && service != null) {
                     service.recordSurveyRegion(this.read_BlockPos, this.read_I, this.write_I, this.update, this.sync, villagers, this.init_Str);
                  }

                  this.load_Str = regionCell;
                  if (village
                     && this.read_I >= 3
                     && this.update >= 8
                     && (this.write_I > 0 || villagers >= 2)
                     && this.sync >= 4
                     && this.flush >= 2
                     && this.close >= 16) {
                     this.write_BlockPos = this.read_BlockPos;
                     this.init_AiOperation = AiGoto.travelToCompat(this.write_BlockPos, 3.0, false, false, false, 60);
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING,
                        "Village evidence cluster found; checking arrival",
                        AiJson.objectOf(
                           "beds",
                           this.read_I,
                           "bells",
                           this.write_I,
                           "paths",
                           this.update,
                           "farmland",
                           this.sync,
                           "villagers",
                           villagers,
                           "doors",
                           this.flush,
                           "buildingBlocks",
                           this.close
                        )
                     );
                  }

                  this.init_Set.add(regionCell);
                  if (!village && this.init_Set.size() >= Math.max(1, AiJson.getInt(args, "regions", 8))) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Requested regions explored");
                  }

                  double angle = this.open * 2.399963229728653;
                  int distance = 48 + this.open / 8 * 48;
                  this.open++;
                  if (distance > this.init_I) {
                     return this.init(AiOperation.Status.FAILED, "Search radius exhausted without verified village evidence");
                  }

                  BlockPos next = new BlockPos(
                     this.init_BlockPos.getX() + (int)(Math.cos(angle) * distance),
                     this.init_Minecraft.player.blockPosition().getY(),
                     this.init_BlockPos.getZ() + (int)(Math.sin(angle) * distance)
                  );
                  if (service != null) {
                     HashSet<String> excluded = new HashSet<>(this.init_Set);
                     excluded.addAll(this.load_Set);
                     int[] frontier = AiRegions.init(service.getDimensionRegions(), excluded, this.init_BlockPos.getX(), this.init_BlockPos.getZ(), this.init_I)
                        .stream()
                        .filter(px -> !this.load_Set.contains(AiRegions.init(px[0], px[2])))
                        .min(Comparator.comparingDouble(px -> this.init_Minecraft.player.position().distanceToSqr(px[0], px[1], px[2])))
                        .orElse(null);
                     if (frontier != null) {
                        next = new BlockPos(frontier[0], frontier[1], frontier[2]);
                     }
                  }

                  this.load_BlockPos = next;
                  this.init_AiOperation = AiTravelFar.init(next, 4.0, true, false, false, false, 180);
                  return AiGameOperations.result(
                     AiOperation.Status.RUNNING, "Exploring a new frontier", AiJson.objectOf("target", next.toShortString())
                  );
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Exploration cancelled");
               }
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(
                  status,
                  message,
                  AiJson.objectOf(
                     "visitedRegions",
                     this.init_Set.size(),
                     "villageCandidate",
                     this.write_BlockPos == null ? null : this.write_BlockPos.toShortString(),
                     "beds",
                     this.read_I,
                     "bells",
                     this.write_I,
                     "paths",
                     this.update,
                     "farmland",
                     this.sync,
                     "doors",
                     this.flush,
                     "buildingBlocks",
                     this.close
                  )
               );
            }

            @Override
            public void cancel() {
               this.init_Z = true;
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Exploration requires a connected living player");
      }
   }
}
