package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiFailure;
import autismclient.ai.AiItems;
import autismclient.ai.AiJson;
import autismclient.ai.AiMiningQuota;
import autismclient.ai.AiMiningTargets;
import autismclient.ai.AiServerEvidence;
import autismclient.api.ai.AiOperation;
import autismclient.util.KeyMappingControl;
import autismclient.util.InventorySlotHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AiMine {
   private static final int init_I = 24;
   private static final int load_I = 512;
   private static final int read = 2;
   private static final int write = 8;
   private static final double init_D = 64.0;
   private static final double load_D = 2.25;
   private static final int update = 10;
   private static final int sync = 16;

   private AiMine() {
   }

   public static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final String blockArg = AiMiningTargets.parse(AiJson.getString(args, "block", "")).spec();
         if (blockArg.isEmpty()) {
            throw new IllegalArgumentException("block must be a registry id or a suffix family like \"log\"");
         }

         final AiMiningQuota quota = AiMiningQuota.init_as_JsonObject(args);
         final int count = quota == null ? Math.max(1, Math.min(640, AiJson.getInt(args, "count", 1))) : quota.init_I();
         final int radius = Math.max(8, Math.min(96, AiJson.getInt(args, "radius", 48)));
         final boolean backfill = AiJson.getBoolean(args, "backfill", false);
         final long deadline = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 300))) * 1000000000L;
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final int init_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
            final List<BlockPos> init_List = new ArrayList<>();
            final Set<Long> init_Set = new HashSet<>();
            final Set<Long> load_Set = new HashSet<>();
            final Map<Long, Integer> init_Map = new HashMap<>();
            final Map<Block, Boolean> load_Map = new HashMap<>();
            final Map<Long, String> read_Map = new HashMap<>();
            final Map<AiMine.Inner_DataUtil, Double> write_Map = new HashMap<>();
            long init_J = -1L;
            final JsonArray init_JsonArray = new JsonArray();
            final AiAim init_DataUtil = new AiAim();
            final AiBlockDigger init_RefQueue_DataUtil = new AiBlockDigger(
               this.init_Minecraft, this::init_V, this.init_DataUtil::aimAt, true, true, this.init_DataUtil::tick
            );
            final AiBackfill init_StreamUtil = backfill ? new AiBackfill(this.init_Minecraft) : null;
            final int load_I = this.load_I_9();
            final AiPathfinder.LiveGridView init_ClassCache_StreamU = new AiPathfinder.LiveGridView(
               this.init_Minecraft,
               this.init_Minecraft.player.blockPosition(),
               this.init_Minecraft.player.blockPosition(),
               this.init_ItemStackArr_2(),
               AiChunkMemory.init_StateCache()
            );
            FutureTask<MiningTargetSearch.Inner_StreamUtil> init_FutureTask;
            long load_J = -1L;
            long read_J;
            boolean init_Z;
            boolean load_Z;
            final Object init_Obj = this.init_Minecraft.level;
            BlockPos init_BlockPos;
            int read_I;
            final IdentityHashMap<LevelChunkSection, Boolean> init_IdentityHashMap = new IdentityHashMap<>();
            final List<Long> load_List = new ArrayList<>();
            long write_J = -1L;
            int write_I;
            int update_I = radius + 15 >> 4;
            int sync;
            int flush;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            Inner_DataUtil init_TokenStore_1_DataU = Inner_DataUtil.TRAVEL;
            AiOperation init_AiOperation;
            BlockPos load_BlockPos;
            BlockPos read_BlockPos;
            List<ItemEntity> read_List = new ArrayList<>();
            int close;
            int open;
            int poll;
            int push = -1;

            @Override
            public AiOperation.Result tick() {
               if (this.write_Z || this.init_Minecraft.player == null || this.init_Minecraft.level == null) {
                  return this.init(AiOperation.Status.CANCELLED, "Mine cancelled");
               }

               if (this.init_Minecraft.level != this.init_Obj) {
                  return this.init(AiOperation.Status.FAILED, AiFailure.WORLD_CHANGED, "World changed during mining");
               }

               if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiLowHealth.init_Z()) {
                  return this.init(
                     AiOperation.Status.FAILED,
                     AiFailure.LOW_HEALTH,
                     AiLowHealth.init("mining", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth())
                  );
               }

               if (System.nanoTime() > deadline) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Mine deadline reached");
               }

               if (!this.read_Z) {
                  this.init_V_16();
               }

               this.init_ClassCache_StreamU.scanStepCompat();
               if ((quota == null ? this.close >= count : quota.init_Z())
                  && this.init_AiOperation == null
                  && this.init_TokenStore_1_DataU != Inner_DataUtil.COLLECT) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Mine count reached");
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init_AiOperation_Result_7(r.message());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.init(AiOperation.Status.CANCELLED, "Mine cancelled");
                  }

                  if (r.failure() == AiFailure.MISSING_TOOL || r.failure() == AiFailure.TOOL_WORN) {
                     JsonObject data = JsonParser.parseString(r.dataJson()).getAsJsonObject();
                     data.addProperty("broken", this.close);
                     if (quota != null) {
                        data.add("brokenByBlock", quota.init_JsonObject());
                        data.add("remainingByBlock", quota.load());
                     }

                     this.cancel();
                     return new AiOperation.Result(r.status(), r.message(), data.toString(), r.evidence(), r.failure());
                  } else if (this.init_TokenStore_1_DataU == Inner_DataUtil.COLLECT) {
                     if (!this.read_List.isEmpty()) {
                        this.read_List.remove(0);
                     }

                     this.open = 0;
                     return this.init_AiOperation_Result_7("Drop collection finished; continuing");
                  } else {
                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.init_TokenStore_1_DataU = Inner_DataUtil.EXTRACT;
                        this.load_V_9();
                        return this.init_AiOperation_Result_7(
                           "Arrived; extracting " + (this.load_BlockPos == null ? "source" : this.load_BlockPos.toShortString())
                        );
                     }

                     if (r.message() != null && r.message().contains("Player is wedged in solid blocks")) {
                        return this.init(AiOperation.Status.FAILED, AiFailure.UNREACHABLE, r.message());
                     }

                     this.init_V_3("travel failed: " + r.message());
                     return this.init_AiOperation_Result_7("Source unreachable; trying another");
                  }
               } else {
                  if (this.init_TokenStore_1_DataU == Inner_DataUtil.EXTRACT && this.load_BlockPos != null) {
                     AiOperation.Result toolFail = this.init_AiOperation_Result_14();
                     if (toolFail != null) {
                        return toolFail;
                     }

                     if (!this.init_RefQueue_DataUtil.isDiggingTargetCompat(this.load_BlockPos)) {
                        if (!this.init_Z_6(this.load_BlockPos)) {
                           this.load_BlockPos = null;
                           this.init_TokenStore_1_DataU = Inner_DataUtil.TRAVEL;
                           return this.init_AiOperation_Result_7("Source changed; re-evaluating");
                        }

                        if (!this.read(this.load_BlockPos) || !this.write(this.load_BlockPos)) {
                           this.init_V_3("no line of sight after arrival");
                           return this.init_AiOperation_Result_7("No line of sight; trying another");
                        }
                     }

                     BlockHitResult interactionHit = AiBlockInteraction.findReachableHit(this.init_Minecraft, this.load_BlockPos);
                     if (interactionHit != null) {
                        this.init(interactionHit.getLocation());
                     }

                     this.init_DataUtil.tick();
                     switch (this.init_RefQueue_DataUtil.digCompat(this.load_BlockPos)) {
                        case init:
                           if (this.init_RefQueue_DataUtil.isDiggingTargetCompat(this.load_BlockPos)
                              && !this.init_Minecraft.level.getBlockState(this.load_BlockPos).isAir()
                              && (!this.read(this.load_BlockPos) || !this.write(this.load_BlockPos))) {
                              this.init_RefQueue_DataUtil.stopCompat();
                              this.init_V_3("lost line of sight mid-dig");
                              return this.init_AiOperation_Result_7("Lost line of sight; trying another");
                           }

                           return this.init_AiOperation_Result_7("Extracting " + this.load_BlockPos.toShortString());
                        case read:
                           this.init_RefQueue_DataUtil.stopCompat();
                           this.init_V_3("break failed");
                           return this.init_AiOperation_Result_7("Break failed; trying another");
                        case load:
                           this.init_RefQueue_DataUtil.stopCompat();
                           if (quota == null || quota.load(this.read_Map.get(this.load_BlockPos.asLong()))) {
                              this.close++;
                           }

                           if (this.init_StreamUtil != null) {
                              this.init_StreamUtil.init_V(this.load_BlockPos);
                           }

                           this.init_V_5(this.load_BlockPos);
                           this.load_BlockPos = null;
                           return this.init_AiOperation_Result_7("Mined " + this.close + "/" + count);
                     }
                  }

                  if (this.init_TokenStore_1_DataU == Inner_DataUtil.COLLECT) {
                     if (!this.read_List.isEmpty()) {
                        ItemEntity drop = this.read_List.get(0);
                        if (!drop.isAlive()) {
                           this.read_List.remove(0);
                           this.open = 0;
                           this.init_Minecraft.options.keyUp.setDown(false);
                           return this.init_AiOperation_Result_7("Collecting drops");
                        }

                        if (this.init_Minecraft.player.distanceToSqr(drop) <= 2.25) {
                           this.init_Minecraft.options.keyUp.setDown(false);
                           if (this.init_StreamUtil != null) {
                              this.init_StreamUtil.init_V();
                           }

                           if (++this.open > 10) {
                              this.read_List.remove(0);
                              this.open = 0;
                           }

                           return this.init_AiOperation_Result_7("Waiting for drop pickup; check inventory space");
                        }

                        this.open = 0;
                        if (this.init_Minecraft.player.distanceToSqr(drop) <= 9.0
                           && this.write(BlockPos.containing(drop.position()))
                           && this.update(drop.blockPosition())) {
                           this.init(Vec3.atCenterOf(drop.blockPosition()));
                           this.init_DataUtil.tick();
                           this.init_Minecraft.options.keyUp.setDown(true);
                           return this.init_AiOperation_Result_7("Collecting drops");
                        }

                        this.init_AiOperation = AiGoto.travelToCompat(drop.blockPosition(), 1.0, true, true, true, this.init_I(25));
                        return this.init_AiOperation_Result_7("Collecting drops");
                     }

                     this.init_Minecraft.options.keyUp.setDown(false);
                     if (this.read_BlockPos != null && ++this.poll <= 8) {
                        this.load_V_3(this.read_BlockPos);
                        if (this.init_StreamUtil != null) {
                           this.init_StreamUtil.init_V();
                        }

                        return this.init_AiOperation_Result_7("Waiting for drops");
                     }

                     this.poll = 0;
                     this.read_BlockPos = null;
                     this.init_TokenStore_1_DataU = Inner_DataUtil.TRAVEL;
                  }

                  if (this.load_BlockPos == null) {
                     if (quota == null ? this.close >= count : quota.init_Z()) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Mine count reached");
                     }

                     if (!this.read_Z) {
                        return this.init_AiOperation_Result_7("Discovering loaded sources");
                     }

                     this.load_BlockPos = this.init_BlockPos_7();
                     if (this.load_BlockPos != null) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] mine {}: target {} ({} blocks away, {} candidates)",
                              blockArg,
                              this.load_BlockPos.toShortString(),
                              (int)Math.sqrt(this.load_BlockPos.distSqr(this.init_Minecraft.player.blockPosition())),
                              this.init_List.size()
                           );
                     }

                     if (this.init_FutureTask != null || this.init_Z) {
                        return this.init_AiOperation_Result_7("Comparing source routes");
                     }

                     if (this.load_BlockPos == null) {
                        if (this.load_Z) {
                           return this.init(AiOperation.Status.TIMED_OUT, AiFailure.TIMEOUT, "Source route calculation budget exhausted");
                        }

                        if (!this.read_Z) {
                           if (this.init_StreamUtil != null) {
                              this.init_StreamUtil.init_V();
                           }

                           return this.init_AiOperation_Result_7("Scanning for sources");
                        }

                        if (this.close < count && this.init_BlockPos != null && this.init_BlockPos.distSqr(this.init_Minecraft.player.blockPosition()) > 576.0) {
                           this.read_Z = false;
                           this.init_BlockPos = null;
                           this.write_I = this.read_I = 0;
                           return this.init_AiOperation_Result_7("Sources exhausted; rescanning near the new position");
                        }

                        if (this.close <= 0 && this.load_Set.isEmpty()) {
                           if (!this.update_Z) {
                              this.update_Z = true;
                              this.read_Z = false;
                              this.init_BlockPos = null;
                              this.write_I = this.read_I = 0;
                              return this.init_AiOperation_Result_7("Re-scanning before declaring no sources");
                           }

                           return this.init(AiOperation.Status.FAILED, AiFailure.TARGET_NOT_FOUND, "No matching sources in known terrain (0 of " + count + ")");
                        }

                        return this.init(AiOperation.Status.FAILED, "No more reachable sources (" + this.close + " of " + count + ")");
                     }

                     this.init_TokenStore_1_DataU = Inner_DataUtil.TRAVEL;
                  }

                  this.init_AiOperation = AiGoto.approachBlockCompat(this.load_BlockPos, this.init_I(120));
                  return this.init_AiOperation_Result_7("Travelling to " + this.load_BlockPos.toShortString());
               }
            }

            void init_V_16() {
               if (this.init_BlockPos == null || this.init_BlockPos.distSqr(this.init_Minecraft.player.blockPosition()) > 576.0) {
                  this.init_BlockPos = this.init_Minecraft.player.blockPosition();
                  this.write_I = this.read_I = 0;
                  this.sync = Math.max(this.init_Minecraft.level.getMinY(), this.init_BlockPos.getY() - 24);
                  this.flush = Math.min(this.init_Minecraft.level.getMaxY() - 1, this.init_BlockPos.getY() + 24);
                  this.load_List.clear();
                  int ox = this.init_BlockPos.getX() >> 4;
                  int oz = this.init_BlockPos.getZ() >> 4;

                  for (int dx = -this.update_I; dx <= this.update_I; dx++) {
                     for (int dz = -this.update_I; dz <= this.update_I; dz++) {
                        this.load_List.add(AiGridRevisions.init_J(ox + dx, oz + dz));
                     }
                  }

                  this.load_List.sort(Comparator.comparingDouble(k -> {
                     double dx = (int)(k >> 32) * 16 + 8 - this.init_BlockPos.getX();
                     double dzx = (int)k.longValue() * 16 + 8 - this.init_BlockPos.getZ();
                     return dx * dx + dzx * dzx;
                  }));
               }

               long revision = AiGridRevisions.init_J();
               if (this.write_J != revision) {
                  this.init_IdentityHashMap.clear();
                  this.write_J = revision;
               }

               long stop = System.nanoTime() + 750000L;
               int height = this.flush - this.sync + 1;

               for (int checked = 0; checked < 4096 && this.write_I < this.load_List.size(); checked++) {
                  if ((checked & 63) == 0 && System.nanoTime() >= stop) {
                     return;
                  }

                  long key = this.load_List.get(this.write_I);
                  int cx = (int)(key >> 32);
                  int cz = (int)key;
                  LevelChunk chunk = this.init_Minecraft.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (chunk != null && this.read_I < 256 * height) {
                     int column = this.read_I / height;
                     int y = this.sync + this.read_I % height;
                     int x = (cx << 4) + column / 16;
                     int z = (cz << 4) + column % 16;
                     if (Math.abs(x - this.init_BlockPos.getX()) <= radius && Math.abs(z - this.init_BlockPos.getZ()) <= radius) {
                        LevelChunkSection section = chunk.getSections()[this.init_Minecraft.level.getSectionIndex(y)];
                        boolean contains = this.init_IdentityHashMap
                           .computeIfAbsent(section, part -> !part.hasOnlyAir() && part.maybeHas(statex -> this.init(statex.getBlock())));
                        if (!contains) {
                           this.read_I = this.read_I + (Math.min(this.flush + 1, (y >> 4) + 1 << 4) - y);
                        } else {
                           this.read_I++;
                           BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                           if (this.init(state.getBlock()) && this.init_List.size() < 512) {
                              long cell = BlockPos.asLong(x, y, z);
                              if (!this.load_Set.contains(cell) && this.init_Set.add(cell)) {
                                 this.init_List.add(new BlockPos(x, y, z));
                              }
                           }
                        }
                     } else {
                        this.read_I = (column + 1) * height;
                     }
                  } else {
                     this.write_I++;
                     this.read_I = 0;
                  }
               }

               if (this.write_I >= this.load_List.size()) {
                  this.read_Z = true;
               }
            }

            boolean init(Block block) {
               return this.load_Map.computeIfAbsent(block, b -> AiMine.init(blockArg, BuiltInRegistries.BLOCK.getKey(b).toString()))
                  && (quota == null || quota.init_Z_Str(BuiltInRegistries.BLOCK.getKey(block).toString()));
            }

            ItemStack[] init_ItemStackArr_2() {
               ItemStack[] items = new ItemStack[36];

               for (int i = 0; i < items.length; i++) {
                  items[i] = this.init_Minecraft.player.getInventory().getItem(i).copy();
               }

               return items;
            }

            BlockPos init_BlockPos_7() {
               if (this.init_FutureTask == null) {
                  if (this.load_J == this.init_ClassCache_StreamU.changeVersionCompat()) {
                     if (System.nanoTime() >= this.read_J) {
                        this.init_Z = false;
                        this.load_Z = true;
                        this.load_J = -1L;
                     }

                     return null;
                  } else {
                     this.load_V_9();
                     return null;
                  }
               } else {
                  if (!this.init_FutureTask.isDone()) {
                     return null;
                  }

                  MiningTargetSearch.Inner_StreamUtil choice;
                  try {
                     choice = this.init_FutureTask.get();
                  } catch (Exception e) {
                     this.init_FutureTask = null;
                     throw new IllegalStateException("Target route calculation failed", e);
                  }

                  this.init_FutureTask = null;
                  this.load_Z = choice.timedOut() && choice.target() == null;
                  if (this.load_Z && !this.init_List.isEmpty()) {
                     BlockPos feet = this.init_Minecraft.player.blockPosition();
                     BlockPos nearest = this.init_List.stream().filter(this::init_Z_6).min(Comparator.comparingDouble(p -> p.distSqr(feet))).orElse(null);
                     if (nearest != null) {
                        this.load_Z = false;
                        this.init_Z = false;
                        this.load_J = -1L;
                        this.init_List.remove(nearest);
                        this.init_Set.remove(nearest.asLong());
                        return nearest;
                     }
                  }

                  this.init_Z = choice.pending() && choice.target() == null && System.nanoTime() < this.read_J;
                  if (choice.pending() && choice.target() == null && System.nanoTime() < this.read_J) {
                     return null;
                  } else {
                     this.init_Z = false;
                     this.load_J = -1L;
                     if (choice.target() != null && this.init_Z_6(choice.target())) {
                        this.init_List.remove(choice.target());
                        this.init_Set.remove(choice.target().asLong());
                        return choice.target();
                     } else {
                        return null;
                     }
                  }
               }
            }

            void load_V_9() {
               if (this.init_FutureTask == null) {
                  if (this.load_J < 0L) {
                     this.read_J = Math.min(deadline, System.nanoTime() + 3000000000L);
                  }

                  this.init_List.removeIf(p -> {
                     boolean remove = this.load_Set.contains(p.asLong()) || !this.init_Z_6(p);
                     if (remove) {
                        this.init_Set.remove(p.asLong());
                     }

                     return remove;
                  });
                  if (this.init_List.isEmpty()) {
                     this.init_Z = false;
                     this.load_J = -1L;
                  } else {
                     ArrayList<MiningTargetSearch.Inner_DataUtil> choices = new ArrayList<>();
                     ItemStack[] tools = this.init_ItemStackArr_2();
                     this.init_ClassCache_StreamU.updateHotbarCompat(tools);
                     long inventoryRevision = AiServerEvidence.inventoryRevision();
                     if (this.init_J != inventoryRevision) {
                        this.write_Map.clear();
                        this.init_J = inventoryRevision;
                     }

                     for (BlockPos candidate : this.init_List) {
                        BlockState state = this.init_Minecraft.level.getBlockState(candidate);
                        double cost = 1.0 / 0.0;

                        for (ItemStack stack : tools) {
                           AiMine.Inner_DataUtil key = new AiMine.Inner_DataUtil(state.getBlock(), stack.getItem());
                           Double memoized = this.write_Map.get(key);
                           if (memoized == null) {
                              memoized = state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)
                                 ? 1.0 / 0.0
                                 : MiningCost.init_D_Minecraft(this.init_Minecraft, stack, state, state.getDestroySpeed(this.init_Minecraft.level, candidate));
                              this.write_Map.put(key, memoized);
                           }

                           cost = Math.min(cost, memoized);
                        }

                        if (Double.isFinite(cost)) {
                           choices.add(new MiningTargetSearch.Inner_DataUtil(candidate, cost));
                        }
                     }

                     BlockPos from = this.init_Minecraft.player.blockPosition();
                     double reach = this.init_Minecraft.player.blockInteractionRange() - 0.25;
                     double eye = this.init_Minecraft.player.getEyeHeight();
                     this.init_ClassCache_StreamU.setFocusCompat(from);
                     this.init_Z = true;
                     this.load_J = this.init_ClassCache_StreamU.changeVersionCompat();
                     boolean sprint = this.init_Minecraft.player.getFoodData().getFoodLevel() > 6;
                     int constructionBlocks = 0;

                     for (ItemStack stack : tools) {
                        if (AiRestock.isScaffoldBlockId(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                           constructionBlocks += stack.getCount();
                        }
                     }

                     AiPathfinder.PathOptions routeCapabilities = new AiPathfinder.PathOptions(true, true, true, sprint, constructionBlocks > 0)
                        .withConstructionBlocks(constructionBlocks);
                     this.init_FutureTask = new FutureTask<>(
                        () -> MiningTargetSearch.init(this.init_ClassCache_StreamU, from, choices, routeCapabilities, reach, eye, this.read_J)
                     );
                     Thread.ofVirtual().name("mining-target-routes").start(this.init_FutureTask);
                  }
               }
            }

            void init_V_3(String reason) {
               if (this.load_BlockPos == null) {
                  this.init_TokenStore_1_DataU = Inner_DataUtil.TRAVEL;
               } else {
                  int strikes = this.init_Map.merge(this.load_BlockPos.asLong(), 1, Integer::sum);
                  AutismClientAddon.LOG.info("[AI] mine {}: target {} failed ({}/{}): {}", blockArg, this.load_BlockPos.toShortString(), strikes, 2, reason);
                  if (strikes >= 2) {
                     this.load_Set.add(this.load_BlockPos.asLong());
                     this.init_List.remove(this.load_BlockPos);
                     this.init_Set.remove(this.load_BlockPos.asLong());
                     if (this.init_JsonArray.size() < 16) {
                        this.init_JsonArray
                           .add(
                              AiJson.objectOf(
                                 "x", this.load_BlockPos.getX(), "y", this.load_BlockPos.getY(), "z", this.load_BlockPos.getZ(), "reason", reason
                              )
                           );
                     }
                  } else {
                     this.init_List.add(this.load_BlockPos);
                     this.init_Set.add(this.load_BlockPos.asLong());
                  }

                  this.load_BlockPos = null;
                  this.init_TokenStore_1_DataU = Inner_DataUtil.TRAVEL;
               }
            }

            void init_V_5(BlockPos brokenCell) {
               this.read_BlockPos = brokenCell.immutable();
               this.poll = 0;
               this.load_V_3(brokenCell);
               this.init_TokenStore_1_DataU = Inner_DataUtil.COLLECT;
            }

            void load_V_3(BlockPos near) {
               this.read_List = new ArrayList<>();
               Vec3 center = Vec3.atCenterOf(near);

               for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item && item.isAlive() && !(item.position().distanceToSqr(center) > 64.0)) {
                     if (this.read_List.size() >= 8) {
                        break;
                     }

                     this.read_List.add(item);
                  }
               }

               this.read_List.sort(Comparator.comparingDouble(this.init_Minecraft.player::distanceToSqr));
               this.open = 0;
            }

            boolean init_Z_6(BlockPos pos) {
               BlockState state = this.init_Minecraft.level.getBlockState(pos);
               Boolean match = this.load_Map.get(state.getBlock());
               if (match == null) {
                  match = AiMine.init(blockArg, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                  this.load_Map.put(state.getBlock(), match);
               }

               String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
               boolean wanted = match && (quota == null || quota.init_Z_Str(id)) && !this.load_Z_4(pos);
               if (wanted && quota != null) {
                  this.read_Map.put(pos.asLong(), id);
               }

               return wanted;
            }

            boolean load_Z_4(BlockPos pos) {
               for (Direction side : Direction.values()) {
                  if (!this.init_Minecraft.level.getFluidState(pos.relative(side)).isEmpty()) {
                     return true;
                  }
               }

               return false;
            }

            boolean read(BlockPos pos) {
               return AiBlockInteraction.findReachableHit(this.init_Minecraft, pos) != null;
            }

            boolean write(BlockPos pos) {
               return AiBlockInteraction.findReachableHit(this.init_Minecraft, pos) != null;
            }

            boolean update(BlockPos to) {
               BlockPos from = this.init_Minecraft.player.blockPosition();
               if (to.getY() != from.getY()) {
                  return false;
               }

               int dx = to.getX() - from.getX();
               int dz = to.getZ() - from.getZ();
               int ax = Math.abs(dx);
               int az = Math.abs(dz);
               if (Math.max(ax, az) <= 3 && (ax == 0 || az == 0 || ax == az)) {
                  if (!AiMotionSafety.isSegmentHazardFree(this.init_Minecraft.player.position(), Vec3.atBottomCenterOf(to))) {
                     return false;
                  }

                  int x = from.getX();
                  int z = from.getZ();
                  int i = 0;

                  for (int steps = Math.max(ax, az); i < steps; i++) {
                     x += Integer.signum(dx);
                     z += Integer.signum(dz);
                     if (!AiGoto.isStandableCompat(new BlockPos(x, from.getY(), z))) {
                        return false;
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            }

            void init(Vec3 point) {
               this.init_DataUtil.aimAt(point);
            }

            AiOperation.Result init_AiOperation_Result_14() {
               BlockState state = this.init_Minecraft.level.getBlockState(this.load_BlockPos);

               for (int slot = 0; slot < 9; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  boolean suitable = !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state);
                  if (stack.isDamageableItem()) {
                     int missing = stack.getMaxDamage() - stack.getDamageValue();
                  } else {
                     byte var11 = -1;
                  }

                  if (suitable && AiItems.hasEnoughDurability(stack, 2)) {
                     return null;
                  }
               }

               ToolUpkeep.Inner_DataUtil decision = ToolUpkeep.init(this.init(state));
               if (decision instanceof ToolUpkeep.Inner_DataUtil.Inner_StreamUtil keep) {
                  this.init_V(keep.hotbarSlot());
                  return null;
               } else {
                  if (decision instanceof ToolUpkeep.Inner_DataUtil.Inner_CachePool swap) {
                     int selected = this.init_Minecraft.player.getInventory().getSelectedSlot();
                     if (InventorySlotHelper.swapToHotbar(this.init_Minecraft, swap.backpackSlot(), selected)) {
                        this.init_V(selected);
                        return null;
                     }
                  }

                  Item needed = AiItems.cheapestRequiredTool(state);
                  JsonObject missing = new JsonObject();
                  missing.addProperty(needed == null ? "minecraft:iron_pickaxe" : BuiltInRegistries.ITEM.getKey(needed).toString(), 1);
                  JsonObject data = this.init_JsonObject_28();
                  data.addProperty("reason", "TOOL_WORN");
                  data.add("missing", missing);
                  this.cancel();
                  return new AiOperation.Result(
                     AiOperation.Status.FAILED, "TOOL_WORN: no suitable tool above the durability floor for " + blockArg, data.toString(), List.of(), AiFailure.TOOL_WORN
                  );
               }
            }

            List<ToolUpkeep.Inner_StreamUtil> init(BlockState state) {
               boolean handsOk = !state.requiresCorrectToolForDrops();
               List<ToolUpkeep.Inner_StreamUtil> out = new ArrayList<>(36);

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
                  out.add(
                     new ToolUpkeep.Inner_StreamUtil(
                        slot,
                        stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : -1,
                        handsOk || stack.isCorrectToolForDrops(state),
                        AiItems.isDisposableTierTool(stack)
                     )
                  );
               }

               return out;
            }

            void init_V(int slot) {
               if (this.init_Minecraft.player.getInventory().getSelectedSlot() != slot) {
                  this.init_Minecraft.player.getInventory().setSelectedSlot(slot);
                  this.push = slot;
               }
            }

            int init_I(int cap) {
               return (int)Math.max(1L, Math.min(cap, (deadline - System.nanoTime()) / 1000000000L));
            }

            int init_I_15() {
               return Math.max(0, this.load_I_9() - this.load_I);
            }

            int load_I_9() {
               int total = 0;
               String dropArg = AiMine.init(blockArg);
               Inventory inventory = this.init_Minecraft.player.getInventory();

               for (int i = 0; i < 36; i++) {
                  ItemStack stack = inventory.getItem(i);
                  if (!stack.isEmpty() && AiMine.init(dropArg, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                     total += stack.getCount();
                  }
               }

               return total;
            }

            JsonObject init_JsonObject_28() {
               return AiJson.objectOf(
                  "brokenByBlock",
                  quota == null ? null : quota.init_JsonObject(),
                  "remainingByBlock",
                  quota == null ? null : quota.load(),
                  "block",
                  blockArg,
                  "broken",
                  this.close,
                  "collected",
                  this.init_I_15(),
                  "count",
                  count,
                  "candidates",
                  this.init_List.size(),
                  "blacklisted",
                  this.load_Set.size(),
                  "blacklist",
                  this.init_JsonArray,
                  "backfilled",
                  this.init_StreamUtil == null ? 0 : this.init_StreamUtil.init_I(),
                  "target",
                  this.load_BlockPos == null ? null : this.load_BlockPos.toShortString(),
                  "scanned",
                  this.read_Z
               );
            }

            AiOperation.Result init_AiOperation_Result_7(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_28().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(
                  status,
                  status == AiOperation.Status.FAILED
                     ? AiFailure.NO_REACHABLE_TARGET
                     : (status == AiOperation.Status.TIMED_OUT ? AiFailure.TIMEOUT : (status == AiOperation.Status.CANCELLED ? AiFailure.CANCELLED : null)),
                  message
               );
            }

            AiOperation.Result init(AiOperation.Status status, AiFailure reason, String message) {
               this.cancel();
               return new AiOperation.Result(status, message, this.init_JsonObject_28().toString(), List.of(), reason);
            }

            @Override
            public void cancel() {
               if (!this.write_Z) {
                  this.write_Z = true;
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  this.init_RefQueue_DataUtil.stopCompat();
                  if (this.init_FutureTask != null) {
                     this.init_FutureTask.cancel(true);
                  }

                  if (this.init_StreamUtil != null) {
                     this.init_StreamUtil.load_V();
                  }

                  if (this.init_Minecraft.options != null) {
                     for (KeyMapping key : List.of(
                        this.init_Minecraft.options.keyUp,
                        this.init_Minecraft.options.keyDown,
                        this.init_Minecraft.options.keyLeft,
                        this.init_Minecraft.options.keyRight,
                        this.init_Minecraft.options.keyJump,
                        this.init_Minecraft.options.keySprint,
                        this.init_Minecraft.options.keyShift
                     )) {
                        key.setDown(KeyMappingControl.of(key).autism$physicalForBody());
                     }
                  }

                  if (this.init_Minecraft.player != null && this.push >= 0 && this.init_Minecraft.player.getInventory().getSelectedSlot() == this.push) {
                     this.init_Minecraft.player.getInventory().setSelectedSlot(this.init_I);
                  }
               }
            }

            enum Inner_DataUtil {
               TRAVEL,
               EXTRACT,
               COLLECT;
            }
         };
      } else {
         throw new IllegalArgumentException("Mining requires a connected living player");
      }
   }

   public static boolean init(String arg, String blockId) {
      return AiMiningTargets.matches(arg, blockId);
   }

   static String init(String blockArg) {
      if (blockArg.indexOf(44) >= 0) {
         return AiMiningTargets.parse(blockArg).types().stream().map(AiMine::init).distinct().collect(Collectors.joining(","));
      }

      if (blockArg.indexOf(58) < 0) {
         return blockArg;
      }

      String path = blockArg.substring(blockArg.indexOf(58) + 1).replace("deepslate_", "");
      if (!path.endsWith("_ore")) {
         return path;
      }

      String base = path.substring(0, path.length() - 4);
      String var10000;
      switch (base) {
         case "iron":
         case "gold":
         case "copper":
            var10000 = "raw_" + base;
            break;
         case "lapis":
            var10000 = "lapis_lazuli";
            break;
         case "nether_quartz":
            var10000 = "quartz";
            break;
         case "nether_gold":
            var10000 = "gold_nugget";
            break;
         default:
            var10000 = base;
      }

      return var10000;
   }

   private record Inner_DataUtil(Block block, Item item) {
   }
}
