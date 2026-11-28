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
   private static final int SCAN_HALF_HEIGHT = 24;
   private static final int MAX_CANDIDATES = 512;
   private static final int read = 2;
   private static final int write = 8;
   private static final double DROP_SEARCH_RADIUS_SQ = 64.0;
   private static final double PICKUP_DISTANCE_SQ = 2.25;
   private static final int update = 10;
   private static final int sync = 16;

   private AiMine() {
   }

   public static AiOperation mine(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final String blockArg = AiMiningTargets.parse(AiJson.getString(args, "block", "")).spec();
         if (blockArg.isEmpty()) {
            throw new IllegalArgumentException("block must be a registry id or a suffix family like \"log\"");
         }

         final AiMiningQuota quota = AiMiningQuota.fromArgs(args);
         final int count = quota == null ? Math.max(1, Math.min(640, AiJson.getInt(args, "count", 1))) : quota.totalCount();
         final int radius = Math.max(8, Math.min(96, AiJson.getInt(args, "radius", 48)));
         final boolean backfill = AiJson.getBoolean(args, "backfill", false);
         final long deadline = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 300))) * 1000000000L;
         return new AiOperation() {
            final Minecraft mc = Minecraft.getInstance();
            final int originalSlot = this.mc.player.getInventory().getSelectedSlot();
            final List<BlockPos> candidates = new ArrayList<>();
            final Set<Long> candidateCells = new HashSet<>();
            final Set<Long> blacklistedCells = new HashSet<>();
            final Map<Long, Integer> strikesByCell = new HashMap<>();
            final Map<Block, Boolean> blockMatchCache = new HashMap<>();
            final Map<Long, String> blockIdByCell = new HashMap<>();
            final Map<AiMine.Inner_DataUtil, Double> breakTicksCache = new HashMap<>();
            long breakTicksInventoryRevision = -1L;
            final JsonArray blacklistJson = new JsonArray();
            final AiAim aim = new AiAim();
            final AiBlockDigger init_RefQueue_DataUtil = new AiBlockDigger(
               this.mc, this::selectSlot, this.aim::aimAt, true, true, this.aim::tick
            );
            final AiBackfill backfiller = backfill ? new AiBackfill(this.mc) : null;
            final int heldAtStart = this.heldDrops();
            final AiPathfinder.LiveGridView grid = new AiPathfinder.LiveGridView(
               this.mc,
               this.mc.player.blockPosition(),
               this.mc.player.blockPosition(),
               this.inventorySnapshot(),
               AiChunkMemory.getInstance()
            );
            FutureTask<MiningTargetSearch.SearchResult> routeSearch;
            long routeSearchVersion = -1L;
            long routeSearchDeadline;
            boolean routeSearchPending;
            boolean routeBudgetExhausted;
            final Object startLevel = this.mc.level;
            BlockPos scanOrigin;
            int columnCursor;
            final IdentityHashMap<LevelChunkSection, Boolean> sectionMatchCache = new IdentityHashMap<>();
            final List<Long> scanChunks = new ArrayList<>();
            long sectionCacheRevision = -1L;
            int chunkCursor;
            int scanChunkRadius = radius + 15 >> 4;
            int sync;
            int flush;
            boolean scanComplete;
            boolean cancelled;
            boolean rescanned;
            Inner_DataUtil phase = Inner_DataUtil.TRAVEL;
            AiOperation travel;
            BlockPos target;
            BlockPos lastBroken;
            List<ItemEntity> drops = new ArrayList<>();
            int close;
            int open;
            int poll;
            int push = -1;

            @Override
            public AiOperation.Result tick() {
               if (this.cancelled || this.mc.player == null || this.mc.level == null) {
                  return this.init(AiOperation.Status.CANCELLED, "Mine cancelled");
               }

               if (this.mc.level != this.startLevel) {
                  return this.init(AiOperation.Status.FAILED, AiFailure.WORLD_CHANGED, "World changed during mining");
               }

               if (this.mc.player.getHealth() <= 4.0F && !AiLowHealth.isOverrideActive()) {
                  return this.init(
                     AiOperation.Status.FAILED,
                     AiFailure.LOW_HEALTH,
                     AiLowHealth.refusalMessage("mining", this.mc.player.getHealth(), this.mc.player.getMaxHealth())
                  );
               }

               if (System.nanoTime() > deadline) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Mine deadline reached");
               }

               if (!this.scanComplete) {
                  this.scanStep();
               }

               this.grid.scanStepCompat();
               if ((quota == null ? this.close >= count : quota.isMet())
                  && this.travel == null
                  && this.phase != Inner_DataUtil.COLLECT) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Mine count reached");
               }

               if (this.travel != null) {
                  AiOperation.Result r = this.travel.tick();
                  if (!r.terminal()) {
                     return this.running(r.message());
                  }

                  this.travel.cancel();
                  this.travel = null;
                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.init(AiOperation.Status.CANCELLED, "Mine cancelled");
                  }

                  if (r.failure() == AiFailure.MISSING_TOOL || r.failure() == AiFailure.TOOL_WORN) {
                     JsonObject data = JsonParser.parseString(r.dataJson()).getAsJsonObject();
                     data.addProperty("broken", this.close);
                     if (quota != null) {
                        data.add("brokenByBlock", quota.brokenByBlock());
                        data.add("remainingByBlock", quota.remaining());
                     }

                     this.cancel();
                     return new AiOperation.Result(r.status(), r.message(), data.toString(), r.evidence(), r.failure());
                  } else if (this.phase == Inner_DataUtil.COLLECT) {
                     if (!this.drops.isEmpty()) {
                        this.drops.remove(0);
                     }

                     this.open = 0;
                     return this.running("Drop collection finished; continuing");
                  } else {
                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.phase = Inner_DataUtil.EXTRACT;
                        this.startRouteSearch();
                        return this.running(
                           "Arrived; extracting " + (this.target == null ? "source" : this.target.toShortString())
                        );
                     }

                     if (r.message() != null && r.message().contains("Player is wedged in solid blocks")) {
                        return this.init(AiOperation.Status.FAILED, AiFailure.UNREACHABLE, r.message());
                     }

                     this.failTarget("travel failed: " + r.message());
                     return this.running("Source unreachable; trying another");
                  }
               } else {
                  if (this.phase == Inner_DataUtil.EXTRACT && this.target != null) {
                     AiOperation.Result toolFail = this.checkTool();
                     if (toolFail != null) {
                        return toolFail;
                     }

                     if (!this.init_RefQueue_DataUtil.isDiggingTargetCompat(this.target)) {
                        if (!this.isWanted(this.target)) {
                           this.target = null;
                           this.phase = Inner_DataUtil.TRAVEL;
                           return this.running("Source changed; re-evaluating");
                        }

                        if (!this.read(this.target) || !this.write(this.target)) {
                           this.failTarget("no line of sight after arrival");
                           return this.running("No line of sight; trying another");
                        }
                     }

                     BlockHitResult interactionHit = AiBlockInteraction.findReachableHit(this.mc, this.target);
                     if (interactionHit != null) {
                        this.init(interactionHit.getLocation());
                     }

                     this.aim.tick();
                     switch (this.init_RefQueue_DataUtil.digCompat(this.target)) {
                        case init:
                           if (this.init_RefQueue_DataUtil.isDiggingTargetCompat(this.target)
                              && !this.mc.level.getBlockState(this.target).isAir()
                              && (!this.read(this.target) || !this.write(this.target))) {
                              this.init_RefQueue_DataUtil.stopCompat();
                              this.failTarget("lost line of sight mid-dig");
                              return this.running("Lost line of sight; trying another");
                           }

                           return this.running("Extracting " + this.target.toShortString());
                        case read:
                           this.init_RefQueue_DataUtil.stopCompat();
                           this.failTarget("break failed");
                           return this.running("Break failed; trying another");
                        case load:
                           this.init_RefQueue_DataUtil.stopCompat();
                           if (quota == null || quota.count(this.blockIdByCell.get(this.target.asLong()))) {
                              this.close++;
                           }

                           if (this.backfiller != null) {
                              this.backfiller.recordDugCell(this.target);
                           }

                           this.onBroken(this.target);
                           this.target = null;
                           return this.running("Mined " + this.close + "/" + count);
                     }
                  }

                  if (this.phase == Inner_DataUtil.COLLECT) {
                     if (!this.drops.isEmpty()) {
                        ItemEntity drop = this.drops.get(0);
                        if (!drop.isAlive()) {
                           this.drops.remove(0);
                           this.open = 0;
                           this.mc.options.keyUp.setDown(false);
                           return this.running("Collecting drops");
                        }

                        if (this.mc.player.distanceToSqr(drop) <= 2.25) {
                           this.mc.options.keyUp.setDown(false);
                           if (this.backfiller != null) {
                              this.backfiller.tick();
                           }

                           if (++this.open > 10) {
                              this.drops.remove(0);
                              this.open = 0;
                           }

                           return this.running("Waiting for drop pickup; check inventory space");
                        }

                        this.open = 0;
                        if (this.mc.player.distanceToSqr(drop) <= 9.0
                           && this.write(BlockPos.containing(drop.position()))
                           && this.update(drop.blockPosition())) {
                           this.init(Vec3.atCenterOf(drop.blockPosition()));
                           this.aim.tick();
                           this.mc.options.keyUp.setDown(true);
                           return this.running("Collecting drops");
                        }

                        this.travel = AiGoto.travelToCompat(drop.blockPosition(), 1.0, true, true, true, this.secondsLeft(25));
                        return this.running("Collecting drops");
                     }

                     this.mc.options.keyUp.setDown(false);
                     if (this.lastBroken != null && ++this.poll <= 8) {
                        this.gatherDropsNear(this.lastBroken);
                        if (this.backfiller != null) {
                           this.backfiller.tick();
                        }

                        return this.running("Waiting for drops");
                     }

                     this.poll = 0;
                     this.lastBroken = null;
                     this.phase = Inner_DataUtil.TRAVEL;
                  }

                  if (this.target == null) {
                     if (quota == null ? this.close >= count : quota.isMet()) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Mine count reached");
                     }

                     if (!this.scanComplete) {
                        return this.running("Discovering loaded sources");
                     }

                     this.target = this.nextTarget();
                     if (this.target != null) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] mine {}: target {} ({} blocks away, {} candidates)",
                              blockArg,
                              this.target.toShortString(),
                              (int)Math.sqrt(this.target.distSqr(this.mc.player.blockPosition())),
                              this.candidates.size()
                           );
                     }

                     if (this.routeSearch != null || this.routeSearchPending) {
                        return this.running("Comparing source routes");
                     }

                     if (this.target == null) {
                        if (this.routeBudgetExhausted) {
                           return this.init(AiOperation.Status.TIMED_OUT, AiFailure.TIMEOUT, "Source route calculation budget exhausted");
                        }

                        if (!this.scanComplete) {
                           if (this.backfiller != null) {
                              this.backfiller.tick();
                           }

                           return this.running("Scanning for sources");
                        }

                        if (this.close < count && this.scanOrigin != null && this.scanOrigin.distSqr(this.mc.player.blockPosition()) > 576.0) {
                           this.scanComplete = false;
                           this.scanOrigin = null;
                           this.chunkCursor = this.columnCursor = 0;
                           return this.running("Sources exhausted; rescanning near the new position");
                        }

                        if (this.close <= 0 && this.blacklistedCells.isEmpty()) {
                           if (!this.rescanned) {
                              this.rescanned = true;
                              this.scanComplete = false;
                              this.scanOrigin = null;
                              this.chunkCursor = this.columnCursor = 0;
                              return this.running("Re-scanning before declaring no sources");
                           }

                           return this.init(AiOperation.Status.FAILED, AiFailure.TARGET_NOT_FOUND, "No matching sources in known terrain (0 of " + count + ")");
                        }

                        return this.init(AiOperation.Status.FAILED, "No more reachable sources (" + this.close + " of " + count + ")");
                     }

                     this.phase = Inner_DataUtil.TRAVEL;
                  }

                  this.travel = AiGoto.approachBlockCompat(this.target, this.secondsLeft(120));
                  return this.running("Travelling to " + this.target.toShortString());
               }
            }

            void scanStep() {
               if (this.scanOrigin == null || this.scanOrigin.distSqr(this.mc.player.blockPosition()) > 576.0) {
                  this.scanOrigin = this.mc.player.blockPosition();
                  this.chunkCursor = this.columnCursor = 0;
                  this.sync = Math.max(this.mc.level.getMinY(), this.scanOrigin.getY() - 24);
                  this.flush = Math.min(this.mc.level.getMaxY() - 1, this.scanOrigin.getY() + 24);
                  this.scanChunks.clear();
                  int ox = this.scanOrigin.getX() >> 4;
                  int oz = this.scanOrigin.getZ() >> 4;

                  for (int dx = -this.scanChunkRadius; dx <= this.scanChunkRadius; dx++) {
                     for (int dz = -this.scanChunkRadius; dz <= this.scanChunkRadius; dz++) {
                        this.scanChunks.add(AiGridRevisions.chunkKey(ox + dx, oz + dz));
                     }
                  }

                  this.scanChunks.sort(Comparator.comparingDouble(k -> {
                     double dx = (int)(k >> 32) * 16 + 8 - this.scanOrigin.getX();
                     double dzx = (int)k.longValue() * 16 + 8 - this.scanOrigin.getZ();
                     return dx * dx + dzx * dzx;
                  }));
               }

               long revision = AiGridRevisions.globalRevision();
               if (this.sectionCacheRevision != revision) {
                  this.sectionMatchCache.clear();
                  this.sectionCacheRevision = revision;
               }

               long stop = System.nanoTime() + 750000L;
               int height = this.flush - this.sync + 1;

               for (int checked = 0; checked < 4096 && this.chunkCursor < this.scanChunks.size(); checked++) {
                  if ((checked & 63) == 0 && System.nanoTime() >= stop) {
                     return;
                  }

                  long key = this.scanChunks.get(this.chunkCursor);
                  int cx = (int)(key >> 32);
                  int cz = (int)key;
                  LevelChunk chunk = this.mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (chunk != null && this.columnCursor < 256 * height) {
                     int column = this.columnCursor / height;
                     int y = this.sync + this.columnCursor % height;
                     int x = (cx << 4) + column / 16;
                     int z = (cz << 4) + column % 16;
                     if (Math.abs(x - this.scanOrigin.getX()) <= radius && Math.abs(z - this.scanOrigin.getZ()) <= radius) {
                        LevelChunkSection section = chunk.getSections()[this.mc.level.getSectionIndex(y)];
                        boolean contains = this.sectionMatchCache
                           .computeIfAbsent(section, part -> !part.hasOnlyAir() && part.maybeHas(statex -> this.init(statex.getBlock())));
                        if (!contains) {
                           this.columnCursor = this.columnCursor + (Math.min(this.flush + 1, (y >> 4) + 1 << 4) - y);
                        } else {
                           this.columnCursor++;
                           BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                           if (this.init(state.getBlock()) && this.candidates.size() < 512) {
                              long cell = BlockPos.asLong(x, y, z);
                              if (!this.blacklistedCells.contains(cell) && this.candidateCells.add(cell)) {
                                 this.candidates.add(new BlockPos(x, y, z));
                              }
                           }
                        }
                     } else {
                        this.columnCursor = (column + 1) * height;
                     }
                  } else {
                     this.chunkCursor++;
                     this.columnCursor = 0;
                  }
               }

               if (this.chunkCursor >= this.scanChunks.size()) {
                  this.scanComplete = true;
               }
            }

            boolean init(Block block) {
               return this.blockMatchCache.computeIfAbsent(block, b -> AiMine.matchesBlock(blockArg, BuiltInRegistries.BLOCK.getKey(b).toString()))
                  && (quota == null || quota.covers(BuiltInRegistries.BLOCK.getKey(block).toString()));
            }

            ItemStack[] inventorySnapshot() {
               ItemStack[] items = new ItemStack[36];

               for (int i = 0; i < items.length; i++) {
                  items[i] = this.mc.player.getInventory().getItem(i).copy();
               }

               return items;
            }

            BlockPos nextTarget() {
               if (this.routeSearch == null) {
                  if (this.routeSearchVersion == this.grid.changeVersionCompat()) {
                     if (System.nanoTime() >= this.routeSearchDeadline) {
                        this.routeSearchPending = false;
                        this.routeBudgetExhausted = true;
                        this.routeSearchVersion = -1L;
                     }

                     return null;
                  } else {
                     this.startRouteSearch();
                     return null;
                  }
               } else {
                  if (!this.routeSearch.isDone()) {
                     return null;
                  }

                  MiningTargetSearch.SearchResult choice;
                  try {
                     choice = this.routeSearch.get();
                  } catch (Exception e) {
                     this.routeSearch = null;
                     throw new IllegalStateException("Target route calculation failed", e);
                  }

                  this.routeSearch = null;
                  this.routeBudgetExhausted = choice.timedOut() && choice.target() == null;
                  if (this.routeBudgetExhausted && !this.candidates.isEmpty()) {
                     BlockPos feet = this.mc.player.blockPosition();
                     BlockPos nearest = this.candidates.stream().filter(this::isWanted).min(Comparator.comparingDouble(p -> p.distSqr(feet))).orElse(null);
                     if (nearest != null) {
                        this.routeBudgetExhausted = false;
                        this.routeSearchPending = false;
                        this.routeSearchVersion = -1L;
                        this.candidates.remove(nearest);
                        this.candidateCells.remove(nearest.asLong());
                        return nearest;
                     }
                  }

                  this.routeSearchPending = choice.pending() && choice.target() == null && System.nanoTime() < this.routeSearchDeadline;
                  if (choice.pending() && choice.target() == null && System.nanoTime() < this.routeSearchDeadline) {
                     return null;
                  } else {
                     this.routeSearchPending = false;
                     this.routeSearchVersion = -1L;
                     if (choice.target() != null && this.isWanted(choice.target())) {
                        this.candidates.remove(choice.target());
                        this.candidateCells.remove(choice.target().asLong());
                        return choice.target();
                     } else {
                        return null;
                     }
                  }
               }
            }

            void startRouteSearch() {
               if (this.routeSearch == null) {
                  if (this.routeSearchVersion < 0L) {
                     this.routeSearchDeadline = Math.min(deadline, System.nanoTime() + 3000000000L);
                  }

                  this.candidates.removeIf(p -> {
                     boolean remove = this.blacklistedCells.contains(p.asLong()) || !this.isWanted(p);
                     if (remove) {
                        this.candidateCells.remove(p.asLong());
                     }

                     return remove;
                  });
                  if (this.candidates.isEmpty()) {
                     this.routeSearchPending = false;
                     this.routeSearchVersion = -1L;
                  } else {
                     ArrayList<MiningTargetSearch.Candidate> choices = new ArrayList<>();
                     ItemStack[] tools = this.inventorySnapshot();
                     this.grid.updateHotbarCompat(tools);
                     long inventoryRevision = AiServerEvidence.inventoryRevision();
                     if (this.breakTicksInventoryRevision != inventoryRevision) {
                        this.breakTicksCache.clear();
                        this.breakTicksInventoryRevision = inventoryRevision;
                     }

                     for (BlockPos candidate : this.candidates) {
                        BlockState state = this.mc.level.getBlockState(candidate);
                        double cost = 1.0 / 0.0;

                        for (ItemStack stack : tools) {
                           AiMine.Inner_DataUtil key = new AiMine.Inner_DataUtil(state.getBlock(), stack.getItem());
                           Double memoized = this.breakTicksCache.get(key);
                           if (memoized == null) {
                              memoized = state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)
                                 ? 1.0 / 0.0
                                 : MiningCost.breakTicks(this.mc, stack, state, state.getDestroySpeed(this.mc.level, candidate));
                              this.breakTicksCache.put(key, memoized);
                           }

                           cost = Math.min(cost, memoized);
                        }

                        if (Double.isFinite(cost)) {
                           choices.add(new MiningTargetSearch.Candidate(candidate, cost));
                        }
                     }

                     BlockPos from = this.mc.player.blockPosition();
                     double reach = this.mc.player.blockInteractionRange() - 0.25;
                     double eye = this.mc.player.getEyeHeight();
                     this.grid.setFocusCompat(from);
                     this.routeSearchPending = true;
                     this.routeSearchVersion = this.grid.changeVersionCompat();
                     boolean sprint = this.mc.player.getFoodData().getFoodLevel() > 6;
                     int constructionBlocks = 0;

                     for (ItemStack stack : tools) {
                        if (AiRestock.isScaffoldBlockId(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                           constructionBlocks += stack.getCount();
                        }
                     }

                     AiPathfinder.PathOptions routeCapabilities = new AiPathfinder.PathOptions(true, true, true, sprint, constructionBlocks > 0)
                        .withConstructionBlocks(constructionBlocks);
                     this.routeSearch = new FutureTask<>(
                        () -> MiningTargetSearch.search(this.grid, from, choices, routeCapabilities, reach, eye, this.routeSearchDeadline)
                     );
                     Thread.ofVirtual().name("mining-target-routes").start(this.routeSearch);
                  }
               }
            }

            void failTarget(String reason) {
               if (this.target == null) {
                  this.phase = Inner_DataUtil.TRAVEL;
               } else {
                  int strikes = this.strikesByCell.merge(this.target.asLong(), 1, Integer::sum);
                  AutismClientAddon.LOG.info("[AI] mine {}: target {} failed ({}/{}): {}", blockArg, this.target.toShortString(), strikes, 2, reason);
                  if (strikes >= 2) {
                     this.blacklistedCells.add(this.target.asLong());
                     this.candidates.remove(this.target);
                     this.candidateCells.remove(this.target.asLong());
                     if (this.blacklistJson.size() < 16) {
                        this.blacklistJson
                           .add(
                              AiJson.objectOf(
                                 "x", this.target.getX(), "y", this.target.getY(), "z", this.target.getZ(), "reason", reason
                              )
                           );
                     }
                  } else {
                     this.candidates.add(this.target);
                     this.candidateCells.add(this.target.asLong());
                  }

                  this.target = null;
                  this.phase = Inner_DataUtil.TRAVEL;
               }
            }

            void onBroken(BlockPos brokenCell) {
               this.lastBroken = brokenCell.immutable();
               this.poll = 0;
               this.gatherDropsNear(brokenCell);
               this.phase = Inner_DataUtil.COLLECT;
            }

            void gatherDropsNear(BlockPos near) {
               this.drops = new ArrayList<>();
               Vec3 center = Vec3.atCenterOf(near);

               for (Entity entity : this.mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item && item.isAlive() && !(item.position().distanceToSqr(center) > 64.0)) {
                     if (this.drops.size() >= 8) {
                        break;
                     }

                     this.drops.add(item);
                  }
               }

               this.drops.sort(Comparator.comparingDouble(this.mc.player::distanceToSqr));
               this.open = 0;
            }

            boolean isWanted(BlockPos pos) {
               BlockState state = this.mc.level.getBlockState(pos);
               Boolean match = this.blockMatchCache.get(state.getBlock());
               if (match == null) {
                  match = AiMine.matchesBlock(blockArg, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                  this.blockMatchCache.put(state.getBlock(), match);
               }

               String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
               boolean wanted = match && (quota == null || quota.covers(id)) && !this.touchesFluid(pos);
               if (wanted && quota != null) {
                  this.blockIdByCell.put(pos.asLong(), id);
               }

               return wanted;
            }

            boolean touchesFluid(BlockPos pos) {
               for (Direction side : Direction.values()) {
                  if (!this.mc.level.getFluidState(pos.relative(side)).isEmpty()) {
                     return true;
                  }
               }

               return false;
            }

            boolean read(BlockPos pos) {
               return AiBlockInteraction.findReachableHit(this.mc, pos) != null;
            }

            boolean write(BlockPos pos) {
               return AiBlockInteraction.findReachableHit(this.mc, pos) != null;
            }

            boolean update(BlockPos to) {
               BlockPos from = this.mc.player.blockPosition();
               if (to.getY() != from.getY()) {
                  return false;
               }

               int dx = to.getX() - from.getX();
               int dz = to.getZ() - from.getZ();
               int ax = Math.abs(dx);
               int az = Math.abs(dz);
               if (Math.max(ax, az) <= 3 && (ax == 0 || az == 0 || ax == az)) {
                  if (!AiMotionSafety.isSegmentHazardFree(this.mc.player.position(), Vec3.atBottomCenterOf(to))) {
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
               this.aim.aimAt(point);
            }

            AiOperation.Result checkTool() {
               BlockState state = this.mc.level.getBlockState(this.target);

               for (int slot = 0; slot < 9; slot++) {
                  ItemStack stack = this.mc.player.getInventory().getItem(slot);
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

               ToolUpkeep.UpkeepDecision decision = ToolUpkeep.decide(this.init(state));
               if (decision instanceof ToolUpkeep.UpkeepDecision.KeepHotbar keep) {
                  this.selectSlot(keep.hotbarSlot());
                  return null;
               } else {
                  if (decision instanceof ToolUpkeep.UpkeepDecision.SwapFromBackpack swap) {
                     int selected = this.mc.player.getInventory().getSelectedSlot();
                     if (InventorySlotHelper.swapToHotbar(this.mc, swap.backpackSlot(), selected)) {
                        this.selectSlot(selected);
                        return null;
                     }
                  }

                  Item needed = AiItems.cheapestRequiredTool(state);
                  JsonObject missing = new JsonObject();
                  missing.addProperty(needed == null ? "minecraft:iron_pickaxe" : BuiltInRegistries.ITEM.getKey(needed).toString(), 1);
                  JsonObject data = this.progressJson();
                  data.addProperty("reason", "TOOL_WORN");
                  data.add("missing", missing);
                  this.cancel();
                  return new AiOperation.Result(
                     AiOperation.Status.FAILED, "TOOL_WORN: no suitable tool above the durability floor for " + blockArg, data.toString(), List.of(), AiFailure.TOOL_WORN
                  );
               }
            }

            List<ToolUpkeep.ToolSlot> init(BlockState state) {
               boolean handsOk = !state.requiresCorrectToolForDrops();
               List<ToolUpkeep.ToolSlot> out = new ArrayList<>(36);

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = this.mc.player.getInventory().getItem(slot);
                  out.add(
                     new ToolUpkeep.ToolSlot(
                        slot,
                        stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : -1,
                        handsOk || stack.isCorrectToolForDrops(state),
                        AiItems.isDisposableTierTool(stack)
                     )
                  );
               }

               return out;
            }

            void selectSlot(int slot) {
               if (this.mc.player.getInventory().getSelectedSlot() != slot) {
                  this.mc.player.getInventory().setSelectedSlot(slot);
                  this.push = slot;
               }
            }

            int secondsLeft(int cap) {
               return (int)Math.max(1L, Math.min(cap, (deadline - System.nanoTime()) / 1000000000L));
            }

            int collectedCount() {
               return Math.max(0, this.heldDrops() - this.heldAtStart);
            }

            int heldDrops() {
               int total = 0;
               String dropArg = AiMine.init(blockArg);
               Inventory inventory = this.mc.player.getInventory();

               for (int i = 0; i < 36; i++) {
                  ItemStack stack = inventory.getItem(i);
                  if (!stack.isEmpty() && AiMine.matchesBlock(dropArg, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                     total += stack.getCount();
                  }
               }

               return total;
            }

            JsonObject progressJson() {
               return AiJson.objectOf(
                  "brokenByBlock",
                  quota == null ? null : quota.brokenByBlock(),
                  "remainingByBlock",
                  quota == null ? null : quota.remaining(),
                  "block",
                  blockArg,
                  "broken",
                  this.close,
                  "collected",
                  this.collectedCount(),
                  "count",
                  count,
                  "candidates",
                  this.candidates.size(),
                  "blacklisted",
                  this.blacklistedCells.size(),
                  "blacklist",
                  this.blacklistJson,
                  "backfilled",
                  this.backfiller == null ? 0 : this.backfiller.getBackfilledCount(),
                  "target",
                  this.target == null ? null : this.target.toShortString(),
                  "scanned",
                  this.scanComplete
               );
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
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
               return new AiOperation.Result(status, message, this.progressJson().toString(), List.of(), reason);
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  if (this.travel != null) {
                     this.travel.cancel();
                     this.travel = null;
                  }

                  this.init_RefQueue_DataUtil.stopCompat();
                  if (this.routeSearch != null) {
                     this.routeSearch.cancel(true);
                  }

                  if (this.backfiller != null) {
                     this.backfiller.reset();
                  }

                  if (this.mc.options != null) {
                     for (KeyMapping key : List.of(
                        this.mc.options.keyUp,
                        this.mc.options.keyDown,
                        this.mc.options.keyLeft,
                        this.mc.options.keyRight,
                        this.mc.options.keyJump,
                        this.mc.options.keySprint,
                        this.mc.options.keyShift
                     )) {
                        key.setDown(KeyMappingControl.of(key).autism$physicalForBody());
                     }
                  }

                  if (this.mc.player != null && this.push >= 0 && this.mc.player.getInventory().getSelectedSlot() == this.push) {
                     this.mc.player.getInventory().setSelectedSlot(this.originalSlot);
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

   public static boolean matchesBlock(String arg, String blockId) {
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
