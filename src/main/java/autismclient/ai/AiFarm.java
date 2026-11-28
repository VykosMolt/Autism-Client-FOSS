package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiFarm {
   private static final int init_I = 10;
   private static final int load_I = 40;
   private static final double init_D = 16.0;
   private static final Map<String, String> YIELD_BY_CROP = new LinkedHashMap<>();
   private static final Map<String, String> CROP_BY_YIELD = new LinkedHashMap<>();
   private static final Map<String, String> SEED_FOR_CROP = Map.ofEntries(
      Map.entry("minecraft:wheat", "minecraft:wheat_seeds"),
      Map.entry("minecraft:carrots", "minecraft:carrot"),
      Map.entry("minecraft:potatoes", "minecraft:potato"),
      Map.entry("minecraft:beetroots", "minecraft:beetroot_seeds"),
      Map.entry("minecraft:nether_wart", "minecraft:nether_wart"),
      Map.entry("minecraft:cocoa", "minecraft:cocoa_beans")
   );
   private static final Map<String, List<String>> BREEDING_FOOD = Map.ofEntries(
      Map.entry("minecraft:cow", List.of("minecraft:wheat")),
      Map.entry("minecraft:mooshroom", List.of("minecraft:wheat")),
      Map.entry("minecraft:sheep", List.of("minecraft:wheat")),
      Map.entry("minecraft:goat", List.of("minecraft:wheat")),
      Map.entry("minecraft:pig", List.of("minecraft:carrot", "minecraft:potato", "minecraft:beetroot")),
      Map.entry("minecraft:chicken", List.of("minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds")),
      Map.entry("minecraft:rabbit", List.of("minecraft:carrot", "minecraft:golden_carrot", "minecraft:dandelion")),
      Map.entry("minecraft:wolf", List.of("minecraft:beef", "minecraft:porkchop", "minecraft:mutton")),
      Map.entry("minecraft:cat", List.of("minecraft:cod", "minecraft:salmon")),
      Map.entry("minecraft:horse", List.of("minecraft:golden_apple", "minecraft:golden_carrot")),
      Map.entry("minecraft:llama", List.of("minecraft:hay_block")),
      Map.entry("minecraft:turtle", List.of("minecraft:seagrass")),
      Map.entry("minecraft:panda", List.of("minecraft:bamboo")),
      Map.entry("minecraft:fox", List.of("minecraft:sweet_berries", "minecraft:glow_berries")),
      Map.entry("minecraft:bee", List.of("minecraft:dandelion", "minecraft:poppy", "minecraft:cornflower")),
      Map.entry("minecraft:frog", List.of("minecraft:slime_ball")),
      Map.entry("minecraft:camel", List.of("minecraft:cactus")),
      Map.entry("minecraft:armadillo", List.of("minecraft:spider_eye")),
      Map.entry("minecraft:sniffer", List.of("minecraft:torchflower_seeds"))
   );
   private static final int SCAN_STEP_LIMIT = 4096;
   private static final long SCAN_BUDGET_NANOS = 750000L;
   private static final int write_I = 32;
   private static final int update = 16;
   private static final int sync = 3;
   private static final int flush = 32;
   private static final int close = 1200;
   private static final int open = 100;
   private static final int poll = 16;
   private static final int push = 40;
   private static final double DROP_SEARCH_RADIUS_SQ = 64.0;
   private static final double PICKUP_DISTANCE_SQ = 2.25;
   private static final String BONE_MEAL = "minecraft:bone_meal";
   private static final int[][] COLUMN_OFFSETS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {0, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   static final Set<String> FIELD_CROPS = Set.of("minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots", "minecraft:nether_wart");

   private AiFarm() {
   }

   static AiOperation init(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         AiFarm.Inner_DataUtil action = parseAction(AiJson.getString(args, "action", "harvest"));
         if (action == AiFarm.Inner_DataUtil.HARVEST) {
            String spec = AiJson.getString(args, "crop", AiJson.getString(args, "item", "minecraft:wheat"));
            String crop = resolveCrop(spec);
            return FIELD_CROPS.contains(crop) ? harvestField(args, crop, YIELD_BY_CROP.get(crop)) : init(args, crop, YIELD_BY_CROP.get(crop));
         } else if (action == AiFarm.Inner_DataUtil.PLANT) {
            String crop = resolveCrop(AiJson.getString(args, "crop", AiJson.getString(args, "item", "minecraft:wheat")));
            return AiFarmPlant.init(args, crop, load(crop));
         } else if (action == AiFarm.Inner_DataUtil.BUILD) {
            String crop = resolveCrop(AiJson.getString(args, "crop", AiJson.getString(args, "item", "minecraft:wheat")));
            return AiFarmBuild.init(args, crop, load(crop));
         } else {
            return breed(args);
         }
      } else {
         throw new IllegalArgumentException("Farming requires a connected living player");
      }
   }

   static String resolveCrop(String spec) {
      String clean = spec.trim().toLowerCase(Locale.ROOT);
      String full = clean.contains(":") ? clean : "minecraft:" + clean;
      if (YIELD_BY_CROP.containsKey(full)) {
         return full;
      } else if (CROP_BY_YIELD.containsKey(full)) {
         return CROP_BY_YIELD.get(full);
      } else {
         throw new IllegalArgumentException("'" + spec + "' is not a farmable crop; known crops: " + String.join(", ", YIELD_BY_CROP.keySet()));
      }
   }

   static AiFarm.Inner_StreamUtil harvestKindFor(Block block) {
      if (block == Blocks.SWEET_BERRY_BUSH) {
         return AiFarm.Inner_StreamUtil.INTERACT;
      } else if (autismclient.util.FarmCropCatalog.load(block)) {
         return AiFarm.Inner_StreamUtil.COLUMN;
      } else {
         return autismclient.util.FarmCropCatalog.read(block) ? AiFarm.Inner_StreamUtil.BREAK_ONLY : AiFarm.Inner_StreamUtil.BREAK_REPLANT;
      }
   }

   static String load(String cropBlockId) {
      return SEED_FOR_CROP.get(cropBlockId);
   }

   static int cropAge(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
            return state.getValue(age);
         }
      }

      return -1;
   }

   static int maxCropAge(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
            return Collections.max(age.getPossibleValues());
         }
      }

      return -1;
   }

   static boolean isRipe(BlockState state) {
      int max = maxCropAge(state);
      return max < 0 || cropAge(state) >= max;
   }

   static boolean canBonemeal(BlockState state) {
      int max = maxCropAge(state);
      return state.getBlock() instanceof BonemealableBlock && max >= 0 && cropAge(state) < max;
   }

   static BlockPos harvestableColumnSegment(Function<BlockPos, BlockState> at, BlockPos pos) {
      BlockState state = at.apply(pos);
      if (state != null && autismclient.util.FarmCropCatalog.load(state.getBlock())) {
         BlockPos base = pos;

         while (at.apply(base.below()) != null && at.apply(base.below()).is(state.getBlock())) {
            base = base.below();
         }

         BlockPos segment = base.above();
         return at.apply(segment) != null && at.apply(segment).is(state.getBlock()) ? segment : null;
      } else {
         return null;
      }
   }

   static Direction facingOf(BlockState state) {
      return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
   }

   static AiOperation init(final JsonObject args, final String cropBlock, final String cropItem) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final Block crop = BuiltInRegistries.BLOCK
            .getOptional(Identifier.parse(cropBlock))
            .orElseThrow(() -> new IllegalArgumentException("Unknown crop block " + cropBlock));
         final AiFarm.Inner_StreamUtil kind = harvestKindFor(crop);
         final String seed = load(cropBlock);
         return new AiOperation() {
            final Minecraft mc = Minecraft.getInstance();
            final BlockPos origin = this.mc.player.blockPosition();
            final int originalSlot = this.mc.player.getInventory().getSelectedSlot();
            final int heldAtStart = AiGameState.countInventoryItem(cropItem);
            final int targetCount = AiJson.getInt(args, "count", 53) + (AiJson.getBoolean(args, "additional", false) ? this.heldAtStart : 0);
            final boolean exactCount = AiJson.getBoolean(args, "exact", false);
            final boolean useBonemeal = AiJson.getBoolean(args, "bonemeal", false);
            final boolean replantAfter = AiJson.getBoolean(args, "replant", true);
            final long deadline = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 600))) * 1000000000L;
            final int write_I = 2;
            final int update_I = 5;
            final int sync = 25;
            final int minScanY = Math.max(this.mc.level.getMinY(), this.origin.getY() - 16);
            final int maxScanY = Math.min(this.mc.level.getMaxY() - 1, this.origin.getY() + 16);
            final List<AiFarm.Inner_CachePool> matureWork = new ArrayList<>();
            final List<AiFarm.Inner_CachePool> growingWork = new ArrayList<>();
            final ArrayDeque<AiFarm.Inner_CachePool> followUps = new ArrayDeque<>();
            final Set<Long> skippedCells = new LinkedHashSet<>();
            final List<JsonObject> skipLog = new ArrayList<>();
            final Map<String, Integer> dropBaseline = new HashMap<>();
            final Map<Long, Integer> replantNotBefore = new HashMap<>();
            final Map<Long, Long> harvestRevision = new HashMap<>();
            long startInventoryRevision = AiServerEvidence.inventoryRevision();
            BlockPos scanCentre = this.origin;
            AiFarm.Inner_CachePool currentWork;
            AiOperation approach;
            AiOperation action;
            BlockPos replantBase;
            Direction replantFace;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            BlockPos walkTarget;
            double walkRange;
            double lastWalkDistSqr = -1.0;
            int walkStallTicks;
            final Deque<AiFarm.Inner_CachePool> deferredDrops = new ArrayDeque<>();
            final List<AiOperation> pendingReplantOps = new ArrayList<>();
            final List<AiFarm.Inner_CachePool> pendingReplantWork = new ArrayList<>();
            final IdentityHashMap<LevelChunkSection, Boolean> sectionHasCrop = new IdentityHashMap<>();
            long sectionCacheRevision = -1L;
            int scanChunkIndex;
            int scanCellCursor;
            int idleRescanTicks;
            int workAttempts;
            int columnOffsetCursor;
            int dropPickupWaitTicks;
            int replantConfirmTicks;
            int seedHandWaitTicks;
            int lastSeenCount = this.heldAtStart;
            int idleTicks;
            int verifyTicks;
            int serverConfirmTicks;
            int harvestedCount;
            int replantedCount;
            int externalReplantCount;
            int replantSkippedCount;
            int dropCount;
            int bonemealCount;
            int skippedTotal;
            boolean scanned;
            boolean cancelled;
            String blockedReason = "";

            @Override
            public AiOperation.Result tick() {
               if (this.cancelled || this.mc.player == null || this.mc.level == null) {
                  return this.finish(AiOperation.Status.CANCELLED, "Harvest cancelled");
               }

               if (this.targetCount >= 0 && this.targetCount <= 2304) {
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Harvest target not reached before deadline");
                  }

                  if (this.mc.gui.screen() != null) {
                     autismclient.util.BlockAttackGraceWindow.init();
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     this.aim.clear();
                     return this.running("Harvest paused while a screen is open");
                  }

                  if (this.mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.finish(
                        AiOperation.Status.FAILED,
                        AiGuard.formatLowHealthAbort("harvesting", this.mc.player.getHealth(), this.mc.player.getMaxHealth())
                     );
                  }

                  int count = AiGameState.countInventoryItem(cropItem);

                  for (int i = this.pendingReplantOps.size() - 1; i >= 0; i--) {
                     AiOperation.Result v = this.pendingReplantOps.get(i).tick();
                     if (v.terminal()) {
                        this.pendingReplantOps.remove(i);
                        AiFarm.Inner_CachePool vw = this.pendingReplantWork.remove(i);
                        if (v.status() == AiOperation.Status.SUCCEEDED) {
                           this.replantedCount++;
                        } else {
                           this.replantSkippedCount++;
                           this.recordSkip(vw.cell(), v.message());
                        }
                     }
                  }

                  boolean owesReplant = this.currentWork != null
                        && (this.currentWork.kind() == AiFarm.Inner_BufferHelper.HARVEST || this.currentWork.kind() == AiFarm.Inner_BufferHelper.REPLANT)
                     || this.followUps.stream().anyMatch(w -> w.kind() == AiFarm.Inner_BufferHelper.REPLANT);
                  if (count < this.targetCount || owesReplant) {
                     this.serverConfirmTicks = 0;
                     this.verifyTicks = 0;
                     this.replantConfirmTicks = 0;
                     if (count < this.targetCount && !this.hasRoomFor(cropItem)) {
                        return this.finish(AiOperation.Status.FAILED, "Inventory full: make room for " + cropItem + " before harvesting");
                     }

                     if (count > this.lastSeenCount) {
                        this.idleTicks = 0;
                     }

                     this.lastSeenCount = count;
                     if (!this.scanned) {
                        return this.scanStep();
                     }

                     if (this.approach != null) {
                        AiOperation.Result r = this.approach.tick();
                        if (!r.terminal()) {
                           return this.running("Walking to the next plant");
                        }

                        this.approach = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           return this.workAttemptFailed("no reachable approach");
                        }

                        if (this.currentWork != null && this.currentWork.kind() != AiFarm.Inner_BufferHelper.DROP) {
                           BlockPos cell = this.currentWork.kind() == AiFarm.Inner_BufferHelper.REPLANT
                              ? this.currentWork.base()
                              : this.currentWork.cell();
                           if (!this.isWithinReach(cell) || !this.canSeeWorkFace(this.currentWork)) {
                              return this.workAttemptFailed("no clear line of sight after approach");
                           }
                        }

                        return this.running("Approach finished; re-evaluating");
                     } else {
                        if (this.walkTarget != null) {
                           return this.walkStep();
                        }

                        if (this.action != null) {
                           AiOperation.Result r = this.action.tick();
                           if (!r.terminal()) {
                              if (this.currentWork == null
                                 || this.currentWork.kind() != AiFarm.Inner_BufferHelper.REPLANT
                                 || !(this.action instanceof AiActedOperation actedOp)
                                 || !actedOp.init()) {
                                 return this.running(this.activityText());
                              }

                              this.pendingReplantOps.add(this.action);
                              this.pendingReplantWork.add(this.currentWork);
                              this.action = null;
                              this.currentWork = null;
                              this.workAttempts = 0;
                              this.columnOffsetCursor = 0;
                           } else {
                              this.action.cancel();
                              this.action = null;
                              AiOperation.Result settled = this.settleWork(this.currentWork, r);
                              if (settled.terminal()) {
                                 return settled;
                              }
                           }
                        }

                        if (this.currentWork == null || !this.stillValid(this.currentWork)) {
                           if (this.currentWork != null && this.currentWork.kind() == AiFarm.Inner_BufferHelper.DROP) {
                              this.countDrop(this.currentWork);
                           }

                           this.currentWork = null;
                           this.workAttempts = 0;
                           this.columnOffsetCursor = 0;
                           this.currentWork = this.nextWork();
                           if (!this.blockedReason.isEmpty()) {
                              return this.finish(AiOperation.Status.FAILED, this.blockedReason);
                           }
                        }

                        if (this.currentWork != null) {
                           this.idleTicks = 0;
                           this.idleRescanTicks = 0;
                           return this.startWork(this.currentWork);
                        }

                        if (!this.pendingReplantOps.isEmpty()) {
                           return this.running("Confirming the last replant");
                        }

                        if (++this.idleRescanTicks >= 100) {
                           this.rescan();
                        }

                        return ++this.idleTicks > 1200
                           ? this.finish(
                              AiOperation.Status.FAILED, "Nothing harvested for 60 seconds; locate reachable mature " + cropBlock + " or free inventory space"
                           )
                           : this.running(this.harvestedCount == 0 ? "Scanning for " + cropBlock : "Waiting for regrowth");
                     }
                  } else if (!this.pendingReplantOps.isEmpty()) {
                     return ++this.replantConfirmTicks > 200
                        ? this.finish(AiOperation.Status.UNCERTAIN, "The last replant is still unconfirmed; inspect the field before repeating")
                        : this.running("Confirming the last replants");
                  } else if (count > this.heldAtStart && AiServerEvidence.inventoryRevision() <= this.startInventoryRevision) {
                     return ++this.serverConfirmTicks > 200
                        ? this.finish(
                           AiOperation.Status.UNCERTAIN, "Harvest target observed locally, but server confirmation not received; inspect before repeating"
                        )
                        : this.running("Verifying harvested inventory after server updates");
                  } else if (count > this.heldAtStart && ++this.verifyTicks < 5) {
                     return this.running("Verifying harvested inventory after server updates");
                  } else {
                     return this.replantSkippedCount > 0
                        ? this.finish(
                           AiOperation.Status.FAILED, "Harvested the target, but " + this.replantSkippedCount + " crops could not be replanted; inspect the reported cells"
                        )
                        : this.finish(
                           this.exactCount && count != this.targetCount ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED,
                           this.exactCount && count != this.targetCount
                              ? "Surplus retained; exact target requires storage reconciliation"
                              : "Harvest inventory target reached"
                        );
                  }
               } else {
                  return this.finish(AiOperation.Status.FAILED, "Harvest target must fit player inventory");
               }
            }

            boolean hasRoomFor(String itemId) {
               AiItems.ItemSpec wanted = AiItems.resolveItem(itemId);
               Inventory inventory = this.mc.player.getInventory();

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = inventory.getItem(slot);
                  if (stack.isEmpty() || wanted.matches(stack) && stack.getCount() < stack.getMaxStackSize()) {
                     return true;
                  }
               }

               return false;
            }

            AiOperation.Result scanStep() {
               long revision = autismclient.ai.engine.AiGridRevisions.globalRevision();
               if (this.sectionCacheRevision != revision) {
                  this.sectionHasCrop.clear();
                  this.sectionCacheRevision = revision;
               }

               long stop = System.nanoTime() + 750000L;
               int height = this.maxScanY - this.minScanY + 1;

               for (int checked = 0; checked < 4096 && this.scanChunkIndex < 25; checked++) {
                  if ((checked & 63) == 0 && System.nanoTime() >= stop) {
                     return this.running("Scanning for " + cropBlock);
                  }

                  int cx = (this.scanCentre.getX() >> 4) - 2 + this.scanChunkIndex % 5;
                  int cz = (this.scanCentre.getZ() >> 4) - 2 + this.scanChunkIndex / 5;
                  LevelChunk chunk = this.mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (chunk != null && this.scanCellCursor < 256 * height) {
                     int column = this.scanCellCursor / height;
                     int y = this.minScanY + this.scanCellCursor % height;
                     int x = (cx << 4) + column / 16;
                     int z = (cz << 4) + column % 16;
                     if (Math.abs(x - this.scanCentre.getX()) <= 32 && Math.abs(z - this.scanCentre.getZ()) <= 32) {
                        LevelChunkSection section = chunk.getSections()[this.mc.level.getSectionIndex(y)];
                        boolean contains = this.sectionHasCrop
                           .computeIfAbsent(section, part -> !part.hasOnlyAir() && part.maybeHas(statex -> statex.is(crop)));
                        if (!contains) {
                           this.scanCellCursor = this.scanCellCursor + (Math.min(this.maxScanY + 1, (y >> 4) + 1 << 4) - y);
                        } else {
                           this.scanCellCursor++;
                           BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                           if (state.is(crop)) {
                              BlockPos cell = new BlockPos(x, y, z);
                              if (!this.skippedCells.contains(cell.asLong()) && !this.isQueued(cell)) {
                                 if (kind == AiFarm.Inner_StreamUtil.COLUMN) {
                                    BlockPos segment = AiFarm.harvestableColumnSegment(pos -> this.mc.level.getBlockState(pos), cell);
                                    if (segment != null && !this.skippedCells.contains(segment.asLong()) && !this.isQueued(segment)) {
                                       this.matureWork
                                          .add(AiFarm.Inner_CachePool.harvest(segment, this.mc.player.distanceToSqr(Vec3.atCenterOf(segment))));
                                    }
                                 } else if (AiFarm.isRipe(state)) {
                                    this.matureWork.add(AiFarm.Inner_CachePool.harvest(cell, this.mc.player.distanceToSqr(Vec3.atCenterOf(cell))));
                                 } else if (this.useBonemeal && AiFarm.canBonemeal(state)) {
                                    this.growingWork.add(AiFarm.Inner_CachePool.bonemeal(cell, this.mc.player.distanceToSqr(Vec3.atCenterOf(cell))));
                                 }
                              }
                           }
                        }
                     } else {
                        this.scanCellCursor = (column + 1) * height;
                     }
                  } else {
                     this.scanChunkIndex++;
                     this.scanCellCursor = 0;
                  }
               }

               if (this.scanChunkIndex < 25) {
                  return this.running("Scanning for " + cropBlock);
               }

               this.scanned = true;
               Comparator<AiFarm.Inner_CachePool> nearest = Comparator.comparingDouble(AiFarm.Inner_CachePool::distanceSqr);
               this.matureWork.sort(nearest);
               this.growingWork.sort(nearest);
               return this.running(
                  "Found " + this.matureWork.size() + " mature " + cropBlock + (this.growingWork.isEmpty() ? "" : " and " + this.growingWork.size() + " growing")
               );
            }

            void rescan() {
               this.scanCentre = this.mc.player.blockPosition();
               this.scanChunkIndex = 0;
               this.scanCellCursor = 0;
               this.idleRescanTicks = 0;
               this.matureWork.clear();
               this.growingWork.clear();
               this.scanned = false;
            }

            boolean isQueued(BlockPos cell) {
               for (AiFarm.Inner_CachePool w : this.matureWork) {
                  if (w.cell().equals(cell)) {
                     return true;
                  }
               }

               for (AiFarm.Inner_CachePool w : this.growingWork) {
                  if (w.cell().equals(cell)) {
                     return true;
                  }
               }

               return this.currentWork != null && this.currentWork.cell().equals(cell);
            }

            boolean stillValid(AiFarm.Inner_CachePool w) {
               switch (w.kind()) {
                  case HARVEST:
                     if (!AiTerrain.isLoaded(this.mc.level, w.cell())) {
                        return false;
                     }

                     BlockState state = this.mc.level.getBlockState(w.cell());
                     return kind == AiFarm.Inner_StreamUtil.COLUMN ? state.is(crop) : state.is(crop) && AiFarm.isRipe(state);
                  case REPLANT:
                     return seed != null && this.mc.level.getBlockState(w.cell()).isAir();
                  case BONEMEAL:
                     return AiTerrain.isLoaded(this.mc.level, w.cell())
                        && AiFarm.canBonemeal(this.mc.level.getBlockState(w.cell()))
                        && AiGameState.countInventoryItem("minecraft:bone_meal") > 0;
                  case DROP:
                     return this.mc.level.getEntity(w.entityId()) instanceof ItemEntity drop && drop.isAlive();
                  default:
                     return false;
               }
            }

            AiFarm.Inner_CachePool nextWork() {
               int queued = this.followUps.size();
               boolean enough = AiGameState.countInventoryItem(cropItem) >= this.targetCount;
               boolean needsSeed = seed != null
                  && AiGameState.countInventoryItem(seed) == 0
                  && this.followUps.stream().anyMatch(wx -> wx.kind() == AiFarm.Inner_BufferHelper.REPLANT);

               while (queued-- > 0) {
                  AiFarm.Inner_CachePool w = this.followUps.poll();
                  if (w.kind() == AiFarm.Inner_BufferHelper.REPLANT) {
                     AiServerEvidence.BlockUpdate update = AiServerEvidence.lastBlockUpdate(w.cell());
                     if (AiReplantEvidence.init(
                           this.mc.level.getBlockState(w.cell()),
                           update,
                           this.harvestRevision.getOrDefault(w.cell().asLong(), 0L),
                           crop,
                           false,
                           AiGameState.countInventoryItem(seed)
                        )
                        == AiReplantEvidence.ReplantState.EXTERNAL) {
                        this.externalReplantCount++;
                        this.replantNotBefore.remove(w.cell().asLong());
                        this.harvestRevision.remove(w.cell().asLong());
                        continue;
                     }

                     if (AiGameState.countInventoryItem(seed) <= 0) {
                        if (this.mc.player.tickCount - this.replantNotBefore.getOrDefault(w.cell().asLong(), this.mc.player.tickCount) > 120) {
                           this.blockedReason = "No " + seed + " to finish the replant at " + w.cell().toShortString();
                           return null;
                        }

                        this.followUps.addLast(w);
                        continue;
                     }

                     if (this.mc.player.tickCount < this.replantNotBefore.getOrDefault(w.cell().asLong(), 0)
                        || update == null
                        || update.revision() <= this.harvestRevision.getOrDefault(w.cell().asLong(), 0L)
                        || !update.state().isAir()) {
                        this.followUps.addLast(w);
                        continue;
                     }
                  }

                  if (w.kind() != AiFarm.Inner_BufferHelper.DROP || enough || needsSeed || this.matureWork.isEmpty() && (!this.useBonemeal || this.growingWork.isEmpty())) {
                     if (this.stillValid(w)) {
                        return w;
                     }

                     if (w.kind() == AiFarm.Inner_BufferHelper.REPLANT) {
                        this.replantSkippedCount++;
                     }

                     if (w.kind() == AiFarm.Inner_BufferHelper.DROP) {
                        this.countDrop(w);
                     }
                  } else {
                     this.deferredDrops.addLast(w);
                  }
               }

               while (!enough && !this.matureWork.isEmpty()) {
                  AiFarm.Inner_CachePool w = this.takeNearestWork(this.matureWork);
                  if (!this.skippedCells.contains(w.cell().asLong()) && this.stillValid(w)) {
                     return w;
                  }
               }

               if (this.useBonemeal && !enough) {
                  while (!this.growingWork.isEmpty()) {
                     AiFarm.Inner_CachePool w = this.takeNearestWork(this.growingWork);
                     if (!this.skippedCells.contains(w.cell().asLong()) && this.stillValid(w)) {
                        return w;
                     }
                  }
               }

               while (!this.deferredDrops.isEmpty()) {
                  AiFarm.Inner_CachePool w = this.deferredDrops.poll();
                  if (this.stillValid(w)) {
                     return w;
                  }

                  this.countDrop(w);
               }

               return null;
            }

            AiFarm.Inner_CachePool takeNearestWork(List<AiFarm.Inner_CachePool> works) {
               int best = -1;
               double bestSqr = 1.7976931348623157E308;

               for (int i = 0; i < works.size(); i++) {
                  double d = this.mc.player.distanceToSqr(Vec3.atCenterOf(works.get(i).cell()));
                  if (d < bestSqr) {
                     bestSqr = d;
                     best = i;
                  }
               }

               return works.remove(best);
            }

            AiOperation.Result startWork(AiFarm.Inner_CachePool w) {
               BlockPos cell = w.kind() == AiFarm.Inner_BufferHelper.REPLANT ? w.base() : w.cell();
               if (w.kind() == AiFarm.Inner_BufferHelper.DROP) {
                  Entity entity = this.mc.level.getEntity(w.entityId());
                  if (!(entity instanceof ItemEntity)) {
                     this.countDrop(w);
                     return this.finishWork();
                  }

                  if (this.mc.player.distanceToSqr(entity) <= 2.25) {
                     if (++this.dropPickupWaitTicks > 40) {
                        this.countDrop(w);
                        return this.finishWork();
                     } else {
                        return this.running("Waiting for drop pickup; check inventory space");
                     }
                  } else {
                     this.dropPickupWaitTicks = 0;
                     return this.moveToStance(AiGoto.findStandableNearCompat(BlockPos.containing(entity.position().add(0.0, 0.2, 0.0)), 24), 0.7, 45, "Sweeping up the drops");
                  }
               } else if (this.isWithinReach(cell) && this.canSeeWorkFace(w)) {
                  switch (w.kind()) {
                     case HARVEST:
                        this.harvestRevision.put(w.cell().asLong(), AiServerEvidence.blockRevision(w.cell()));
                        if (this.replantAfter && kind == AiFarm.Inner_StreamUtil.BREAK_REPLANT && seed != null) {
                           BlockState before = this.mc.level.getBlockState(w.cell());
                           if (crop == Blocks.COCOA) {
                              Direction facing = AiFarm.facingOf(before);
                              this.replantBase = w.cell().relative(facing);
                              this.replantFace = facing.getOpposite();
                           } else {
                              this.replantBase = w.cell().below();
                              this.replantFace = Direction.UP;
                           }
                        }

                        if (kind == AiFarm.Inner_StreamUtil.INTERACT) {
                           this.action = AiGameOperations.blockAction("use", this.posJson(w.cell()));
                           return this.running("Picking " + w.cell().toShortString());
                        } else {
                           this.action = AiGameOperations.blockAction("break", this.posJson(w.cell()));
                           AiOperation.Result first = this.action.tick();
                           if (!first.terminal()) {
                              return this.running("Breaking " + w.cell().toShortString());
                           }

                           this.action.cancel();
                           this.action = null;
                           return this.settleWork(this.currentWork, first);
                        }
                     case REPLANT:
                        int slot = AiItems.equipHotbar(AiItems.resolveItem(seed));
                        if (slot < 0) {
                           if (AiGameState.countInventoryItem(seed) <= 0) {
                              this.followUps.addLast(w);
                              this.currentWork = null;
                              this.seedHandWaitTicks = 0;
                              return this.running("Picking up seed for the unfinished replant");
                           } else {
                              if (++this.seedHandWaitTicks > 80) {
                                 return this.finish(
                                    AiOperation.Status.FAILED, "The seed hand did not settle; the replant remains unfinished at " + w.cell().toShortString()
                                 );
                              }

                              return this.running("Preparing the seed hand");
                           }
                        } else {
                           this.seedHandWaitTicks = 0;
                           JsonObject placeArgs = this.posJson(w.base());
                           placeArgs.addProperty("face", w.face().name());
                           placeArgs.addProperty("slot", slot);
                           this.action = AiGameOperations.blockAction("place", placeArgs);
                           AiOperation.Result opened = this.action.tick();
                           if (!opened.terminal()) {
                              return this.running("Replanting " + w.cell().toShortString());
                           }

                           this.action.cancel();
                           this.action = null;
                           return this.settleWork(this.currentWork, opened);
                        }
                     case BONEMEAL:
                        int mealSlot = AiItems.equipHotbar(AiItems.resolveItem("minecraft:bone_meal"));
                        if (mealSlot < 0) {
                           return this.finishWork();
                        }

                        JsonObject useArgs = this.posJson(w.cell());
                        useArgs.addProperty("slot", mealSlot);
                        useArgs.addProperty("aimY", this.aimHeight(w.cell()));
                        this.action = AiGameOperations.blockAction("use", useArgs);
                        return this.running("Feeding " + w.cell().toShortString());
                     default:
                        return this.finishWork();
                  }
               } else {
                  if (w.kind() == AiFarm.Inner_BufferHelper.REPLANT) {
                     return this.moveToStance(AiGoto.findStandableNearCompat(w.cell(), 24), 0.45, 30, "Lining up the replant face");
                  }

                  int[] column = AiFarm.COLUMN_OFFSETS[this.columnOffsetCursor++ % AiFarm.COLUMN_OFFSETS.length];
                  BlockPos stance = AiGoto.findStandableNearCompat(new BlockPos(cell.getX() + column[0], cell.getY() - 1, cell.getZ() + column[1]), 24);
                  return this.moveToStance(stance, 2.0, 60, "Moving into reach of " + cell.toShortString());
               }
            }

            AiOperation.Result moveToStance(BlockPos stance, double range, int timeoutSeconds, String message) {
               if (this.walkTarget == null && this.hasStraightWalk(this.mc.player.blockPosition(), stance)) {
                  this.walkTarget = stance;
                  this.walkRange = range;
                  this.walkStallTicks = 0;
                  this.lastWalkDistSqr = -1.0;
                  return this.running(message);
               } else {
                  this.approach = AiGoto.travelToCompat(stance, range, false, false, false, timeoutSeconds);
                  return this.running(message);
               }
            }

            AiOperation.Result walkStep() {
               Vec3 center = Vec3.atBottomCenterOf(this.walkTarget);
               double distSqr = this.mc.player.distanceToSqr(center);
               if (distSqr <= Math.max(0.16, this.walkRange * this.walkRange)) {
                  this.stopWalking();
                  return this.running("Arrived; re-evaluating");
               }

               BlockPos feet = this.mc.player.blockPosition();
               int sx = Integer.signum(this.walkTarget.getX() - feet.getX());
               int sz = Integer.signum(this.walkTarget.getZ() - feet.getZ());
               boolean blocked = (sx != 0 || sz != 0) && !AiGoto.isStandableCompat(feet.offset(sx, 0, sz));
               if (this.lastWalkDistSqr >= 0.0 && distSqr > this.lastWalkDistSqr - 0.004) {
                  if (++this.walkStallTicks > 40) {
                     blocked = true;
                  }
               } else {
                  this.walkStallTicks = 0;
               }

               if (blocked) {
                  this.stopWalking();
                  return this.running("Direct walk blocked; pathing around");
               } else {
                  this.lastWalkDistSqr = distSqr;
                  this.aim.aimAt(center);
                  this.aim.tick();
                  this.mc.options.keyUp.setDown(true);
                  this.mc.player.setSprinting(distSqr > 9.0);
                  return this.running("Walking straight to the next plant");
               }
            }

            void stopWalking() {
               this.walkTarget = null;
               this.mc.options.keyUp.setDown(false);
               if (this.mc.player != null) {
                  this.mc.player.setSprinting(false);
               }
            }

            boolean hasStraightWalk(BlockPos from, BlockPos stance) {
               if (from.getY() != stance.getY()) {
                  return false;
               }

               int dx = stance.getX() - from.getX();
               int dz = stance.getZ() - from.getZ();
               int ax = Math.abs(dx);
               int az = Math.abs(dz);
               if (Math.max(ax, az) <= 8 && (ax == 0 || az == 0 || ax == az)) {
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

            AiOperation.Result settleWork(AiFarm.Inner_CachePool w, AiOperation.Result r) {
               if (w == null) {
                  return this.running("Re-evaluating the field");
               }

               switch (w.kind()) {
                  case HARVEST:
                     if (r.status() == AiOperation.Status.SUCCEEDED
                        || r.status() == AiOperation.Status.TIMED_OUT && !this.mc.level.getBlockState(w.cell()).is(crop)) {
                        this.harvestedCount++;
                        this.queueDropsNear(w.cell());
                        if (kind == AiFarm.Inner_StreamUtil.BREAK_REPLANT && seed != null && this.replantBase != null) {
                           ClientPacketListener connection = this.mc.getConnection();
                           PlayerInfo info = connection == null ? null : connection.getPlayerInfo(this.mc.player.getUUID());
                           this.replantNotBefore
                              .put(w.cell().asLong(), this.mc.player.tickCount + autismclient.util.BlockBreakGraceCalc.init(info == null ? 0 : info.getLatency()));
                           this.followUps.addFirst(AiFarm.Inner_CachePool.replant(w.cell(), this.replantBase, this.replantFace));
                        }

                        this.replantBase = null;
                        this.replantFace = null;
                        return this.finishWork();
                     } else {
                        this.replantBase = null;
                        this.replantFace = null;
                        return this.workAttemptFailed(r.message());
                     }
                  case REPLANT:
                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.replantedCount++;
                        return this.finishWork();
                     }

                     return this.replantFailed(r.message());
                  case BONEMEAL:
                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.bonemealCount++;
                        return this.finishWork();
                     }

                     return this.workAttemptFailed(r.message());
                  default:
                     return this.finishWork();
               }
            }

            AiOperation.Result finishWork() {
               this.currentWork = null;
               this.workAttempts = 0;
               this.columnOffsetCursor = 0;
               this.dropPickupWaitTicks = 0;
               this.seedHandWaitTicks = 0;
               return this.running("Harvested " + this.harvestedCount + ", " + cropItem + " " + this.lastSeenCount + "/" + this.targetCount);
            }

            AiOperation.Result replantFailed(String reason) {
               this.replantSkippedCount++;
               if (this.currentWork != null) {
                  this.recordSkip(this.currentWork.cell(), reason);
               }

               return this.finishWork();
            }

            AiOperation.Result workAttemptFailed(String reason) {
               if (this.currentWork == null) {
                  return this.running("Re-evaluating the field");
               }

               if (++this.workAttempts >= 3) {
                  if (this.currentWork.kind() == AiFarm.Inner_BufferHelper.REPLANT) {
                     this.replantSkippedCount++;
                  }

                  this.recordSkip(this.currentWork.cell(), reason);
                  this.skippedCells.add(this.currentWork.cell().asLong());
                  return this.finishWork();
               } else {
                  return this.running("Attempt " + this.workAttempts + " failed: " + reason);
               }
            }

            void recordSkip(BlockPos cell, String reason) {
               if (this.skipLog.size() < 32) {
                  this.skipLog.add(AiJson.objectOf("x", cell.getX(), "y", cell.getY(), "z", cell.getZ(), "reason", reason));
               }

               this.skippedTotal++;
            }

            void queueDropsNear(BlockPos cell) {
               Vec3 center = Vec3.atCenterOf(cell);
               List<AiFarm.Inner_CachePool> found = new ArrayList<>();

               for (Entity entity : this.mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item && item.isAlive() && !(item.position().distanceToSqr(center) > 64.0)) {
                     if (found.size() >= 16) {
                        break;
                     }

                     String id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
                     if (id.equals(cropItem) || seed != null && id.equals(seed)) {
                        this.dropBaseline.putIfAbsent(id, AiGameState.countInventoryItem(id));
                        found.add(AiFarm.Inner_CachePool.drop(item.getId(), id, item.blockPosition(), this.mc.player.distanceToSqr(item)));
                     }
                  }
               }

               found.sort(Comparator.comparingDouble(AiFarm.Inner_CachePool::distanceSqr));

               for (AiFarm.Inner_CachePool drop : found) {
                  this.followUps.addLast(drop);
               }
            }

            void countDrop(AiFarm.Inner_CachePool w) {
               int now = AiGameState.countInventoryItem(w.item());
               int base = this.dropBaseline.getOrDefault(w.item(), now);
               if (now > base) {
                  this.dropCount += now - base;
               }

               this.dropBaseline.put(w.item(), now);
            }

            JsonObject posJson(BlockPos p) {
               return AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ());
            }

            boolean isWithinReach(BlockPos pos) {
               return this.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) <= 25.0
                  && autismclient.util.InteractionTargetRef.isInReach(this.mc, pos);
            }

            boolean hasLineOfSight(BlockPos pos) {
               return this.hasLineOfSightAt(pos, 0.5);
            }

            boolean canSeeWorkFace(AiFarm.Inner_CachePool w) {
               BlockPos cell = w.kind() == AiFarm.Inner_BufferHelper.REPLANT ? w.base() : w.cell();
               if (w.kind() == AiFarm.Inner_BufferHelper.REPLANT) {
                  return autismclient.ai.engine.AiBlockInteraction.findFaceHit(this.mc, w.base(), w.face()) != null;
               } else {
                  return w.kind() == AiFarm.Inner_BufferHelper.BONEMEAL ? this.hasLineOfSightAt(cell, this.aimHeight(cell)) : this.hasLineOfSight(cell);
               }
            }

            boolean hasLineOfSightAt(BlockPos pos, double aimY) {
               BlockHitResult hit = this.mc
                  .level
                  .clip(
                     new ClipContext(
                        this.mc.player.getEyePosition(),
                        new Vec3(pos.getX() + 0.5, pos.getY() + aimY, pos.getZ() + 0.5),
                        net.minecraft.world.level.ClipContext.Block.OUTLINE,
                        Fluid.NONE,
                        this.mc.player
                     )
                  );
               return hit.getType() != Type.MISS && hit.getBlockPos().equals(pos);
            }

            double aimHeight(BlockPos pos) {
               VoxelShape shape = this.mc.level.getBlockState(pos).getShape(this.mc.level, pos);
               double h = shape.isEmpty() ? 1.0 : shape.max(Axis.Y);
               return Math.max(0.05, Math.min(0.95, h / 2.0));
            }

            String activityText() {
               if (this.currentWork == null) {
                  return "Working the field";
               }

               String var10000;
               switch (this.currentWork.kind()) {
                  case HARVEST:
                     var10000 = (kind == AiFarm.Inner_StreamUtil.INTERACT ? "Picking " : "Breaking ") + this.currentWork.cell().toShortString();
                     break;
                  case REPLANT:
                     var10000 = "Replanting " + this.currentWork.cell().toShortString();
                     break;
                  case BONEMEAL:
                     var10000 = "Feeding " + this.currentWork.cell().toShortString();
                     break;
                  case DROP:
                     var10000 = "Sweeping up the drops";
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               return var10000;
            }

            JsonObject progressJson() {
               JsonArray skipList = new JsonArray();

               for (JsonObject entry : this.skipLog) {
                  skipList.add(entry);
               }

               return AiJson.objectOf(
                  "crop",
                  cropBlock,
                  "count",
                  AiGameState.countInventoryItem(cropItem),
                  "target",
                  this.targetCount,
                  "harvested",
                  this.harvestedCount,
                  "replanted",
                  this.replantedCount,
                  "externalReplants",
                  this.externalReplantCount,
                  "replantSkipped",
                  this.replantSkippedCount,
                  "drops",
                  this.dropCount,
                  "bonemeal",
                  this.bonemealCount,
                  "skipped",
                  skipList,
                  "skippedTotal",
                  this.skippedTotal,
                  "queued",
                  this.matureWork.size() + this.followUps.size(),
                  "confirmingReplants",
                  this.pendingReplantOps.size(),
                  "activeWork",
                  this.currentWork == null ? "" : this.currentWork.kind().name()
               );
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(status, message, this.progressJson());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  if (this.approach != null) {
                     this.approach.cancel();
                     this.approach = null;
                  }

                  if (this.action != null) {
                     this.action.cancel();
                     this.action = null;
                  }

                  for (AiOperation op : this.pendingReplantOps) {
                     op.cancel();
                  }

                  this.pendingReplantOps.clear();
                  this.pendingReplantWork.clear();
                  this.walkTarget = null;
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  if (this.mc.player != null && this.mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(this.mc, this.originalSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Farming requires a connected living player");
      }
   }

   static List<String> breedingFoodFor(String entityTypeId) {
      return BREEDING_FOOD.getOrDefault(entityTypeId, List.of());
   }

   private static AiOperation breed(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      final String species = AiJson.getString(args, "animal", "").trim().toLowerCase(Locale.ROOT);
      if (species.isEmpty()) {
         throw new IllegalArgumentException("breed needs animal, the species to feed");
      }

      final String typeId = species.contains(":") ? species : "minecraft:" + species;
      final List<String> foods = breedingFoodFor(typeId);
      if (foods.isEmpty()) {
         throw new IllegalArgumentException("No known breeding food for '" + species + "'; breedable species: " + String.join(", ", BREEDING_FOOD.keySet()));
      }

      final int feeds = Math.max(2, Math.min(64, AiJson.getInt(args, "count", 2)));
      final int timeoutSeconds = Math.max(10, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 180)));
      return new AiOperation() {
         final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final Set<Integer> fedIds = new LinkedHashSet<>();
         final Set<Integer> skippedIds = new LinkedHashSet<>();
         AiOperation approach;
         Entity candidate;
         Entity lastFed;
         int confirmTicks;
         int turnTicks;
         int feedCooldownTicks;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadline) {
                  return this.finish(
                     this.fedIds.size() >= 2 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.TIMED_OUT,
                     "Breeding deadline reached after " + this.fedIds.size() + " feed(s)"
                  );
               }

               if (this.fedIds.size() >= feeds) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Fed " + this.fedIds.size() + " " + species);
               }

               if (this.lastFed != null) {
                  if (this.lastFed instanceof Animal animal && animal.isInLove()) {
                     this.fedIds.add(this.lastFed.getId());
                     this.lastFed = null;
                     this.confirmTicks = 0;
                     return this.running("Fed " + this.fedIds.size() + "/" + feeds);
                  } else if (++this.confirmTicks > 10) {
                     this.skippedIds.add(this.lastFed.getId());
                     this.lastFed = null;
                     this.confirmTicks = 0;
                     return this.running("No love mode from that " + species + " (healing, growing or already fed); skipping it");
                  } else {
                     return this.running("Confirming the feed");
                  }
               } else {
                  String food = this.heldFood();
                  if (food == null) {
                     return this.finish(
                        this.fedIds.size() >= 2 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                        this.fedIds.size() >= 2
                           ? "Ran out of breeding food after " + this.fedIds.size() + " feed(s)"
                           : "No breeding food for " + species + " in inventory (needs " + String.join(" or ", foods) + ")"
                     );
                  }

                  if (this.feedCooldownTicks > 0) {
                     this.feedCooldownTicks--;
                     return this.running("Waiting between feeds");
                  }

                  if (this.candidate == null
                     || !this.candidate.isAlive()
                     || this.fedIds.contains(this.candidate.getId())
                     || this.skippedIds.contains(this.candidate.getId())) {
                     this.candidate = this.nearestUnfed(typeId);
                     this.turnTicks = 0;
                     if (this.candidate == null) {
                        if (this.fedIds.size() >= 2) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "No more unfed " + species + " nearby after " + this.fedIds.size() + " feed(s)");
                        }

                        String untamed = this.untamedHint(typeId);
                        if (untamed != null) {
                           return this.finish(AiOperation.Status.FAILED, untamed);
                        }

                        return this.finish(AiOperation.Status.FAILED, "No breedable " + species + " within 16 blocks");
                     }
                  }

                  double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
                  if (mc.player.distanceTo(this.candidate) > reach * 0.9) {
                     if (this.approach == null) {
                        this.approach = AiGoto.travelToCompat(this.candidate.blockPosition(), 1.8, false, false, true, 20);
                     }

                     AiOperation.Result legResult = this.approach.tick();
                     if (!legResult.terminal()) {
                        return this.running("Walking to a " + species);
                     }

                     this.approach.cancel();
                     this.approach = null;
                     if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                        this.skippedIds.add(this.candidate.getId());
                        this.candidate = null;
                     }

                     return this.running("Re-evaluating the pen");
                  } else {
                     if (this.approach != null) {
                        this.approach.cancel();
                        this.approach = null;
                     }

                     this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.candidate));
                     this.aim.tick();
                     if (!autismclient.ai.engine.AiAim.isLookingAtEntity(this.candidate)) {
                        if (++this.turnTicks > 40) {
                           this.skippedIds.add(this.candidate.getId());
                           this.candidate = null;
                        }

                        return this.running("Turning toward a " + species);
                     } else {
                        if (AiItems.equipHotbar(AiItems.resolveItem(food)) < 0) {
                           return this.finish(AiOperation.Status.FAILED, "Could not hold " + food);
                        }

                        mc.gameMode
                           .interact(
                              mc.player,
                              this.candidate,
                              new EntityHitResult(this.candidate, autismclient.ai.engine.AiAim.defaultEntityAimPoint(this.candidate)),
                              InteractionHand.MAIN_HAND
                           );
                        Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                        this.lastFed = this.candidate;
                        this.confirmTicks = 0;
                        this.candidate = null;
                        this.feedCooldownTicks = 10;
                        return this.running("Feeding a " + species);
                     }
                  }
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Breeding cancelled");
            }
         }

         String untamedHint(String wanted) {
            if (!wanted.equals("minecraft:wolf") && !wanted.equals("minecraft:cat")) {
               return null;
            }

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof TamableAnimal tamable
                  && !tamable.isTame()
                  && entity.isAlive()
                  && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(wanted)
                  && entity.distanceToSqr(mc.player) <= 256.0) {
                  return "Nearby "
                     + wanted.substring(wanted.indexOf(58) + 1)
                     + " are not tamed; tame them first (wolves with bones, cats with raw fish) - food only heals an untamed one";
               }
            }

            return null;
         }

         String heldFood() {
            for (String food : foods) {
               if (AiGameState.countInventoryItem(food) > 0) {
                  return food;
               }
            }

            return null;
         }

         Entity nearestUnfed(String wanted) {
            Entity best = null;
            double bestDistance = 256.0;

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Animal animal
                  && animal.isAlive()
                  && !animal.isBaby()
                  && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(wanted)
                  && !this.fedIds.contains(entity.getId())
                  && !this.skippedIds.contains(entity.getId())) {
                  double distance = entity.distanceToSqr(mc.player);
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = entity;
                  }
               }
            }

            return best;
         }

         JsonObject progressJson() {
            return AiJson.objectOf(
               "action",
               "breed",
               "animal",
               typeId,
               "fed",
               this.fedIds.size(),
               "requested",
               feeds,
               "skipped",
               this.skippedIds.size(),
               "food",
               AiJson.GSON.toJsonTree(foods)
            );
         }

         AiOperation.Result running(String message) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            JsonObject data = this.progressJson();
            this.cancel();
            return new AiOperation.Result(status, message, data.toString(), List.of());
         }

         @Override
         public void cancel() {
            if (!this.cancelled) {
               this.cancelled = true;
               this.aim.clear();
               if (this.approach != null) {
                  this.approach.cancel();
                  this.approach = null;
               }
            }
         }
      };
   }

   static List<BlockPos> findNearestField(Minecraft mc, Block crop) {
      BlockPos origin = mc.player.blockPosition();
      BlockPos seed = null;
      double best = 1.7976931348623157E308;
      MutableBlockPos p = new MutableBlockPos();

      for (int dx = -32; dx <= 32; dx++) {
         for (int dz = -32; dz <= 32; dz++) {
            for (int dy = -8; dy <= 8; dy++) {
               p.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
               if (AiTerrain.isLoaded(mc.level, p)) {
                  BlockState state = mc.level.getBlockState(p);
                  if (state.is(crop) && isRipe(state)) {
                     double sq = p.distSqr(origin);
                     if (sq < best) {
                        best = sq;
                        seed = p.immutable();
                     }
                  }
               }
            }
         }
      }

      if (seed == null) {
         return List.of();
      }

      List<BlockPos> field = new ArrayList<>();
      Set<Long> seen = new HashSet<>();
      ArrayDeque<BlockPos> queue = new ArrayDeque<>(List.of(seed.below()));
      seen.add(seed.below().asLong());

      while (!queue.isEmpty() && field.size() < 512) {
         BlockPos ground = queue.poll();
         field.add(ground);

         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos next = ground.offset(dx, 0, dz);
               if (seen.add(next.asLong())
                  && AiTerrain.isLoaded(mc.level, next)
                  && autismclient.util.FarmCropCatalog.canGrowOn(crop, mc.level.getBlockState(next))
                  && (mc.level.getBlockState(next.above()).isAir() || mc.level.getBlockState(next.above()).is(crop))) {
                  queue.add(next);
               }
            }
         }
      }

      return field;
   }

   private static AiOperation harvestField(final JsonObject args, final String cropBlock, final String cropItem) {
      final Minecraft mc = Minecraft.getInstance();
      Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
      int baseline = AiGameState.countInventoryItem(cropItem);
      final int target = AiJson.getInt(args, "count", 53) + (AiJson.getBoolean(args, "additional", false) ? baseline : 0);
      final List<BlockPos> field = findNearestField(mc, crop);
      if (!field.isEmpty() && !AiJson.getBoolean(args, "bonemeal", false)) {
         final JsonObject rest = args.deepCopy();
         rest.addProperty("count", target);
         rest.addProperty("additional", false);
         return new AiOperation() {
            AiOperation fieldOp = AiFieldWork.init(field, cropBlock, AiFarm.load(cropBlock), cropItem, target, AiJson.getBoolean(args, "replant", true));
            boolean fallbackStarted;
            boolean dropSweepStarted;

            @Override
            public AiOperation.Result tick() {
               AiOperation.Result r = this.fieldOp.tick();
               if (!r.terminal()) {
                  return r;
               }

               if (!this.dropSweepStarted && r.status() == AiOperation.Status.SUCCEEDED && AiGameState.countInventoryItem(cropItem) >= target && this.hasFieldDrops()) {
                  this.fieldOp.cancel();
                  this.dropSweepStarted = true;
                  this.fieldOp = AiFieldWork.init(field, cropBlock, AiFarm.load(cropBlock), cropItem, AiGameState.countInventoryItem(cropItem));
                  return AiGameOperations.result(AiOperation.Status.RUNNING, "Collecting the field's remaining drops", new JsonObject());
               }

               if (this.dropSweepStarted
                  || this.fallbackStarted
                  || r.status() == AiOperation.Status.CANCELLED
                  || AiGameState.countInventoryItem(cropItem) >= target
                  || r.status() == AiOperation.Status.FAILED && r.message().contains("Inventory full")) {
                  return r;
               }

               this.fieldOp.cancel();
               this.fallbackStarted = true;
               this.fieldOp = AiFarm.init(rest, cropBlock, cropItem);
               return AiGameOperations.result(AiOperation.Status.RUNNING, "Field done; looking for more " + cropItem, new JsonObject());
            }

            boolean hasFieldDrops() {
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

               int y = field.getFirst().getY() + 1;
               String seed = AiFarm.load(cropBlock);
               AABB box = new AABB(minX - 2, y - 2, minZ - 2, maxX + 3, y + 3, maxZ + 3);
               return !mc.level.getEntitiesOfClass(ItemEntity.class, box, item -> {
                  if (!item.isAlive()) {
                     return false;
                  }

                  String id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
                  return id.equals(cropItem) || id.equals(seed);
               }).isEmpty();
            }

            @Override
            public void cancel() {
               this.fieldOp.cancel();
            }
         };
      } else {
         return init(args, cropBlock, cropItem);
      }
   }

   private static AiFarm.Inner_DataUtil parseAction(String text) {
      String t = text.trim().toLowerCase(Locale.ROOT);
      if (Set.of("sow", "till").contains(t)) {
         return AiFarm.Inner_DataUtil.PLANT;
      }

      if (Set.of("create", "make", "start", "farm").contains(t)) {
         return AiFarm.Inner_DataUtil.BUILD;
      }

      try {
         return AiFarm.Inner_DataUtil.valueOf(t.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be harvest, breed, plant or build");
      }
   }

   static {
      YIELD_BY_CROP.put("minecraft:wheat", "minecraft:wheat");
      YIELD_BY_CROP.put("minecraft:carrots", "minecraft:carrot");
      YIELD_BY_CROP.put("minecraft:potatoes", "minecraft:potato");
      YIELD_BY_CROP.put("minecraft:beetroots", "minecraft:beetroot");
      YIELD_BY_CROP.put("minecraft:nether_wart", "minecraft:nether_wart");
      YIELD_BY_CROP.put("minecraft:melon", "minecraft:melon_slice");
      YIELD_BY_CROP.put("minecraft:pumpkin", "minecraft:pumpkin");
      YIELD_BY_CROP.put("minecraft:sweet_berry_bush", "minecraft:sweet_berries");
      YIELD_BY_CROP.put("minecraft:cocoa", "minecraft:cocoa_beans");
      YIELD_BY_CROP.put("minecraft:sugar_cane", "minecraft:sugar_cane");
      YIELD_BY_CROP.put("minecraft:cactus", "minecraft:cactus");
      YIELD_BY_CROP.put("minecraft:bamboo", "minecraft:bamboo");
      YIELD_BY_CROP.forEach((crop, yield) -> CROP_BY_YIELD.putIfAbsent(yield, crop));
   }

   private enum Inner_BufferHelper {
      HARVEST,
      REPLANT,
      BONEMEAL,
      DROP;
   }

   private record Inner_CachePool(AiFarm.Inner_BufferHelper kind, BlockPos cell, BlockPos base, Direction face, int entityId, String item, double distanceSqr) {
      static AiFarm.Inner_CachePool harvest(BlockPos cell, double distanceSqr) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.HARVEST, cell, null, null, -1, null, distanceSqr);
      }

      static AiFarm.Inner_CachePool replant(BlockPos cell, BlockPos base, Direction face) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.REPLANT, cell, base, face, -1, null, 0.0);
      }

      static AiFarm.Inner_CachePool bonemeal(BlockPos cell, double distanceSqr) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.BONEMEAL, cell, null, null, -1, null, distanceSqr);
      }

      static AiFarm.Inner_CachePool drop(int entityId, String item, BlockPos cell, double distanceSqr) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.DROP, cell, null, null, entityId, item, distanceSqr);
      }
   }

   enum Inner_DataUtil {
      HARVEST,
      BREED,
      PLANT,
      BUILD;
   }

   enum Inner_StreamUtil {
      BREAK_REPLANT,
      BREAK_ONLY,
      INTERACT,
      COLUMN;
   }
}
