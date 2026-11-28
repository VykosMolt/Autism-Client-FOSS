package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

final class AiFarmBuild {
   static final int init_I = 9;
   static final int load = 80;
   static final int read = 320;
   static final int write = 27;
   static final int update = 35;
   static final double init_D = 4.3;
   static final int sync = 20;
   static final int flush = 3;
   static final int close = 24;
   static final Set<String> selfPlantingCrops = Set.of("minecraft:carrot", "minecraft:potato");
   static final List<String> woodTypes = List.of(
      "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped"
   );

   private AiFarmBuild() {
   }

   static AiFarmBuild.FarmLayout layoutFor(int plots, int width, int length) {
      if (width > 0 && length > 0) {
         int w = Math.max(1, Math.min(27, width));
         int l = Math.max(1, Math.min(27, length));
         return new AiFarmBuild.FarmLayout(w, l, waterCenters(w, l));
      }

      int n = Math.max(1, Math.min(320, plots));
      if (n > 80) {
         int tiles = (n + 81 - 2) / 80;
         int w = tiles >= 4 ? 18 : 9;
         int l = tiles >= 4 ? 18 : tiles * 9;
         return new AiFarmBuild.FarmLayout(w, l, waterCenters(w, l));
      }

      int bestW = 9;
      int bestL = 9;
      double best = 1.7976931348623157E308;

      for (int w = 3; w <= 9; w++) {
         for (int l = w; l <= 9; l++) {
            if (w * l - 1 >= n) {
               double score = w * l + 2.0 * (l - w);
               if (score < best) {
                  best = score;
                  bestW = w;
                  bestL = l;
               }
            }
         }
      }

      return new AiFarmBuild.FarmLayout(bestW, bestL, waterCenters(bestW, bestL));
   }

   static List<int[]> waterCenters(int width, int length) {
      List<int[]> out = new ArrayList<>();

      for (int x0 = 0; x0 < width; x0 += 9) {
         for (int z0 = 0; z0 < length; z0 += 9) {
            int x1 = Math.min(width - 1, x0 + 9 - 1);
            int z1 = Math.min(length - 1, z0 + 9 - 1);
            out.add(new int[]{(x0 + x1) / 2, (z0 + z1) / 2});
         }
      }

      return List.copyOf(out);
   }

   static boolean allPlotsWatered(AiFarmBuild.FarmLayout layout) {
      for (int x = 0; x < layout.width(); x++) {
         for (int z = 0; z < layout.length(); z++) {
            boolean wet = false;

            for (int[] w : layout.water()) {
               wet |= Math.abs(w[0] - x) <= 4 && Math.abs(w[1] - z) <= 4;
            }

            if (!wet) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean isInfiniteWaterAt(Function<BlockPos, BlockState> at, BlockPos pos) {
      if (!isWaterSource(at.apply(pos))) {
         return false;
      }

      int sources = 0;

      for (Direction d : Plane.HORIZONTAL) {
         if (isWaterSource(at.apply(pos.relative(d)))) {
            sources++;
         }
      }

      BlockState below = at.apply(pos.below());
      return sources >= 2 && (isWaterSource(below) || !below.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty());
   }

   private static boolean isWaterSource(BlockState state) {
      return state.getFluidState().isSource() && state.getFluidState().getType().isSame(Fluids.WATER);
   }

   static double walkSeconds(double distance) {
      return distance / 4.3;
   }

   static boolean needsPool(String substrate, int sourceCount, int waterHeld, boolean infiniteSource) {
      return needsPoolForSite(substrate, sourceCount, waterHeld, infiniteSource, true);
   }

   static boolean needsPoolForSite(String substrate, int sourceCount, int waterHeld, boolean infiniteSource, boolean automaticSite) {
      return !AiFarmArea.isSoulSand(substrate) && !infiniteSource && (automaticSite || waterHeld < sourceCount);
   }

   static AiFarmBuild.ColumnScan scanColumn(Function<BlockPos, BlockState> at, int x, int z, int fromY, int toY) {
      boolean obstacle = false;
      MutableBlockPos p = new MutableBlockPos();

      for (int y = fromY; y >= toY; y--) {
         BlockState state = at.apply(p.set(x, y, z));
         if (!state.isAir()) {
            if (!state.getFluidState().isEmpty()) {
               boolean lava = !state.getFluidState().getType().isSame(Fluids.WATER);
               return new AiFarmBuild.ColumnScan(y, false, false, true, lava, obstacle);
            }

            if (!state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
               if (!state.is(Blocks.SNOW)
                  && !state.canBeReplaced()
                  && !state.is(BlockTags.SMALL_FLOWERS)
                  && !state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty()) {
                  boolean tillable = state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.DIRT_PATH) || state.is(Blocks.FARMLAND);
                  boolean natural = tillable
                     || state.is(BlockTags.DIRT)
                     || state.is(BlockTags.SAND)
                     || state.is(BlockTags.BASE_STONE_OVERWORLD)
                     || state.is(BlockTags.TERRACOTTA)
                     || state.is(Blocks.GRAVEL)
                     || state.is(Blocks.CLAY)
                     || state.is(Blocks.SNOW_BLOCK)
                     || state.is(Blocks.SANDSTONE)
                     || state.is(Blocks.RED_SANDSTONE)
                     || state.is(Blocks.MUD)
                     || state.is(Blocks.PACKED_ICE)
                     || state.is(Blocks.ICE)
                     || state.is(Blocks.SOUL_SAND);
                  return new AiFarmBuild.ColumnScan(y, natural && !state.hasBlockEntity(), tillable, false, false, obstacle);
               }
            } else {
               obstacle = true;
            }
         }
      }

      return AiFarmBuild.ColumnScan.NONE;
   }

   static AiFarmBuild.FarmSite planAt(
      AiFarmBuild.FarmLayout layout, BlockPos origin, Direction gate, boolean pool, BiFunction<Integer, Integer, AiFarmBuild.ColumnScan> columns
   ) {
      int[] tops = new int[layout.width() * layout.length()];
      int i = 0;

      for (int x = 0; x < layout.width(); x++) {
         for (int z = 0; z < layout.length(); z++) {
            AiFarmBuild.ColumnScan c = columns.apply(origin.getX() + x, origin.getZ() + z);
            if (c.top() == -2147483648 || c.lava() || !c.natural() && !c.fluid()) {
               return null;
            }

            tops[i++] = c.top();
         }
      }

      Arrays.sort(tops);
      int ground = tops[tops.length / 2];
      AiFarmBuild.FarmSite site = new AiFarmBuild.FarmSite(new BlockPos(origin.getX(), ground, origin.getZ()), layout, gate, pool, 0.0);
      double cost = 0.0;
      Set<Long> seen = new HashSet<>();
      List<BlockPos> cells = new ArrayList<>(site.interior());
      cells.addAll(site.ring());
      if (pool) {
         cells.addAll(site.poolWalls());
      }

      for (BlockPos cell : cells) {
         if (seen.add(BlockPos.asLong(cell.getX(), 0, cell.getZ()))) {
            AiFarmBuild.ColumnScan c = columns.apply(cell.getX(), cell.getZ());
            if (c.top() == -2147483648 || c.lava() || !c.natural() && !c.fluid() || Math.abs(c.top() - ground) > 3) {
               return null;
            }

            cost += 2 * Math.abs(c.top() - ground);
            if (c.obstacle()) {
               cost += 40.0;
            }

            if (c.fluid()) {
               cost += 12.0;
            }

            if (!c.tillable()) {
               cost += 3.0;
            }
         }
      }

      return new AiFarmBuild.FarmSite(site.origin(), layout, gate, pool, cost);
   }

   static AiFarmBuild.FarmSite planNear(AiFarmBuild.FarmLayout layout, BlockPos player, boolean pool, BiFunction<Integer, Integer, AiFarmBuild.ColumnScan> columns) {
      int w = layout.width();
      int l = layout.length();
      AiFarmBuild.FarmSite best = null;

      for (int ox = player.getX() - 20 - w; ox <= player.getX() + 20; ox++) {
         for (int oz = player.getZ() - 20 - l; oz <= player.getZ() + 20; oz++) {
            int[] tops = new int[w * l];
            int i = 0;
            boolean ok = true;

            for (int x = 0; x < w && ok; x++) {
               for (int z = 0; z < l && ok; z++) {
                  AiFarmBuild.ColumnScan c = columns.apply(ox + x, oz + z);
                  if (c.top() == -2147483648 || c.lava() || !c.natural() && !c.fluid()) {
                     ok = false;
                  } else {
                     tops[i++] = c.top();
                  }
               }
            }

            if (ok) {
               Arrays.sort(tops);
               int ground = tops[tops.length / 2];
               Direction side = gateFacing(ox, oz, w, l, player);
               AiFarmBuild.FarmSite site = new AiFarmBuild.FarmSite(new BlockPos(ox, ground, oz), layout, side, pool, 0.0);
               double cost = 0.0;
               Set<Long> seen = new HashSet<>();
               List<BlockPos> cells = new ArrayList<>(site.interior());
               cells.addAll(site.ring());
               if (pool) {
                  cells.addAll(site.poolWalls());
               }

               for (BlockPos cell : cells) {
                  if (seen.add(BlockPos.asLong(cell.getX(), 0, cell.getZ()))) {
                     AiFarmBuild.ColumnScan c = columns.apply(cell.getX(), cell.getZ());
                     if (c.top() == -2147483648 || c.lava() || !c.natural() && !c.fluid() || Math.abs(c.top() - ground) > 3) {
                        ok = false;
                        break;
                     }

                     cost += 2 * Math.abs(c.top() - ground);
                     if (c.obstacle()) {
                        cost += 40.0;
                     }

                     if (c.fluid()) {
                        cost += 12.0;
                     }

                     if (!c.tillable()) {
                        cost += 3.0;
                     }
                  }
               }

               if (ok) {
                  double cx = ox + w / 2.0;
                  double cz = oz + l / 2.0;
                  cost += 0.3 * Math.sqrt((cx - player.getX()) * (cx - player.getX()) + (cz - player.getZ()) * (cz - player.getZ()));
                  if (player.getX() >= ox - 1 && player.getX() <= ox + w && player.getZ() >= oz - 1 && player.getZ() <= oz + l) {
                     cost += 6.0;
                  }

                  if (best == null || cost < best.cost()) {
                     best = new AiFarmBuild.FarmSite(site.origin(), layout, side, pool, cost);
                  }
               }
            }
         }
      }

      return best;
   }

   static Direction gateFacing(int ox, int oz, int w, int l, BlockPos player) {
      double dx = player.getX() + 0.5 - (ox + w / 2.0);
      double dz = player.getZ() + 0.5 - (oz + l / 2.0);
      if (Math.abs(dx) * l > Math.abs(dz) * w) {
         return dx > 0.0 ? Direction.EAST : Direction.WEST;
      } else {
         return dz > 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   static String pickWood(Map<String, Integer> woodInBag, String nearestLog) {
      String best = null;
      int most = 0;

      for (Entry<String, Integer> e : woodInBag.entrySet()) {
         if (e.getValue() > most) {
            most = e.getValue();
            best = e.getKey();
         }
      }

      if (best != null) {
         return best;
      } else {
         return nearestLog != null ? nearestLog : "oak";
      }
   }

   static String woodTypeOf(String id) {
      String path = id.substring(id.indexOf(58) + 1).replace("stripped_", "");

      for (String s : woodTypes) {
         if (path.equals(s + "_planks")
            || path.equals(s + "_log")
            || path.equals(s + "_wood")
            || path.equals(s + "_fence")
            || path.equals(s + "_stem")
            || path.equals(s + "_hyphae")
            || path.equals(s + "_block")) {
            return s;
         }
      }

      return null;
   }

   static AiOperation create(JsonObject args, final String cropBlock, final String seedItem) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null) {
         throw new IllegalArgumentException("Farming requires a connected living player");
      }

      if (seedItem == null) {
         throw new IllegalArgumentException(cropBlock + " is not planted from a seed item; build a wheat, carrot, potato, beetroot or nether-wart farm");
      }

      final AiFarmArea.Area selectedArea = AiFarmArea.fromArgs(args);
      final String substrate = AiFarmArea.substrateFor(args, cropBlock);
      AiFarmBuild.FarmLayout sized = layoutFor(AiJson.getInt(args, "count", 80), AiJson.getInt(args, "width", 0), AiJson.getInt(args, "length", 0));
      final AiFarmBuild.FarmLayout layout = selectedArea != null
         ? selectedArea.layout(substrate)
         : (AiFarmArea.isSoulSand(substrate) ? new AiFarmBuild.FarmLayout(sized.width(), sized.length(), List.of()) : sized);
      if (selectedArea != null) {
         String issue = selectedArea.validate(substrate);
         if (!issue.isEmpty()) {
            throw new IllegalArgumentException(issue);
         }
      }

      if (layout.plots() < 1) {
         throw new IllegalArgumentException("The farm layout leaves no room for a crop beside its water");
      }

      final int timeoutSeconds = Math.max(60, Math.min(7200, AiJson.getInt(args, "timeoutSeconds", 3600)));
      return new AiOperation() {
         final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
         final JsonArray progressLog = new JsonArray();
         final String substrateId = substrate;
         final boolean soulSandFarm = AiFarmArea.isSoulSand(substrate);
         final AiFarmArea.Area chosenArea = selectedArea;
         final BlockPos siteOrigin = this.chosenArea == null ? autismclient.ai.engine.AiStance.standingPos(mc) : this.chosenArea.origin();
         boolean siteTravelStarted;
         boolean seedExploreStarted;
         AiFarmBuild.BuildPhase phase = AiFarmBuild.BuildPhase.SEEDS;
         AiOperation stepOp;
         AiFarmBuild.FarmSite farmPlan;
         String slabWood;
         String waterSourceLabel = "";
         BlockPos waterSourcePos;
         final List<BlockPos> clearCells = new ArrayList<>();
         int clearIndex;
         int centerIndex;
         int poolIndex;
         int settleTicks;
         int unusedTicks;
         int missingRefillRetries;
         int flush;
         int close;
         int extraHoeAttempts;
         long fieldConfirmAt;
         long centerConfirmAt;
         long poolFloorConfirmAt;
         AiFarmBuild.BuildPhase lastLoggedPhase;
         boolean refillPending;
         boolean update_Z;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadlineNanos) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Farm building ran out of time in " + this.phase);
               }

               if (this.stepOp != null) {
                  AiOperation.Result r = this.stepOp.tick();
                  if (!r.terminal()) {
                     return this.running(r.message());
                  } else {
                     this.stepOp.cancel();
                     this.stepOp = null;
                     if (this.refillPending) {
                        this.refillPending = false;
                        this.logProgress(this.phase + " refill: " + r.message());
                        return r.status() != AiOperation.Status.SUCCEEDED
                           ? this.finish(
                              r.status(), "Could not get the materials for the " + this.phase.name().toLowerCase(Locale.ROOT) + ": " + r.message()
                           )
                           : this.running("Materials refilled");
                     } else {
                        return this.onStepFinished(r);
                     }
                  }
               } else {
                  if (this.phase != this.lastLoggedPhase) {
                     this.lastLoggedPhase = this.phase;
                     this.close = 0;
                  }

                  if (this.close > 12) {
                     return this.finish(
                        AiOperation.Status.FAILED,
                        "Farm building is stuck in "
                           + this.phase
                           + " ("
                           + (this.progressLog.isEmpty() ? "no progress" : this.progressLog.get(this.progressLog.size() - 1).getAsString())
                           + ")"
                     );
                  }

                  AiOperation.Result var10000;
                  switch (this.phase) {
                     case SEEDS:
                        var10000 = this.tickSeedsPhase();
                        break;
                     case HOE:
                        var10000 = this.tickHoePhase();
                        break;
                     case SITE:
                        var10000 = this.tickSitePhase();
                        break;
                     case WOOD:
                        var10000 = this.tickWoodPhase();
                        break;
                     case DIRT:
                        var10000 = this.tickDirtPhase();
                        break;
                     case BUCKETS:
                        var10000 = this.tickBucketsPhase();
                        break;
                     case CLEAR:
                        var10000 = this.tickClearPhase();
                        break;
                     case FOUNDATION:
                        var10000 = this.tickFoundationPhase();
                        break;
                     case POOL_FILL:
                        var10000 = this.tickPoolFillPhase();
                        break;
                     case POOL_DIG:
                        var10000 = this.tickPoolDigPhase();
                        break;
                     case POOL_POUR:
                        var10000 = this.tickPoolPourPhase();
                        break;
                     case POOL_SETTLE:
                        var10000 = this.tickPoolSettlePhase();
                        break;
                     case CENTER_FILL:
                        var10000 = this.tickCenterFillPhase();
                        break;
                     case CENTER_DIG:
                        var10000 = this.tickCenterDigPhase();
                        break;
                     case CENTER_POUR:
                        var10000 = this.tickCenterPourPhase();
                        break;
                     case CENTER_COVER:
                        var10000 = this.tickCenterCoverPhase();
                        break;
                     case FIELD:
                        var10000 = this.tickFieldPhase();
                        break;
                     case VERIFY:
                        var10000 = this.tickVerifyPhase();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  AiOperation.Result next = var10000;
                  if (this.stepOp != null) {
                     this.close++;
                  }

                  return next;
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Farm building cancelled");
            }
         }

         AiOperation.Result onStepFinished(AiOperation.Result r) {
            boolean ok = r.status() == AiOperation.Status.SUCCEEDED;
            this.logProgress(this.phase + ": " + r.message());
            if (!ok) {
               JsonObject data = AiJson.parseObject(r.dataJson());
               if (data.has("failed")) {
                  this.logProgress(
                     this.phase
                        + " failed cells: "
                        + data.get("failed").toString().substring(0, Math.min(1500, data.get("failed").toString().length()))
                  );
               }

               this.logProgress(
                  this.phase
                     + " body at "
                     + String.format(Locale.ROOT, "%.2f %.2f %.2f", mc.player.getX(), mc.player.getY(), mc.player.getZ())
                     + " holding "
                     + BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem())
               );
            }

            switch (this.phase) {
               case SEEDS:
                  if (AiGameState.countInventoryItem(seedItem) <= 0) {
                     if (AiFarmBuild.selfPlantingCrops.contains(seedItem) && !this.seedExploreStarted) {
                        this.seedExploreStarted = true;
                        this.stepOp = new AiOperation() {
                           AiOperation stepOp = AiGameOperations.explore(AiJson.objectOf("maxDistance", 1500, "timeoutSeconds", 900), true);
                           boolean obtainStarted;

                           @Override
                           public AiOperation.Result tick() {
                              AiOperation.Result s = this.stepOp.tick();
                              if (s.terminal() && !this.obtainStarted) {
                                 this.stepOp.cancel();
                                 if (s.status() != AiOperation.Status.SUCCEEDED) {
                                    return s;
                                 }

                                 this.obtainStarted = true;
                                 this.stepOp = init(seedItem, Math.min(layout.plots(), 24), 600);
                                 return AiGameOperations.result(
                                    AiOperation.Status.RUNNING, "Village found; taking " + seedItem + " from its fields", new JsonObject()
                                 );
                              } else {
                                 return s;
                              }
                           }

                           @Override
                           public void cancel() {
                              this.stepOp.cancel();
                           }
                        };
                        return this.running("No " + seedItem + " nearby; looking for a village");
                     }

                     return this.finish(AiOperation.Status.FAILED, "No " + seedItem + " to plant: " + r.message());
                  }

                  this.phase = this.soulSandFarm ? AiFarmBuild.BuildPhase.SITE : AiFarmBuild.BuildPhase.HOE;
                  break;
               case HOE:
                  if (!AiFarmPlant.hasHoe(mc)) {
                     return this.finish(AiOperation.Status.FAILED, "No hoe to till with: " + r.message());
                  }

                  this.phase = AiFarmBuild.BuildPhase.SITE;
                  break;
               case SITE:
                  if (!ok) {
                     this.logProgress("walk home: " + r.message());
                  }
                  break;
               case WOOD:
                  if (!ok) {
                     return this.finish(r.status(), "Could not get the slabs or the hoe: " + r.message());
                  }

                  return this.running("Fence materials ready");
               case DIRT:
                  if (!ok && AiCraft.countItem(AiFarmArea.levelingBlock(this.substrateId)) < this.substrateNeeded()) {
                     return this.finish(r.status(), "Could not get " + AiFarmArea.levelingBlock(this.substrateId) + " to level the field: " + r.message());
                  }

                  this.phase = this.soulSandFarm ? AiFarmBuild.BuildPhase.FOUNDATION : AiFarmBuild.BuildPhase.BUCKETS;
                  break;
               case BUCKETS:
                  if (!ok) {
                     return this.finish(r.status(), "Could not get water for the field: " + r.message());
                  }

                  return this.running("Water ready");
               case CLEAR:
                  if (!ok) {
                     this.logProgress("left " + this.clearCells.get(this.clearIndex).toShortString() + ": " + r.message());
                  }

                  this.clearIndex++;
                  break;
               case FOUNDATION:
               case FIELD:
                  if (!ok) {
                     String missing = AiFarmBuild.missingMaterial(r.message());
                     if (missing == null && this.phase == AiFarmBuild.BuildPhase.FIELD && r.dataJson().contains("Missing hoe") && this.missingRefillRetries++ < 3) {
                        return this.running("The hoe wore out; making another");
                     }

                     if (missing != null && this.missingRefillRetries++ < 3) {
                        this.stepOp = this.init(missing, AiCraft.countItem(missing) + (missing.equals("minecraft:dirt") ? 16 : 1), 600);
                        this.refillPending = true;
                        this.update_Z = true;
                        return this.running("Getting more " + missing);
                     }

                     if (this.phase == AiFarmBuild.BuildPhase.FIELD && this.fieldSoilReady()) {
                        this.phase = AiFarmBuild.BuildPhase.VERIFY;
                        return this.running("Field done");
                     }

                     return this.finish(r.status(), (this.phase == AiFarmBuild.BuildPhase.FIELD ? "The field" : "The foundation") + " failed: " + r.message());
                  }

                  this.phase = this.phase == AiFarmBuild.BuildPhase.FOUNDATION
                     ? (this.soulSandFarm ? AiFarmBuild.BuildPhase.FIELD : (this.farmPlan.pool() ? AiFarmBuild.BuildPhase.POOL_FILL : AiFarmBuild.BuildPhase.CENTER_FILL))
                     : AiFarmBuild.BuildPhase.VERIFY;
                  break;
               case POOL_FILL:
                  if (!ok) {
                     return this.finish(r.status(), "Could not fill buckets for the pool: " + r.message());
                  }
                  break;
               case POOL_DIG:
                  if (!ok && !this.isOpenOrSource(this.farmPlan.poolCells().get(this.poolIndex))) {
                     return this.finish(r.status(), "Could not dig the pool: " + r.message());
                  }

                  this.poolIndex++;
                  break;
               case POOL_POUR:
                  if (!ok && !AiFarmBuild.isWaterSource(mc.level.getBlockState(this.farmPlan.poolPours().get(this.poolIndex)))) {
                     return this.finish(r.status(), "Could not pour the pool: " + r.message());
                  }

                  this.poolIndex++;
               case POOL_SETTLE:
               default:
                  break;
               case CENTER_FILL:
                  if (!ok) {
                     return this.finish(r.status(), "Could not fill a bucket for the field: " + r.message());
                  }
                  break;
               case CENTER_DIG:
                  if (!ok && !this.isOpenOrSource(this.farmPlan.waterCells().get(this.centerIndex))) {
                     return this.finish(r.status(), "Could not open the water hole: " + r.message());
                  }

                  this.phase = AiFarmBuild.BuildPhase.CENTER_POUR;
                  break;
               case CENTER_POUR:
                  if (!ok && !this.isWaterSourceAt(this.farmPlan.waterCells().get(this.centerIndex))) {
                     return this.finish(r.status(), "Could not pour the field's water: " + r.message());
                  }

                  this.phase = AiFarmBuild.BuildPhase.CENTER_COVER;
                  break;
               case CENTER_COVER:
                  if (!ok && !this.isWaterloggedSlabAt(this.farmPlan.waterCells().get(this.centerIndex))) {
                     this.logProgress("water left uncovered: " + r.message());
                  }

                  this.centerIndex++;
                  this.phase = AiFarmBuild.BuildPhase.CENTER_FILL;
            }

            return this.running(r.message());
         }

         AiOperation.Result tickSeedsPhase() {
            int held = AiGameState.countInventoryItem(seedItem);
            int want = Math.min(layout.plots(), 24);
            if (this.progressLog.isEmpty()) {
               this.logProgress("start: " + held + " " + seedItem + " in the bag, " + layout.plots() + " plots");
            }

            if (held >= want) {
               this.phase = this.soulSandFarm ? AiFarmBuild.BuildPhase.SITE : AiFarmBuild.BuildPhase.HOE;
               return this.running("Holding " + held + " " + seedItem);
            } else {
               this.stepOp = this.init(seedItem, want, held > 0 ? 180 : 600);
               return this.running("Getting " + seedItem + " to plant");
            }
         }

         AiOperation.Result tickHoePhase() {
            if (AiFarmPlant.hasHoe(mc)) {
               this.phase = AiFarmBuild.BuildPhase.SITE;
               return this.running("Hoe in the bag");
            } else {
               this.stepOp = this.init("minecraft:wooden_hoe", 1, 600);
               return this.running("Making a hoe");
            }
         }

         AiOperation.Result tickSitePhase() {
            if (!this.siteTravelStarted && Math.sqrt(mc.player.blockPosition().distSqr(this.siteOrigin)) > 32.0) {
               this.siteTravelStarted = true;
               this.stepOp = Math.sqrt(mc.player.blockPosition().distSqr(this.siteOrigin)) > 96.0
                  ? AiTravelFar.travel(this.siteOrigin, 3.0, true, true, true, true, 900)
                  : AiGoto.travelToCompat(this.siteOrigin, 3.0, true, true, true, 300);
               return this.running("Walking back to where the farm goes");
            }

            BlockPos feet = this.siteOrigin;
            this.waterSourcePos = this.soulSandFarm ? null : this.nearestWaterSource(feet);
            int waterHeld = this.soulSandFarm ? 0 : AiCraft.countItem("minecraft:water_bucket");
            boolean pool = AiFarmBuild.needsPoolForSite(this.substrateId, layout.water().size(), waterHeld, this.waterSourcePos != null, this.chosenArea == null);
            HashMap<Long, AiFarmBuild.ColumnScan> cache = new HashMap<>();
            int fromY = (this.chosenArea == null ? feet.getY() : this.chosenArea.maxY()) + 8;
            int toY = (this.chosenArea == null ? feet.getY() : this.chosenArea.minY()) - 12;
            BiFunction<Integer, Integer, AiFarmBuild.ColumnScan> columns = (x, z) -> cache.computeIfAbsent(
               BlockPos.asLong(x, 0, z),
               k -> AiTerrain.isLoaded(mc.level, new BlockPos(x, feet.getY(), z)) ? AiFarmBuild.scanColumn(mc.level::getBlockState, x, z, fromY, toY) : AiFarmBuild.ColumnScan.NONE
            );
            this.farmPlan = this.chosenArea == null
               ? AiFarmBuild.planNear(layout, feet, pool, columns)
               : AiFarmBuild.planAt(
                  layout,
                  this.chosenArea.origin(),
                  AiFarmBuild.gateFacing(this.chosenArea.minX(), this.chosenArea.minZ(), layout.width(), layout.length(), mc.player.blockPosition()),
                  pool,
                  columns
               );
            if (this.farmPlan == null) {
               return this.finish(
                  AiOperation.Status.FAILED,
                  this.chosenArea == null
                     ? "No open, level ground within 20 blocks for a "
                        + layout.width()
                        + "x"
                        + layout.length()
                        + " field (trees, lava, buildings or slopes steeper than 3 blocks)"
                     : "The selected " + layout.width() + "x" + layout.length() + " area is not usable; clear trees, lava, buildings and slopes over 3 blocks"
               );
            }

            this.waterSourceLabel = this.soulSandFarm
               ? "not required"
               : (pool ? "own infinite pool" : (waterHeld >= layout.water().size() ? "held buckets" : "infinite source nearby"));
            if (this.waterSourcePos != null && waterHeld < layout.water().size()) {
               double d = Math.sqrt(this.waterSourcePos.distSqr(this.farmPlan.origin()));
               this.logProgress("infinite water " + Math.round(d) + " blocks away, about " + Math.round(AiFarmBuild.walkSeconds(d)) + " s");
            }

            this.logProgress(
               "site "
                  + this.farmPlan.origin().toShortString()
                  + " "
                  + layout.width()
                  + "x"
                  + layout.length()
                  + ", gate "
                  + this.farmPlan.gateSide().getName()
                  + ", water: "
                  + this.waterSourceLabel
            );
            this.slabWood = this.soulSandFarm ? "oak" : AiFarmBuild.pickWood(this.woodInBag(), this.nearestLogWood());
            this.phase = this.soulSandFarm ? AiFarmBuild.BuildPhase.DIRT : AiFarmBuild.BuildPhase.WOOD;
            return this.running("Field site chosen at " + this.farmPlan.origin().toShortString());
         }

         AiOperation.Result tickWoodPhase() {
            if (this.slabCount() < layout.water().size()) {
               this.stepOp = this.init(
                  this.minecraftId(this.slabWood + "_slab"), AiCraft.countItem(this.minecraftId(this.slabWood + "_slab")) + layout.water().size() - this.slabCount(), 600
               );
               return this.running("Getting slabs to cover the water");
            } else if (this.hoeUsesLeft() < this.plotsToTill() && this.extraHoeAttempts++ < 4) {
               this.stepOp = this.init(this.hoeToMake(), AiCraft.countItem(this.hoeToMake()) + 1, 600);
               return this.running("Making another hoe: " + this.hoeUsesLeft() + " uses for " + this.plotsToTill() + " plots");
            } else {
               this.phase = AiFarmBuild.BuildPhase.DIRT;
               return this.running("Slabs and hoes in the bag");
            }
         }

         AiOperation.Result tickDirtPhase() {
            int need = this.substrateNeeded();
            String base = AiFarmArea.levelingBlock(this.substrateId);
            if (AiCraft.countItem(base) >= need) {
               this.phase = this.soulSandFarm ? AiFarmBuild.BuildPhase.FOUNDATION : AiFarmBuild.BuildPhase.BUCKETS;
               return this.running(base + " in the bag");
            } else if (this.soulSandFarm) {
               this.stepOp = this.init(base, AiCraft.countItem(base) + need, 900);
               return this.running("Getting soul sand for the field");
            } else {
               this.stepOp = autismclient.ai.engine.AiBlockMine.create(
                  AiJson.objectOf(
                     "blocks",
                     autismclient.ai.engine.AiBlockMine.jsonArray(List.of("minecraft:dirt", "minecraft:grass_block", "minecraft:coarse_dirt", "minecraft:rooted_dirt")),
                     "drops",
                     autismclient.ai.engine.AiBlockMine.jsonArray(List.of("minecraft:dirt")),
                     "held",
                     need,
                     "radius",
                     40,
                     "explore",
                     false,
                     "avoid",
                     this.farmPlan.box(),
                     "timeoutSeconds",
                     300
                  )
               );
               return this.running("Digging " + need + " dirt beside the site");
            }
         }

         AiOperation.Result tickBucketsPhase() {
            int need = layout.water().size();
            int water = AiCraft.countItem("minecraft:water_bucket");
            int empty = AiCraft.countItem("minecraft:bucket");
            if (!this.farmPlan.pool() && water >= need) {
               this.phase = AiFarmBuild.BuildPhase.CLEAR;
               return this.running("Water in the bag");
            } else {
               int buckets = this.farmPlan.pool() ? 2 : 1;
               if (water + empty < buckets) {
                  this.stepOp = this.init("minecraft:bucket", empty + buckets - water - empty, 1800);
                  return this.running("Making " + (buckets - water - empty) + " bucket(s)");
               } else {
                  this.phase = AiFarmBuild.BuildPhase.CLEAR;
                  return this.running("Buckets ready");
               }
            }
         }

         AiOperation.Result tickClearPhase() {
            if (this.clearCells.isEmpty() && this.clearIndex == 0) {
               this.collectClearCells();
            }

            while (this.clearIndex < this.clearCells.size()) {
               BlockPos p = this.clearCells.get(this.clearIndex);
               BlockState state = mc.level.getBlockState(p);
               if (!state.isAir() && (state.getFluidState().isEmpty() || !state.getCollisionShape(mc.level, p).isEmpty())) {
                  this.stepOp = autismclient.ai.engine.AiBreakBlock.breakBlock(p, 30, false, this.protectedFarmCells());
                  return this.running("Clearing " + BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath() + " at " + p.toShortString());
               }

               this.clearIndex++;
            }

            this.phase = AiFarmBuild.BuildPhase.FOUNDATION;
            return this.running("Site cleared");
         }

         void collectClearCells() {
            int g = this.farmPlan.ground();
            Set<Long> columns = new HashSet<>();
            List<BlockPos> cells = new ArrayList<>(this.farmPlan.interior());
            cells.addAll(this.farmPlan.ring());
            if (this.farmPlan.pool()) {
               cells.addAll(this.farmPlan.poolWalls());
               cells.addAll(this.farmPlan.poolCells());
            }

            for (BlockPos c : cells) {
               if (columns.add(BlockPos.asLong(c.getX(), 0, c.getZ()))) {
                  for (int y = g + 3 + 2; y >= g + 1; y--) {
                     BlockPos p = new BlockPos(c.getX(), y, c.getZ());
                     BlockState s = mc.level.getBlockState(p);
                     if (!s.isAir() && (y != g + 1 || !s.is(BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock))))) {
                        this.clearCells.add(p);
                     }
                  }
               }
            }

            BlockPos feet = mc.player.blockPosition();
            this.clearCells.sort(Comparator.<BlockPos>comparingInt(px -> -px.getY()).thenComparingDouble(px -> px.distSqr(feet)));
         }

         AiOperation.Result tickFoundationPhase() {
            List<AiBuilder.CellSpec> cells = this.foundationCells();
            if (cells.isEmpty()) {
               this.phase = this.soulSandFarm ? AiFarmBuild.BuildPhase.FIELD : (this.farmPlan.pool() ? AiFarmBuild.BuildPhase.POOL_FILL : AiFarmBuild.BuildPhase.CENTER_FILL);
               return this.running("Ground already level");
            } else {
               this.stepOp = AiBuilder.buildSchematic(new AiBuilder.Blueprint(BlockPos.ZERO, cells), 900);
               return this.running("Levelling " + cells.size() + " cells with " + AiFarmArea.levelingBlock(this.substrateId));
            }
         }

         List<AiBuilder.CellSpec> foundationCells() {
            int g = this.farmPlan.ground();
            Map<Long, AiBuilder.CellSpec> out = new LinkedHashMap<>();
            List<BlockPos> top = new ArrayList<>(this.farmPlan.interior());
            top.addAll(this.farmPlan.ring());

            for (BlockPos c : top) {
               if (!this.farmPlan.waterCells().contains(c)) {
                  for (int y = g - 3; y <= g; y++) {
                     this.addFoundationCell(out, new BlockPos(c.getX(), y, c.getZ()), y < g);
                  }
               }
            }

            for (BlockPos c : this.farmPlan.interior()) {
               if (!this.farmPlan.waterCells().contains(c) && !out.containsKey(c.asLong())) {
                  BlockState s = mc.level.getBlockState(c);
                  if (!s.getCollisionShape(mc.level, c).isEmpty() && s.getFluidState().isEmpty()) {
                     boolean correct = this.soulSandFarm
                        ? s.is(Blocks.SOUL_SAND)
                        : s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.DIRT_PATH) || s.is(Blocks.FARMLAND);
                     if (!correct && !s.hasBlockEntity()) {
                        out.put(c.asLong(), new AiBuilder.CellSpec(c.getX(), c.getY(), c.getZ(), AiFarmArea.levelingBlock(this.substrateId)));
                     }
                  }
               }
            }

            for (BlockPos w : this.farmPlan.waterCells()) {
               for (int y = g - 3; y <= g - 1; y++) {
                  this.addFoundationCell(out, new BlockPos(w.getX(), y, w.getZ()), true);
               }
            }

            if (this.farmPlan.pool()) {
               for (BlockPos wall : this.farmPlan.poolWalls()) {
                  for (int y = g - 3; y <= g; y++) {
                     this.addFoundationCell(out, new BlockPos(wall.getX(), y, wall.getZ()), y < g);
                  }
               }

               for (BlockPos cell : this.farmPlan.poolCells()) {
                  for (int y = g - 3; y <= g - 1; y++) {
                     this.addFoundationCell(out, new BlockPos(cell.getX(), y, cell.getZ()), true);
                  }
               }
            }

            return new ArrayList<>(out.values());
         }

         void addFoundationCell(Map<Long, AiBuilder.CellSpec> out, BlockPos p, boolean below) {
            BlockState state = mc.level.getBlockState(p);
            if ((!AiSchematicActions.keepsWaterOut(state) || state.getFluidState().isSource())
               && (state.getCollisionShape(mc.level, p).isEmpty() || !state.getFluidState().isEmpty())
               && (!below || this.isCoveredAbove(p))) {
               out.put(p.asLong(), new AiBuilder.CellSpec(p.getX(), p.getY(), p.getZ(), AiFarmArea.levelingBlock(this.substrateId)));
            }
         }

         boolean isCoveredAbove(BlockPos p) {
            for (int y = p.getY() + 1; y < this.farmPlan.ground(); y++) {
               BlockPos q = new BlockPos(p.getX(), y, p.getZ());
               BlockState s = mc.level.getBlockState(q);
               if (!s.getCollisionShape(mc.level, q).isEmpty() && s.getFluidState().isEmpty()) {
                  return false;
               }
            }

            return true;
         }

         int substrateNeeded() {
            if (this.farmPlan == null) {
               return 0;
            }

            int cells = this.foundationCells().size();
            if (this.soulSandFarm) {
               return cells;
            }

            for (BlockPos c : this.farmPlan.interior()) {
               if (!this.farmPlan.waterCells().contains(c)) {
                  BlockState s = mc.level.getBlockState(c);
                  if (!s.is(Blocks.GRASS_BLOCK)
                     && !s.is(Blocks.DIRT)
                     && !s.is(Blocks.DIRT_PATH)
                     && !s.is(Blocks.FARMLAND)
                     && !s.is(Blocks.COARSE_DIRT)
                     && !s.is(Blocks.ROOTED_DIRT)) {
                     cells++;
                  }
               }
            }

            return cells == 0 ? 0 : cells + 4;
         }

         AiOperation.Result tickPoolFillPhase() {
            int water = AiCraft.countItem("minecraft:water_bucket");
            if (water < 2 && !this.poolCellsAllSources()) {
               List<BlockPos> sources = AiTerrain.findFluidSources(mc, Blocks.WATER, 160, 64, true);
               if (sources.isEmpty()) {
                  return this.finish(AiOperation.Status.FAILED, "No water anywhere in loaded terrain to start the farm's pool; bring two water buckets");
               }

               this.stepOp = this.fetchWaterOp(sources);
               return this.running("Filling a bucket for the infinite pool");
            } else {
               this.phase = AiFarmBuild.BuildPhase.POOL_DIG;
               this.poolIndex = 0;
               return this.running("Two bucketfuls for the pool");
            }
         }

         AiOperation.Result tickPoolDigPhase() {
            for (List<BlockPos> cells = this.farmPlan.poolCells(); this.poolIndex < cells.size(); this.poolIndex++) {
               BlockPos p = cells.get(this.poolIndex);
               if (!this.isOpenOrSource(p)) {
                  this.stepOp = autismclient.ai.engine.AiBreakBlock.breakBlock(p, 30, false, this.init_Set(p));
                  return this.running("Digging the pool at " + p.toShortString());
               }
            }

            this.phase = AiFarmBuild.BuildPhase.POOL_POUR;
            this.poolIndex = 0;
            return this.running("Pool dug");
         }

         AiOperation.Result tickPoolPourPhase() {
            if (this.poolFloorConfirmAt == 0L) {
               this.poolFloorConfirmAt = System.nanoTime() + AiGameplay.pingMs(mc) * 50000000L;
               return this.running("Confirming the pool's floor with the server");
            }

            if (System.nanoTime() < this.poolFloorConfirmAt) {
               return this.running("Confirming the pool's floor with the server");
            }

            for (List<BlockPos> pours = this.farmPlan.poolPours(); this.poolIndex < pours.size(); this.poolIndex++) {
               BlockPos p = pours.get(this.poolIndex);
               if (!AiFarmBuild.isWaterSource(mc.level.getBlockState(p))) {
                  String leak = AiSchematicActions.spillReason(p, mc.level, this.poolShape());
                  if (leak != null) {
                     return this.finish(AiOperation.Status.FAILED, "The pool would spill: " + leak);
                  }

                  this.stepOp = this.pourAtOp(p);
                  return this.running("Pouring the pool at " + p.toShortString());
               }
            }

            this.phase = AiFarmBuild.BuildPhase.POOL_SETTLE;
            this.settleTicks = 0;
            return this.running("Pool poured");
         }

         AiOperation.Result tickPoolSettlePhase() {
            if (this.poolCellsAllSources()) {
               this.phase = AiFarmBuild.BuildPhase.CENTER_FILL;
               return this.running("The pool is an infinite source");
            } else {
               return ++this.settleTicks > 60
                  ? this.finish(AiOperation.Status.FAILED, "The pool did not become an infinite source")
                  : this.running("Waiting for the pool to fill");
            }
         }

         boolean poolCellsAllSources() {
            for (BlockPos p : this.farmPlan.poolCells()) {
               if (!AiFarmBuild.isWaterSource(mc.level.getBlockState(p))) {
                  return false;
               }
            }

            return true;
         }

         AiOperation.Result tickCenterFillPhase() {
            List<BlockPos> centers = this.farmPlan.waterCells();

            while (this.centerIndex < centers.size() && this.isWaterloggedSlabAt(centers.get(this.centerIndex))) {
               this.centerIndex++;
            }

            if (this.centerIndex >= centers.size()) {
               this.phase = AiFarmBuild.BuildPhase.FIELD;
               return this.running("Every source in place");
            }

            if (this.isWaterSourceAt(centers.get(this.centerIndex))) {
               this.phase = AiFarmBuild.BuildPhase.CENTER_COVER;
               return this.running("Covering the source");
            }

            if (AiCraft.countItem("minecraft:water_bucket") > 0) {
               this.phase = AiFarmBuild.BuildPhase.CENTER_DIG;
               return this.running("Water in hand");
            }

            List<BlockPos> sources = this.farmPlan.pool()
               ? this.farmPlan.poolCells()
               : (this.waterSourcePos != null ? List.of(this.waterSourcePos) : AiTerrain.findFluidSources(mc, Blocks.WATER, 160, 64, true));
            if (sources.isEmpty()) {
               return this.finish(AiOperation.Status.FAILED, "No water source to fill a bucket from");
            }

            this.stepOp = this.fetchWaterOp(sources);
            return this.running("Filling a bucket for the field");
         }

         AiOperation.Result tickCenterDigPhase() {
            BlockPos c = this.farmPlan.waterCells().get(this.centerIndex);
            if (this.isOpenOrSource(c)) {
               if (this.centerConfirmAt == 0L) {
                  this.centerConfirmAt = System.nanoTime() + AiGameplay.pingMs(mc) * 50000000L;
                  return this.running("Confirming the water hole with the server");
               }

               if (System.nanoTime() < this.centerConfirmAt) {
                  return this.running("Confirming the water hole with the server");
               }

               this.centerConfirmAt = 0L;
               this.phase = AiFarmBuild.BuildPhase.CENTER_POUR;
               return this.running("Water hole open");
            } else {
               this.stepOp = autismclient.ai.engine.AiBreakBlock.breakBlock(c, 30, false, this.init_Set(c));
               return this.running("Opening the water hole at " + c.toShortString());
            }
         }

         AiOperation.Result tickCenterPourPhase() {
            BlockPos c = this.farmPlan.waterCells().get(this.centerIndex);
            if (this.isWaterSourceAt(c)) {
               this.phase = AiFarmBuild.BuildPhase.CENTER_COVER;
               return this.running("Source in place");
            }

            Map<Long, AiBuilder.CellSpec> shape = new HashMap<>();

            for (Direction d : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
               shape.put(c.relative(d).asLong(), new AiBuilder.CellSpec(0, 0, 0, "minecraft:dirt"));
            }

            String leak = AiSchematicActions.spillReason(c, mc.level, shape);
            if (leak != null) {
               return this.finish(AiOperation.Status.FAILED, "The field's water would spill: " + leak);
            }

            this.stepOp = this.pourAtOp(c);
            return this.running("Pouring the field's water at " + c.toShortString());
         }

         AiOperation.Result tickCenterCoverPhase() {
            BlockPos c = this.farmPlan.waterCells().get(this.centerIndex);
            if (this.isWaterloggedSlabAt(c)) {
               this.centerIndex++;
               this.phase = AiFarmBuild.BuildPhase.CENTER_FILL;
               return this.running("Source covered");
            } else {
               String slab = this.firstSlabId();
               if (slab == null) {
                  this.logProgress("no slab to cover the water at " + c.toShortString());
                  this.centerIndex++;
                  this.phase = AiFarmBuild.BuildPhase.CENTER_FILL;
                  return this.running("No slab");
               } else {
                  this.stepOp = AiBuilder.buildSchematic(
                     new AiBuilder.Blueprint(c, List.of(new AiBuilder.CellSpec(0, 0, 0, slab, Map.of("type", "bottom", "waterlogged", "true")))),
                     60
                  );
                  return this.running("Covering the source with a slab");
               }
            }
         }

         AiOperation.Result tickFieldPhase() {
            int tills = this.plotsToTill();
            int uses = this.hoeUsesLeft();
            if (uses < tills && this.extraHoeAttempts++ < 4) {
               this.stepOp = this.init(this.hoeToMake(), AiCraft.countItem(this.hoeToMake()) + 1, 600);
               this.refillPending = true;
               this.update_Z = true;
               return this.running("Making another hoe: " + uses + " uses left for " + tills + " plots");
            } else if (this.update_Z) {
               this.update_Z = false;
               this.clearCells.clear();
               this.clearIndex = 0;
               this.phase = AiFarmBuild.BuildPhase.CLEAR;
               return this.running("Checking the site after the refill");
            } else {
               List<BlockPos> plots = new ArrayList<>(this.farmPlan.interior());
               plots.removeAll(this.farmPlan.waterCells());
               this.stepOp = AiFieldWork.plantPlots(plots, this.farmPlan.origin(), layout.width(), layout.length(), cropBlock, seedItem);
               return this.running("Tilling and sowing " + layout.plots() + " plots on the move");
            }
         }

         String hoeToMake() {
            return AiCraft.countItem("minecraft:cobblestone") + AiCraft.countItem("minecraft:cobbled_deepslate") >= 2 ? "minecraft:stone_hoe" : "minecraft:wooden_hoe";
         }

         int plotsToTill() {
            if (this.soulSandFarm) {
               return 0;
            }

            int n = 0;

            for (BlockPos p : this.farmPlan.interior()) {
               if (!this.farmPlan.waterCells().contains(p) && !mc.level.getBlockState(p).is(Blocks.FARMLAND)) {
                  n++;
               }
            }

            return n;
         }

         int hoeUsesLeft() {
            int uses = 0;
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
               ItemStack stack = inv.getItem(i);
               if (stack.is(ItemTags.HOES)) {
                  uses += stack.getMaxDamage() - stack.getDamageValue();
               }
            }

            return uses;
         }

         boolean fieldSoilReady() {
            Block soil = BuiltInRegistries.BLOCK.getValue(Identifier.parse(AiFarmArea.soilBlock(this.substrateId)));

            for (BlockPos p : this.farmPlan.interior()) {
               if (!this.farmPlan.waterCells().contains(p) && !mc.level.getBlockState(p).is(soil)) {
                  return false;
               }
            }

            return true;
         }

         AiOperation.Result tickVerifyPhase() {
            if (this.fieldConfirmAt == 0L) {
               this.fieldConfirmAt = System.nanoTime() + AiGameplay.pingMs(mc) * 50000000L;
               return this.running("Confirming the field with the server");
            }

            if (System.nanoTime() < this.fieldConfirmAt) {
               return this.running("Confirming the field with the server");
            }

            int farmland = 0;
            int crops = 0;
            int sources = 0;
            List<String> problems = new ArrayList<>();
            Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
            Block soil = BuiltInRegistries.BLOCK.getValue(Identifier.parse(AiFarmArea.soilBlock(this.substrateId)));

            for (BlockPos p : this.farmPlan.interior()) {
               if (this.farmPlan.waterCells().contains(p)) {
                  if (this.isWaterSourceAt(p)) {
                     sources++;
                  } else {
                     problems.add("no water at " + p.toShortString());
                  }
               } else {
                  if (mc.level.getBlockState(p).is(soil)) {
                     farmland++;
                  } else if (problems.size() < 8) {
                     problems.add("not " + AiFarmArea.soilBlock(this.substrateId) + " at " + p.toShortString());
                  }

                  if (mc.level.getBlockState(p.above()).is(crop)) {
                     crops++;
                  }
               }
            }

            String summary = layout.width()
               + "x"
               + layout.length()
               + " "
               + AiFarmBuild.displayName(cropBlock)
               + " farm at "
               + this.farmPlan.origin().toShortString()
               + ": "
               + crops
               + " planted, "
               + farmland
               + "/"
               + layout.plots()
               + (
                  this.soulSandFarm
                     ? " soul-sand plots"
                     : " plots tilled and watered by " + sources + " covered source" + (sources == 1 ? "" : "s") + ", water from " + this.waterSourceLabel
               );
            this.logProgress("verify: " + summary + (problems.isEmpty() ? "" : "; " + String.join(", ", problems)));
            if (!problems.isEmpty()) {
               return this.finish(AiOperation.Status.FAILED, summary + "; " + String.join(", ", problems));
            }

            if (crops < layout.plots()) {
               summary = summary + " (" + (layout.plots() - crops) + " plots wait for more seeds)";
            }

            return this.finish(AiOperation.Status.SUCCEEDED, summary);
         }

         AiOperation init(String item, int count, int seconds) {
            return AiObtain.obtain(
               AiJson.objectOf(
                  "item",
                  item,
                  "count",
                  Math.max(1, count),
                  "timeoutSeconds",
                  (int)Math.max(30L, Math.min(seconds, (this.deadlineNanos - System.nanoTime()) / 1000000000L))
               )
            );
         }

         AiOperation fetchWaterOp(final List<BlockPos> sources) {
            return new AiOperation() {
               AiOperation stepOp = AiGoto.approachAnyWaterSourceCompat(sources, 120);
               boolean fillStarted;
               BlockPos sourcePos = sources.getFirst();

               @Override
               public AiOperation.Result tick() {
                  AiOperation.Result r = this.stepOp.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.stepOp.cancel();
                  if (r.status() == AiOperation.Status.SUCCEEDED && !this.fillStarted) {
                     JsonObject data = AiJson.parseObject(r.dataJson());
                     if (data.has("fluidSourceX")) {
                        this.sourcePos = new BlockPos(
                           data.get("fluidSourceX").getAsInt(), data.get("fluidSourceY").getAsInt(), data.get("fluidSourceZ").getAsInt()
                        );
                     }

                     this.fillStarted = true;
                     this.stepOp = AiBucket.create(
                        AiJson.objectOf(
                           "action",
                           "fill",
                           "fluid",
                           "water",
                           "x",
                           this.sourcePos.getX(),
                           "y",
                           this.sourcePos.getY(),
                           "z",
                           this.sourcePos.getZ(),
                           "timeoutSeconds",
                           10
                        )
                     );
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Filling the bucket", new JsonObject());
                  } else {
                     return r;
                  }
               }

               @Override
               public void cancel() {
                  this.stepOp.cancel();
               }
            };
         }

         AiOperation pourAtOp(final BlockPos cell) {
            return new AiOperation() {
               AiOperation stepOp;
               boolean pourStarted;

               @Override
               public AiOperation.Result tick() {
                  if (this.stepOp == null) {
                     BlockHitResult floor = autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, cell.below(), Direction.UP);
                     this.stepOp = floor != null
                           && !(mc.player.getEyePosition().distanceTo(floor.getLocation()) > mc.player.blockInteractionRange() - 1.0)
                        ? this.pourWaterOp()
                        : AiGoto.approachBlockToInteractCompat(cell.below(), 30, init_Set(cell), true, Direction.UP, 1.5);
                  }

                  AiOperation.Result r = this.stepOp.tick();
                  if (!r.terminal()) {
                     return r;
                  } else {
                     this.stepOp.cancel();
                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return r;
                     } else if (AiFarmBuild.isWaterSource(mc.level.getBlockState(cell))) {
                        return r;
                     } else if (!this.pourStarted) {
                        this.stepOp = this.pourWaterOp();
                        return AiGameOperations.result(AiOperation.Status.RUNNING, "Pouring", new JsonObject());
                     } else {
                        return AiGameOperations.result(
                           AiOperation.Status.UNCERTAIN,
                           "The bucket changed, but the water source was not confirmed; reobserve before pouring again",
                           new JsonObject()
                        );
                     }
                  }
               }

               AiOperation pourWaterOp() {
                  this.pourStarted = true;
                  return AiBucket.create(
                     AiJson.objectOf("action", "empty", "fluid", "water", "x", cell.getX(), "y", cell.getY(), "z", cell.getZ(), "timeoutSeconds", 15)
                  );
               }

               @Override
               public void cancel() {
                  if (this.stepOp != null) {
                     this.stepOp.cancel();
                  }
               }
            };
         }

         BlockPos nearestWaterSource(BlockPos from) {
            double limit = 150.5;

            for (BlockPos p : AiTerrain.findFluidSources(mc, Blocks.WATER, (int)Math.min(160.0, limit + 8.0), 64)) {
               if (!(Math.sqrt(p.distSqr(from)) > limit) && mc.level.getBlockState(p.above()).isAir() && AiFarmBuild.isInfiniteWaterAt(mc.level::getBlockState, p)) {
                  return p;
               }
            }

            return null;
         }

         Map<Long, AiBuilder.CellSpec> poolShape() {
            Map<Long, AiBuilder.CellSpec> shape = new HashMap<>();

            for (BlockPos w : this.farmPlan.poolWalls()) {
               shape.put(w.asLong(), new AiBuilder.CellSpec(0, 0, 0, "minecraft:dirt"));
            }

            for (BlockPos c : this.farmPlan.poolCells()) {
               shape.put(c.asLong(), new AiBuilder.CellSpec(0, 0, 0, "minecraft:water"));
               shape.put(c.below().asLong(), new AiBuilder.CellSpec(0, 0, 0, "minecraft:dirt"));
            }

            return shape;
         }

         Set<Long> protectedFarmCells() {
            Set<Long> out = new HashSet<>();
            if (this.farmPlan == null) {
               return out;
            }

            int g = this.farmPlan.ground();

            for (BlockPos p : this.farmPlan.interior()) {
               out.add(p.asLong());
               out.add(p.below().asLong());
            }

            for (BlockPos p : this.farmPlan.ring()) {
               out.add(p.asLong());
               out.add(p.above().asLong());
            }

            if (this.farmPlan.pool()) {
               for (BlockPos p : this.farmPlan.poolWalls()) {
                  out.add(p.asLong());
               }
            }

            for (BlockPos p : this.farmPlan.waterCells()) {
               out.add(p.below().asLong());
            }

            out.removeIf(key -> BlockPos.of(key).getY() > g + 1);
            return out;
         }

         Set<Long> init_Set(BlockPos p) {
            Set<Long> out = this.protectedFarmCells();
            out.remove(p.asLong());
            return out;
         }

         boolean isOpenOrSource(BlockPos p) {
            BlockState s = mc.level.getBlockState(p);
            return s.isAir() || s.canBeReplaced() || s.getFluidState().isSource() && s.getCollisionShape(mc.level, p).isEmpty();
         }

         boolean isWaterSourceAt(BlockPos p) {
            return AiFarmBuild.isWaterSource(mc.level.getBlockState(p));
         }

         boolean isWaterloggedSlabAt(BlockPos p) {
            BlockState s = mc.level.getBlockState(p);
            return s.getBlock() instanceof SlabBlock && AiFarmBuild.isWaterSource(s);
         }

         Map<String, Integer> woodInBag() {
            Map<String, Integer> out = new HashMap<>();
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
               ItemStack stack = inv.getItem(i);
               if (!stack.isEmpty()) {
                  String sid = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                  String sp = AiFarmBuild.woodTypeOf(sid);
                  if (sp != null) {
                     int planks = sid.endsWith("_planks") ? stack.getCount() : (sid.endsWith("_fence") ? stack.getCount() * 2 : stack.getCount() * 4);
                     out.merge(sp, planks, Integer::sum);
                  }
               }
            }

            return out;
         }

         String nearestLogWood() {
            BlockPos feet = mc.player.blockPosition();
            String best = null;
            double bestSq = 1.7976931348623157E308;
            MutableBlockPos p = new MutableBlockPos();

            for (int dx = -32; dx <= 32; dx++) {
               for (int dz = -32; dz <= 32; dz++) {
                  for (int dy = -6; dy <= 16; dy++) {
                     p.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                     double sq = dx * dx + dz * dz + dy * dy;
                     if (!(sq >= bestSq) && AiTerrain.isLoaded(mc.level, p)) {
                        BlockState s = mc.level.getBlockState(p);
                        if (s.is(BlockTags.LOGS)) {
                           String sp = AiFarmBuild.woodTypeOf(BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString());
                           if (sp != null) {
                              best = sp;
                              bestSq = sq;
                           }
                        }
                     }
                  }
               }
            }

            return best;
         }

         int slabCount() {
            int n = 0;
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
               if (inv.getItem(i).is(BlockItemTags.SLABS.item())) {
                  n += inv.getItem(i).getCount();
               }
            }

            return n;
         }

         String firstSlabId() {
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
               if (inv.getItem(i).is(BlockItemTags.SLABS.item())) {
                  return BuiltInRegistries.ITEM.getKey(inv.getItem(i).getItem()).toString();
               }
            }

            return null;
         }

         String minecraftId(String path) {
            return "minecraft:" + path;
         }

         void logProgress(String line) {
            if (this.progressLog.size() < 60) {
               this.progressLog.add(line);
            }

            AutismClientAddon.LOG.info("[AI] farm: {}", line);
         }

         JsonObject statusData() {
            JsonObject out = AiJson.objectOf(
               "phase",
               this.phase.name(),
               "crop",
               cropBlock,
               "substrate",
               this.substrateId,
               "width",
               layout.width(),
               "length",
               layout.length(),
               "plots",
               layout.plots(),
               "sources",
               layout.water().size(),
               "water",
               this.waterSourceLabel,
               "planted",
               this.flush,
               "log",
               this.progressLog
            );
            if (this.chosenArea != null) {
               out.add("area", this.chosenArea.arguments(this.substrateId));
            }

            if (this.farmPlan != null) {
               out.add(
                  "site",
                  AiJson.objectOf(
                     "x",
                     this.farmPlan.origin().getX(),
                     "y",
                     this.farmPlan.origin().getY(),
                     "z",
                     this.farmPlan.origin().getZ(),
                     "gate",
                     this.farmPlan.gateSide().getName(),
                     "pool",
                     this.farmPlan.pool()
                  )
               );
            }

            return out;
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(
               AiOperation.Status.RUNNING, "Farm (" + this.phase.name().toLowerCase(Locale.ROOT) + "): " + message, this.statusData()
            );
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            if (this.stepOp != null) {
               this.stepOp.cancel();
            }

            this.stepOp = null;
            return AiGameOperations.result(status, message, this.statusData());
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.stepOp != null) {
               this.stepOp.cancel();
            }
         }
      };
   }

   static String missingMaterial(String message) {
      String key = "Missing material: ";
      int at = message == null ? -1 : message.indexOf(key);
      if (at < 0) {
         return null;
      }

      String rest = message.substring(at + key.length()).trim();
      int end = 0;

      while (end < rest.length() && (Character.isLetterOrDigit(rest.charAt(end)) || ":_./".indexOf(rest.charAt(end)) >= 0)) {
         end++;
      }

      return end == 0 ? null : rest.substring(0, end);
   }

   static String displayName(String id) {
      return id.substring(id.indexOf(58) + 1).replace('_', ' ');
   }

   record EdgeRun(Direction out, int length) {
   }

   enum BuildPhase {
      SEEDS,
      HOE,
      SITE,
      WOOD,
      DIRT,
      BUCKETS,
      CLEAR,
      FOUNDATION,
      POOL_FILL,
      POOL_DIG,
      POOL_POUR,
      POOL_SETTLE,
      CENTER_FILL,
      CENTER_DIG,
      CENTER_POUR,
      CENTER_COVER,
      FIELD,
      VERIFY;
   }

   record FarmSite(BlockPos origin, AiFarmBuild.FarmLayout layout, Direction gateSide, boolean pool, double cost) {
      int ground() {
         return this.origin.getY();
      }

      BlockPos cell(int x, int z) {
         return this.origin.offset(x, 0, z);
      }

      BlockPos side(int a, int o) {
         int w = this.layout.width();
         int l = this.layout.length();
         BlockPos var10000;
         switch (this.gateSide) {
            case NORTH:
               var10000 = this.origin.offset(a, 0, -1 - o);
               break;
            case SOUTH:
               var10000 = this.origin.offset(a, 0, l + o);
               break;
            case WEST:
               var10000 = this.origin.offset(-1 - o, 0, a);
               break;
            default:
               var10000 = this.origin.offset(w + o, 0, a);
         }

         return var10000;
      }

      int sideLength() {
         return this.gateSide.getAxis() == Axis.Z ? this.layout.width() : this.layout.length();
      }

      int gateAlong() {
         return this.sideLength() / 2;
      }

      BlockPos gate() {
         return this.side(this.gateAlong(), 0).above();
      }

      BlockPos outside() {
         return this.side(this.gateAlong(), 1).above();
      }

      List<BlockPos> poolCells() {
         int a = this.gateAlong() + 2;
         return List.of(this.side(a, 1), this.side(a + 1, 1), this.side(a, 2), this.side(a + 1, 2));
      }

      List<BlockPos> poolPours() {
         int a = this.gateAlong() + 2;
         return List.of(this.side(a, 1), this.side(a + 1, 2));
      }

      List<BlockPos> poolWalls() {
         int a = this.gateAlong() + 2;
         List<BlockPos> out = new ArrayList<>();

         for (int da = -1; da <= 2; da++) {
            for (int o = 0; o <= 3; o++) {
               if (da < 0 || da > 1 || o == 0 || o == 3) {
                  out.add(this.side(a + da, o));
               }
            }
         }

         return out;
      }

      List<BlockPos> ring() {
         int w = this.layout.width();
         int l = this.layout.length();
         List<BlockPos> out = new ArrayList<>();

         for (int x = -1; x <= w; x++) {
            for (int z = -1; z <= l; z++) {
               if (x == -1 || z == -1 || x == w || z == l) {
                  out.add(this.origin.offset(x, 0, z));
               }
            }
         }

         return out;
      }

      List<BlockPos> interior() {
         List<BlockPos> out = new ArrayList<>();

         for (int x = 0; x < this.layout.width(); x++) {
            for (int z = 0; z < this.layout.length(); z++) {
               out.add(this.origin.offset(x, 0, z));
            }
         }

         return out;
      }

      List<BlockPos> waterCells() {
         return this.layout.water().stream().map(w -> this.origin.offset(w[0], 0, w[1])).toList();
      }

      JsonObject box() {
         int minX = this.origin.getX() - 1;
         int minZ = this.origin.getZ() - 1;
         int maxX = this.origin.getX() + this.layout.width();
         int maxZ = this.origin.getZ() + this.layout.length();
         if (this.pool) {
            for (BlockPos p : this.poolWalls()) {
               minX = Math.min(minX, p.getX());
               minZ = Math.min(minZ, p.getZ());
               maxX = Math.max(maxX, p.getX());
               maxZ = Math.max(maxZ, p.getZ());
            }
         }

         return AiJson.objectOf(
            "minX", minX - 1, "minY", this.ground() - 3, "minZ", minZ - 1, "maxX", maxX + 1, "maxY", this.ground() + 3, "maxZ", maxZ + 1
         );
      }
   }

   record ColumnScan(int top, boolean natural, boolean tillable, boolean fluid, boolean lava, boolean obstacle) {
      static final AiFarmBuild.ColumnScan NONE = new AiFarmBuild.ColumnScan(-2147483648, false, false, false, false, true);
   }

   record FarmLayout(int width, int length, List<int[]> water) {
      int plots() {
         return this.width * this.length - this.water.size();
      }

      boolean water(int x, int z) {
         for (int[] w : this.water) {
            if (w[0] == x && w[1] == z) {
               return true;
            }
         }

         return false;
      }
   }
}
