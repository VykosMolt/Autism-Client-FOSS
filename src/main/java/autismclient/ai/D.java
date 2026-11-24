package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
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

final class D {
   static final int init_I = 9;
   static final int load = 80;
   static final int read = 320;
   static final int write = 27;
   static final int update = 35;
   static final double init_D = 4.3;
   static final int sync = 20;
   static final int flush = 3;
   static final int close = 24;
   static final Set<String> init_Set = Set.of("minecraft:carrot", "minecraft:potato");
   static final List<String> init_List = List.of(
      "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped"
   );

   private D() {
   }

   static D.Inner_StreamUtil init(int plots, int width, int length) {
      if (width > 0 && length > 0) {
         int w = Math.max(1, Math.min(27, width));
         int l = Math.max(1, Math.min(27, length));
         return new D.Inner_StreamUtil(w, l, init(w, l));
      }

      int n = Math.max(1, Math.min(320, plots));
      if (n > 80) {
         int tiles = (n + 81 - 2) / 80;
         int w = tiles >= 4 ? 18 : 9;
         int l = tiles >= 4 ? 18 : tiles * 9;
         return new D.Inner_StreamUtil(w, l, init(w, l));
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

      return new D.Inner_StreamUtil(bestW, bestL, init(bestW, bestL));
   }

   static List<int[]> init(int width, int length) {
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

   static boolean init(D.Inner_StreamUtil layout) {
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

   static boolean init(Function<BlockPos, BlockState> at, BlockPos pos) {
      if (!init(at.apply(pos))) {
         return false;
      }

      int sources = 0;

      for (Direction d : Plane.HORIZONTAL) {
         if (init(at.apply(pos.relative(d)))) {
            sources++;
         }
      }

      BlockState below = at.apply(pos.below());
      return sources >= 2 && (init(below) || !below.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty());
   }

   private static boolean init(BlockState state) {
      return state.getFluidState().isSource() && state.getFluidState().getType().isSame(Fluids.WATER);
   }

   static double init(double distance) {
      return distance / 4.3;
   }

   static boolean init(String substrate, int sourceCount, int waterHeld, boolean infiniteSource) {
      return init(substrate, sourceCount, waterHeld, infiniteSource, true);
   }

   static boolean init(String substrate, int sourceCount, int waterHeld, boolean infiniteSource, boolean automaticSite) {
      return !C.init_Z(substrate) && !infiniteSource && (automaticSite || waterHeld < sourceCount);
   }

   static D.Inner_DataUtil init(Function<BlockPos, BlockState> at, int x, int z, int fromY, int toY) {
      boolean obstacle = false;
      MutableBlockPos p = new MutableBlockPos();

      for (int y = fromY; y >= toY; y--) {
         BlockState state = at.apply(p.set(x, y, z));
         if (!state.isAir()) {
            if (!state.getFluidState().isEmpty()) {
               boolean lava = !state.getFluidState().getType().isSame(Fluids.WATER);
               return new D.Inner_DataUtil(y, false, false, true, lava, obstacle);
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
                  return new D.Inner_DataUtil(y, natural && !state.hasBlockEntity(), tillable, false, false, obstacle);
               }
            } else {
               obstacle = true;
            }
         }
      }

      return D.Inner_DataUtil.NONE;
   }

   static D.Inner_ConfigStore init(
      D.Inner_StreamUtil layout, BlockPos origin, Direction gate, boolean pool, BiFunction<Integer, Integer, D.Inner_DataUtil> columns
   ) {
      int[] tops = new int[layout.width() * layout.length()];
      int i = 0;

      for (int x = 0; x < layout.width(); x++) {
         for (int z = 0; z < layout.length(); z++) {
            D.Inner_DataUtil c = columns.apply(origin.getX() + x, origin.getZ() + z);
            if (c.top() == -2147483648 || c.lava() || !c.natural() && !c.fluid()) {
               return null;
            }

            tops[i++] = c.top();
         }
      }

      Arrays.sort(tops);
      int ground = tops[tops.length / 2];
      D.Inner_ConfigStore site = new D.Inner_ConfigStore(new BlockPos(origin.getX(), ground, origin.getZ()), layout, gate, pool, 0.0);
      double cost = 0.0;
      Set<Long> seen = new HashSet<>();
      List<BlockPos> cells = new ArrayList<>(site.interior());
      cells.addAll(site.ring());
      if (pool) {
         cells.addAll(site.poolWalls());
      }

      for (BlockPos cell : cells) {
         if (seen.add(BlockPos.asLong(cell.getX(), 0, cell.getZ()))) {
            D.Inner_DataUtil c = columns.apply(cell.getX(), cell.getZ());
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

      return new D.Inner_ConfigStore(site.origin(), layout, gate, pool, cost);
   }

   static D.Inner_ConfigStore init(D.Inner_StreamUtil layout, BlockPos player, boolean pool, BiFunction<Integer, Integer, D.Inner_DataUtil> columns) {
      int w = layout.width();
      int l = layout.length();
      D.Inner_ConfigStore best = null;

      for (int ox = player.getX() - 20 - w; ox <= player.getX() + 20; ox++) {
         for (int oz = player.getZ() - 20 - l; oz <= player.getZ() + 20; oz++) {
            int[] tops = new int[w * l];
            int i = 0;
            boolean ok = true;

            for (int x = 0; x < w && ok; x++) {
               for (int z = 0; z < l && ok; z++) {
                  D.Inner_DataUtil c = columns.apply(ox + x, oz + z);
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
               Direction side = init(ox, oz, w, l, player);
               D.Inner_ConfigStore site = new D.Inner_ConfigStore(new BlockPos(ox, ground, oz), layout, side, pool, 0.0);
               double cost = 0.0;
               Set<Long> seen = new HashSet<>();
               List<BlockPos> cells = new ArrayList<>(site.interior());
               cells.addAll(site.ring());
               if (pool) {
                  cells.addAll(site.poolWalls());
               }

               for (BlockPos cell : cells) {
                  if (seen.add(BlockPos.asLong(cell.getX(), 0, cell.getZ()))) {
                     D.Inner_DataUtil c = columns.apply(cell.getX(), cell.getZ());
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
                     best = new D.Inner_ConfigStore(site.origin(), layout, side, pool, cost);
                  }
               }
            }
         }
      }

      return best;
   }

   static Direction init(int ox, int oz, int w, int l, BlockPos player) {
      double dx = player.getX() + 0.5 - (ox + w / 2.0);
      double dz = player.getZ() + 0.5 - (oz + l / 2.0);
      if (Math.abs(dx) * l > Math.abs(dz) * w) {
         return dx > 0.0 ? Direction.EAST : Direction.WEST;
      } else {
         return dz > 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   static String init(Map<String, Integer> woodInBag, String nearestLog) {
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

   static String init(String id) {
      String path = id.substring(id.indexOf(58) + 1).replace("stripped_", "");

      for (String s : init_List) {
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

   static AiOperation init(JsonObject args, final String cropBlock, final String seedItem) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null) {
         throw new IllegalArgumentException("Farming requires a connected living player");
      }

      if (seedItem == null) {
         throw new IllegalArgumentException(cropBlock + " is not planted from a seed item; build a wheat, carrot, potato, beetroot or nether-wart farm");
      }

      final C.Inner_DataUtil selectedArea = C.init_C_DataUtil(args);
      final String substrate = C.init_Str(args, cropBlock);
      D.Inner_StreamUtil sized = init(ag.init_I_JsonObject(args, "count", 80), ag.init_I_JsonObject(args, "width", 0), ag.init_I_JsonObject(args, "length", 0));
      final D.Inner_StreamUtil layout = selectedArea != null
         ? selectedArea.layout(substrate)
         : (C.init_Z(substrate) ? new D.Inner_StreamUtil(sized.width(), sized.length(), List.of()) : sized);
      if (selectedArea != null) {
         String issue = selectedArea.validate(substrate);
         if (!issue.isEmpty()) {
            throw new IllegalArgumentException(issue);
         }
      }

      if (layout.plots() < 1) {
         throw new IllegalArgumentException("The farm layout leaves no room for a crop beside its water");
      }

      final int timeoutSeconds = Math.max(60, Math.min(7200, ag.init_I_JsonObject(args, "timeoutSeconds", 3600)));
      return new AiOperation() {
         final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         final JsonArray init_JsonArray = new JsonArray();
         final String init_Str = substrate;
         final boolean init_Z = C.init_Z(substrate);
         final C.Inner_DataUtil init_C_DataUtil = selectedArea;
         final BlockPos init_BlockPos = this.init_C_DataUtil == null ? autismclient.ai.a.i.init(mc) : this.init_C_DataUtil.origin();
         boolean load_Z;
         boolean read_Z;
         D.Inner_CachePool init_D_CachePool = D.Inner_CachePool.init;
         AiOperation init_AiOperation;
         D.Inner_ConfigStore init_D_ConfigStore;
         String load_Str;
         String read_Str = "";
         BlockPos load_BlockPos;
         final List<BlockPos> init_List = new ArrayList<>();
         int init_I;
         int load_I;
         int read_I;
         int write_I;
         int update_I;
         int sync_I;
         int flush;
         int close;
         int open;
         long load_J;
         long read_J;
         long write_J;
         D.Inner_CachePool load_D_CachePool;
         boolean write_Z;
         boolean update_Z;
         boolean sync_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.sync_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Farm building ran out of time in " + this.init_D_CachePool);
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init_AiOperation_Result_3(r.message());
                  } else {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (this.write_Z) {
                        this.write_Z = false;
                        this.init_V(this.init_D_CachePool + " refill: " + r.message());
                        return r.status() != AiOperation.Status.SUCCEEDED
                           ? this.init(
                              r.status(), "Could not get the materials for the " + this.init_D_CachePool.name().toLowerCase(Locale.ROOT) + ": " + r.message()
                           )
                           : this.init_AiOperation_Result_3("Materials refilled");
                     } else {
                        return this.init(r);
                     }
                  }
               } else {
                  if (this.init_D_CachePool != this.load_D_CachePool) {
                     this.load_D_CachePool = this.init_D_CachePool;
                     this.close = 0;
                  }

                  if (this.close > 12) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        "Farm building is stuck in "
                           + this.init_D_CachePool
                           + " ("
                           + (this.init_JsonArray.isEmpty() ? "no progress" : this.init_JsonArray.get(this.init_JsonArray.size() - 1).getAsString())
                           + ")"
                     );
                  }

                  AiOperation.Result var10000;
                  switch (this.init_D_CachePool) {
                     case init:
                        var10000 = this.init_AiOperation_Result_4();
                        break;
                     case load:
                        var10000 = this.load_AiOperation_Result_3();
                        break;
                     case read:
                        var10000 = this.read_AiOperation_Result_2();
                        break;
                     case write:
                        var10000 = this.write_AiOperation_Result();
                        break;
                     case update:
                        var10000 = this.update();
                        break;
                     case sync:
                        var10000 = this.sync();
                        break;
                     case flush:
                        var10000 = this.flush();
                        break;
                     case close:
                        var10000 = this.close();
                        break;
                     case open:
                        var10000 = this.open();
                        break;
                     case poll:
                        var10000 = this.poll();
                        break;
                     case push:
                        var10000 = this.push();
                        break;
                     case pop:
                        var10000 = this.pop();
                        break;
                     case tick:
                        var10000 = this.run();
                        break;
                     case run:
                        var10000 = this.get();
                        break;
                     case get:
                        var10000 = this.set();
                        break;
                     case set:
                        var10000 = this.put();
                        break;
                     case put:
                        var10000 = this.add();
                        break;
                     case add:
                        var10000 = this.clear();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  AiOperation.Result next = var10000;
                  if (this.init_AiOperation != null) {
                     this.close++;
                  }

                  return next;
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Farm building cancelled");
            }
         }

         AiOperation.Result init(AiOperation.Result r) {
            boolean ok = r.status() == AiOperation.Status.SUCCEEDED;
            this.init_V(this.init_D_CachePool + ": " + r.message());
            if (!ok) {
               JsonObject data = ag.init_JsonObject_Str(r.dataJson());
               if (data.has("failed")) {
                  this.init_V(
                     this.init_D_CachePool
                        + " failed cells: "
                        + data.get("failed").toString().substring(0, Math.min(1500, data.get("failed").toString().length()))
                  );
               }

               this.init_V(
                  this.init_D_CachePool
                     + " body at "
                     + String.format(Locale.ROOT, "%.2f %.2f %.2f", mc.player.getX(), mc.player.getY(), mc.player.getZ())
                     + " holding "
                     + BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem())
               );
            }

            switch (this.init_D_CachePool) {
               case init:
                  if (S.init_I_Str(seedItem) <= 0) {
                     if (D.init_Set.contains(seedItem) && !this.read_Z) {
                        this.read_Z = true;
                        this.init_AiOperation = new AiOperation() {
                           AiOperation init_AiOperation = R.init(ag.init_JsonObject_ObjArr("maxDistance", 1500, "timeoutSeconds", 900), true);
                           boolean init_Z;

                           @Override
                           public AiOperation.Result tick() {
                              AiOperation.Result s = this.init_AiOperation.tick();
                              if (s.terminal() && !this.init_Z) {
                                 this.init_AiOperation.cancel();
                                 if (s.status() != AiOperation.Status.SUCCEEDED) {
                                    return s;
                                 }

                                 this.init_Z = true;
                                 this.init_AiOperation = init(seedItem, Math.min(layout.plots(), 24), 600);
                                 return R.init_AiOperation_Result(
                                    AiOperation.Status.RUNNING, "Village found; taking " + seedItem + " from its fields", new JsonObject()
                                 );
                              } else {
                                 return s;
                              }
                           }

                           @Override
                           public void cancel() {
                              this.init_AiOperation.cancel();
                           }
                        };
                        return this.init_AiOperation_Result_3("No " + seedItem + " nearby; looking for a village");
                     }

                     return this.init(AiOperation.Status.FAILED, "No " + seedItem + " to plant: " + r.message());
                  }

                  this.init_D_CachePool = this.init_Z ? D.Inner_CachePool.read : D.Inner_CachePool.load;
                  break;
               case load:
                  if (!F.init(mc)) {
                     return this.init(AiOperation.Status.FAILED, "No hoe to till with: " + r.message());
                  }

                  this.init_D_CachePool = D.Inner_CachePool.read;
                  break;
               case read:
                  if (!ok) {
                     this.init_V("walk home: " + r.message());
                  }
                  break;
               case write:
                  if (!ok) {
                     return this.init(r.status(), "Could not get the slabs or the hoe: " + r.message());
                  }

                  return this.init_AiOperation_Result_3("Fence materials ready");
               case update:
                  if (!ok && e.init_I(C.load(this.init_Str)) < this.init_I_2()) {
                     return this.init(r.status(), "Could not get " + C.load(this.init_Str) + " to level the field: " + r.message());
                  }

                  this.init_D_CachePool = this.init_Z ? D.Inner_CachePool.close : D.Inner_CachePool.sync;
                  break;
               case sync:
                  if (!ok) {
                     return this.init(r.status(), "Could not get water for the field: " + r.message());
                  }

                  return this.init_AiOperation_Result_3("Water ready");
               case flush:
                  if (!ok) {
                     this.init_V("left " + this.init_List.get(this.init_I).toShortString() + ": " + r.message());
                  }

                  this.init_I++;
                  break;
               case close:
               case put:
                  if (!ok) {
                     String missing = D.load(r.message());
                     if (missing == null && this.init_D_CachePool == D.Inner_CachePool.put && r.dataJson().contains("Missing hoe") && this.sync_I++ < 3) {
                        return this.init_AiOperation_Result_3("The hoe wore out; making another");
                     }

                     if (missing != null && this.sync_I++ < 3) {
                        this.init_AiOperation = this.init(missing, e.init_I(missing) + (missing.equals("minecraft:dirt") ? 16 : 1), 600);
                        this.write_Z = true;
                        this.update_Z = true;
                        return this.init_AiOperation_Result_3("Getting more " + missing);
                     }

                     if (this.init_D_CachePool == D.Inner_CachePool.put && this.load_Z_2()) {
                        this.init_D_CachePool = D.Inner_CachePool.add;
                        return this.init_AiOperation_Result_3("Field done");
                     }

                     return this.init(r.status(), (this.init_D_CachePool == D.Inner_CachePool.put ? "The field" : "The foundation") + " failed: " + r.message());
                  }

                  this.init_D_CachePool = this.init_D_CachePool == D.Inner_CachePool.close
                     ? (this.init_Z ? D.Inner_CachePool.put : (this.init_D_ConfigStore.pool() ? D.Inner_CachePool.open : D.Inner_CachePool.tick))
                     : D.Inner_CachePool.add;
                  break;
               case open:
                  if (!ok) {
                     return this.init(r.status(), "Could not fill buckets for the pool: " + r.message());
                  }
                  break;
               case poll:
                  if (!ok && !this.load(this.init_D_ConfigStore.poolCells().get(this.read_I))) {
                     return this.init(r.status(), "Could not dig the pool: " + r.message());
                  }

                  this.read_I++;
                  break;
               case push:
                  if (!ok && !D.init(mc.level.getBlockState(this.init_D_ConfigStore.poolPours().get(this.read_I)))) {
                     return this.init(r.status(), "Could not pour the pool: " + r.message());
                  }

                  this.read_I++;
               case pop:
               default:
                  break;
               case tick:
                  if (!ok) {
                     return this.init(r.status(), "Could not fill a bucket for the field: " + r.message());
                  }
                  break;
               case run:
                  if (!ok && !this.load(this.init_D_ConfigStore.waterCells().get(this.load_I))) {
                     return this.init(r.status(), "Could not open the water hole: " + r.message());
                  }

                  this.init_D_CachePool = D.Inner_CachePool.get;
                  break;
               case get:
                  if (!ok && !this.read(this.init_D_ConfigStore.waterCells().get(this.load_I))) {
                     return this.init(r.status(), "Could not pour the field's water: " + r.message());
                  }

                  this.init_D_CachePool = D.Inner_CachePool.set;
                  break;
               case set:
                  if (!ok && !this.write(this.init_D_ConfigStore.waterCells().get(this.load_I))) {
                     this.init_V("water left uncovered: " + r.message());
                  }

                  this.load_I++;
                  this.init_D_CachePool = D.Inner_CachePool.tick;
            }

            return this.init_AiOperation_Result_3(r.message());
         }

         AiOperation.Result init_AiOperation_Result_4() {
            int held = S.init_I_Str(seedItem);
            int want = Math.min(layout.plots(), 24);
            if (this.init_JsonArray.isEmpty()) {
               this.init_V("start: " + held + " " + seedItem + " in the bag, " + layout.plots() + " plots");
            }

            if (held >= want) {
               this.init_D_CachePool = this.init_Z ? D.Inner_CachePool.read : D.Inner_CachePool.load;
               return this.init_AiOperation_Result_3("Holding " + held + " " + seedItem);
            } else {
               this.init_AiOperation = this.init(seedItem, want, held > 0 ? 180 : 600);
               return this.init_AiOperation_Result_3("Getting " + seedItem + " to plant");
            }
         }

         AiOperation.Result load_AiOperation_Result_3() {
            if (F.init(mc)) {
               this.init_D_CachePool = D.Inner_CachePool.read;
               return this.init_AiOperation_Result_3("Hoe in the bag");
            } else {
               this.init_AiOperation = this.init("minecraft:wooden_hoe", 1, 600);
               return this.init_AiOperation_Result_3("Making a hoe");
            }
         }

         AiOperation.Result read_AiOperation_Result_2() {
            if (!this.load_Z && Math.sqrt(mc.player.blockPosition().distSqr(this.init_BlockPos)) > 32.0) {
               this.load_Z = true;
               this.init_AiOperation = Math.sqrt(mc.player.blockPosition().distSqr(this.init_BlockPos)) > 96.0
                  ? cD.init(this.init_BlockPos, 3.0, true, true, true, true, 900)
                  : HashUtil.init_AiOperation_BlockPos(this.init_BlockPos, 3.0, true, true, true, 300);
               return this.init_AiOperation_Result_3("Walking back to where the farm goes");
            }

            BlockPos feet = this.init_BlockPos;
            this.load_BlockPos = this.init_Z ? null : this.init_BlockPos(feet);
            int waterHeld = this.init_Z ? 0 : e.init_I("minecraft:water_bucket");
            boolean pool = D.init(this.init_Str, layout.water().size(), waterHeld, this.load_BlockPos != null, this.init_C_DataUtil == null);
            HashMap<Long, D.Inner_DataUtil> cache = new HashMap<>();
            int fromY = (this.init_C_DataUtil == null ? feet.getY() : this.init_C_DataUtil.maxY()) + 8;
            int toY = (this.init_C_DataUtil == null ? feet.getY() : this.init_C_DataUtil.minY()) - 12;
            BiFunction<Integer, Integer, D.Inner_DataUtil> columns = (x, z) -> cache.computeIfAbsent(
               BlockPos.asLong(x, 0, z),
               k -> cu.init(mc.level, new BlockPos(x, feet.getY(), z)) ? D.init(mc.level::getBlockState, x, z, fromY, toY) : D.Inner_DataUtil.NONE
            );
            this.init_D_ConfigStore = this.init_C_DataUtil == null
               ? D.init(layout, feet, pool, columns)
               : D.init(
                  layout,
                  this.init_C_DataUtil.origin(),
                  D.init(this.init_C_DataUtil.minX(), this.init_C_DataUtil.minZ(), layout.width(), layout.length(), mc.player.blockPosition()),
                  pool,
                  columns
               );
            if (this.init_D_ConfigStore == null) {
               return this.init(
                  AiOperation.Status.FAILED,
                  this.init_C_DataUtil == null
                     ? "No open, level ground within 20 blocks for a "
                        + layout.width()
                        + "x"
                        + layout.length()
                        + " field (trees, lava, buildings or slopes steeper than 3 blocks)"
                     : "The selected " + layout.width() + "x" + layout.length() + " area is not usable; clear trees, lava, buildings and slopes over 3 blocks"
               );
            }

            this.read_Str = this.init_Z
               ? "not required"
               : (pool ? "own infinite pool" : (waterHeld >= layout.water().size() ? "held buckets" : "infinite source nearby"));
            if (this.load_BlockPos != null && waterHeld < layout.water().size()) {
               double d = Math.sqrt(this.load_BlockPos.distSqr(this.init_D_ConfigStore.origin()));
               this.init_V("infinite water " + Math.round(d) + " blocks away, about " + Math.round(D.init(d)) + " s");
            }

            this.init_V(
               "site "
                  + this.init_D_ConfigStore.origin().toShortString()
                  + " "
                  + layout.width()
                  + "x"
                  + layout.length()
                  + ", gate "
                  + this.init_D_ConfigStore.gateSide().getName()
                  + ", water: "
                  + this.read_Str
            );
            this.load_Str = this.init_Z ? "oak" : D.init(this.load_Map(), this.load_Str());
            this.init_D_CachePool = this.init_Z ? D.Inner_CachePool.update : D.Inner_CachePool.write;
            return this.init_AiOperation_Result_3("Field site chosen at " + this.init_D_ConfigStore.origin().toShortString());
         }

         AiOperation.Result write_AiOperation_Result() {
            if (this.write_I() < layout.water().size()) {
               this.init_AiOperation = this.init(
                  this.init_Str_2(this.load_Str + "_slab"), e.init_I(this.init_Str_2(this.load_Str + "_slab")) + layout.water().size() - this.write_I(), 600
               );
               return this.init_AiOperation_Result_3("Getting slabs to cover the water");
            } else if (this.read_I() < this.load_I() && this.open++ < 4) {
               this.init_AiOperation = this.init(this.init_Str_3(), e.init_I(this.init_Str_3()) + 1, 600);
               return this.init_AiOperation_Result_3("Making another hoe: " + this.read_I() + " uses for " + this.load_I() + " plots");
            } else {
               this.init_D_CachePool = D.Inner_CachePool.update;
               return this.init_AiOperation_Result_3("Slabs and hoes in the bag");
            }
         }

         AiOperation.Result update() {
            int need = this.init_I_2();
            String base = C.load(this.init_Str);
            if (e.init_I(base) >= need) {
               this.init_D_CachePool = this.init_Z ? D.Inner_CachePool.close : D.Inner_CachePool.sync;
               return this.init_AiOperation_Result_3(base + " in the bag");
            } else if (this.init_Z) {
               this.init_AiOperation = this.init(base, e.init_I(base) + need, 900);
               return this.init_AiOperation_Result_3("Getting soul sand for the field");
            } else {
               this.init_AiOperation = autismclient.ai.a.BufferHelper.init_AiOperation(
                  ag.init_JsonObject_ObjArr(
                     "blocks",
                     autismclient.ai.a.BufferHelper.init(List.of("minecraft:dirt", "minecraft:grass_block", "minecraft:coarse_dirt", "minecraft:rooted_dirt")),
                     "drops",
                     autismclient.ai.a.BufferHelper.init(List.of("minecraft:dirt")),
                     "held",
                     need,
                     "radius",
                     40,
                     "explore",
                     false,
                     "avoid",
                     this.init_D_ConfigStore.box(),
                     "timeoutSeconds",
                     300
                  )
               );
               return this.init_AiOperation_Result_3("Digging " + need + " dirt beside the site");
            }
         }

         AiOperation.Result sync() {
            int need = layout.water().size();
            int water = e.init_I("minecraft:water_bucket");
            int empty = e.init_I("minecraft:bucket");
            if (!this.init_D_ConfigStore.pool() && water >= need) {
               this.init_D_CachePool = D.Inner_CachePool.flush;
               return this.init_AiOperation_Result_3("Water in the bag");
            } else {
               int buckets = this.init_D_ConfigStore.pool() ? 2 : 1;
               if (water + empty < buckets) {
                  this.init_AiOperation = this.init("minecraft:bucket", empty + buckets - water - empty, 1800);
                  return this.init_AiOperation_Result_3("Making " + (buckets - water - empty) + " bucket(s)");
               } else {
                  this.init_D_CachePool = D.Inner_CachePool.flush;
                  return this.init_AiOperation_Result_3("Buckets ready");
               }
            }
         }

         AiOperation.Result flush() {
            if (this.init_List.isEmpty() && this.init_I == 0) {
               this.init_V_2();
            }

            while (this.init_I < this.init_List.size()) {
               BlockPos p = this.init_List.get(this.init_I);
               BlockState state = mc.level.getBlockState(p);
               if (!state.isAir() && (state.getFluidState().isEmpty() || !state.getCollisionShape(mc.level, p).isEmpty())) {
                  this.init_AiOperation = autismclient.ai.a.ConfigStore.init(p, 30, false, this.init_Set());
                  return this.init_AiOperation_Result_3("Clearing " + BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath() + " at " + p.toShortString());
               }

               this.init_I++;
            }

            this.init_D_CachePool = D.Inner_CachePool.close;
            return this.init_AiOperation_Result_3("Site cleared");
         }

         void init_V_2() {
            int g = this.init_D_ConfigStore.ground();
            Set<Long> columns = new HashSet<>();
            List<BlockPos> cells = new ArrayList<>(this.init_D_ConfigStore.interior());
            cells.addAll(this.init_D_ConfigStore.ring());
            if (this.init_D_ConfigStore.pool()) {
               cells.addAll(this.init_D_ConfigStore.poolWalls());
               cells.addAll(this.init_D_ConfigStore.poolCells());
            }

            for (BlockPos c : cells) {
               if (columns.add(BlockPos.asLong(c.getX(), 0, c.getZ()))) {
                  for (int y = g + 3 + 2; y >= g + 1; y--) {
                     BlockPos p = new BlockPos(c.getX(), y, c.getZ());
                     BlockState s = mc.level.getBlockState(p);
                     if (!s.isAir() && (y != g + 1 || !s.is(BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock))))) {
                        this.init_List.add(p);
                     }
                  }
               }
            }

            BlockPos feet = mc.player.blockPosition();
            this.init_List.sort(Comparator.<BlockPos>comparingInt(px -> -px.getY()).thenComparingDouble(px -> px.distSqr(feet)));
         }

         AiOperation.Result close() {
            List<TableIndex.Inner_StreamUtil> cells = this.init_List_2();
            if (cells.isEmpty()) {
               this.init_D_CachePool = this.init_Z ? D.Inner_CachePool.put : (this.init_D_ConfigStore.pool() ? D.Inner_CachePool.open : D.Inner_CachePool.tick);
               return this.init_AiOperation_Result_3("Ground already level");
            } else {
               this.init_AiOperation = TableIndex.init(new TableIndex.Inner_DataUtil(BlockPos.ZERO, cells), 900);
               return this.init_AiOperation_Result_3("Levelling " + cells.size() + " cells with " + C.load(this.init_Str));
            }
         }

         List<TableIndex.Inner_StreamUtil> init_List_2() {
            int g = this.init_D_ConfigStore.ground();
            Map<Long, TableIndex.Inner_StreamUtil> out = new LinkedHashMap<>();
            List<BlockPos> top = new ArrayList<>(this.init_D_ConfigStore.interior());
            top.addAll(this.init_D_ConfigStore.ring());

            for (BlockPos c : top) {
               if (!this.init_D_ConfigStore.waterCells().contains(c)) {
                  for (int y = g - 3; y <= g; y++) {
                     this.init(out, new BlockPos(c.getX(), y, c.getZ()), y < g);
                  }
               }
            }

            for (BlockPos c : this.init_D_ConfigStore.interior()) {
               if (!this.init_D_ConfigStore.waterCells().contains(c) && !out.containsKey(c.asLong())) {
                  BlockState s = mc.level.getBlockState(c);
                  if (!s.getCollisionShape(mc.level, c).isEmpty() && s.getFluidState().isEmpty()) {
                     boolean correct = this.init_Z
                        ? s.is(Blocks.SOUL_SAND)
                        : s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.DIRT_PATH) || s.is(Blocks.FARMLAND);
                     if (!correct && !s.hasBlockEntity()) {
                        out.put(c.asLong(), new TableIndex.Inner_StreamUtil(c.getX(), c.getY(), c.getZ(), C.load(this.init_Str)));
                     }
                  }
               }
            }

            for (BlockPos w : this.init_D_ConfigStore.waterCells()) {
               for (int y = g - 3; y <= g - 1; y++) {
                  this.init(out, new BlockPos(w.getX(), y, w.getZ()), true);
               }
            }

            if (this.init_D_ConfigStore.pool()) {
               for (BlockPos wall : this.init_D_ConfigStore.poolWalls()) {
                  for (int y = g - 3; y <= g; y++) {
                     this.init(out, new BlockPos(wall.getX(), y, wall.getZ()), y < g);
                  }
               }

               for (BlockPos cell : this.init_D_ConfigStore.poolCells()) {
                  for (int y = g - 3; y <= g - 1; y++) {
                     this.init(out, new BlockPos(cell.getX(), y, cell.getZ()), true);
                  }
               }
            }

            return new ArrayList<>(out.values());
         }

         void init(Map<Long, TableIndex.Inner_StreamUtil> out, BlockPos p, boolean below) {
            BlockState state = mc.level.getBlockState(p);
            if ((!bQ.read(state) || state.getFluidState().isSource())
               && (state.getCollisionShape(mc.level, p).isEmpty() || !state.getFluidState().isEmpty())
               && (!below || this.init_Z_2(p))) {
               out.put(p.asLong(), new TableIndex.Inner_StreamUtil(p.getX(), p.getY(), p.getZ(), C.load(this.init_Str)));
            }
         }

         boolean init_Z_2(BlockPos p) {
            for (int y = p.getY() + 1; y < this.init_D_ConfigStore.ground(); y++) {
               BlockPos q = new BlockPos(p.getX(), y, p.getZ());
               BlockState s = mc.level.getBlockState(q);
               if (!s.getCollisionShape(mc.level, q).isEmpty() && s.getFluidState().isEmpty()) {
                  return false;
               }
            }

            return true;
         }

         int init_I_2() {
            if (this.init_D_ConfigStore == null) {
               return 0;
            }

            int cells = this.init_List_2().size();
            if (this.init_Z) {
               return cells;
            }

            for (BlockPos c : this.init_D_ConfigStore.interior()) {
               if (!this.init_D_ConfigStore.waterCells().contains(c)) {
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

         AiOperation.Result open() {
            int water = e.init_I("minecraft:water_bucket");
            if (water < 2 && !this.init_Z_2()) {
               List<BlockPos> sources = cu.init(mc, Blocks.WATER, 160, 64, true);
               if (sources.isEmpty()) {
                  return this.init(AiOperation.Status.FAILED, "No water anywhere in loaded terrain to start the farm's pool; bring two water buckets");
               }

               this.init_AiOperation = this.init(sources);
               return this.init_AiOperation_Result_3("Filling a bucket for the infinite pool");
            } else {
               this.init_D_CachePool = D.Inner_CachePool.poll;
               this.read_I = 0;
               return this.init_AiOperation_Result_3("Two bucketfuls for the pool");
            }
         }

         AiOperation.Result poll() {
            for (List<BlockPos> cells = this.init_D_ConfigStore.poolCells(); this.read_I < cells.size(); this.read_I++) {
               BlockPos p = cells.get(this.read_I);
               if (!this.load(p)) {
                  this.init_AiOperation = autismclient.ai.a.ConfigStore.init(p, 30, false, this.init_Set(p));
                  return this.init_AiOperation_Result_3("Digging the pool at " + p.toShortString());
               }
            }

            this.init_D_CachePool = D.Inner_CachePool.push;
            this.read_I = 0;
            return this.init_AiOperation_Result_3("Pool dug");
         }

         AiOperation.Result push() {
            if (this.write_J == 0L) {
               this.write_J = System.nanoTime() + T.init_I(mc) * 50000000L;
               return this.init_AiOperation_Result_3("Confirming the pool's floor with the server");
            }

            if (System.nanoTime() < this.write_J) {
               return this.init_AiOperation_Result_3("Confirming the pool's floor with the server");
            }

            for (List<BlockPos> pours = this.init_D_ConfigStore.poolPours(); this.read_I < pours.size(); this.read_I++) {
               BlockPos p = pours.get(this.read_I);
               if (!D.init(mc.level.getBlockState(p))) {
                  String leak = bQ.init(p, mc.level, this.init_Map());
                  if (leak != null) {
                     return this.init(AiOperation.Status.FAILED, "The pool would spill: " + leak);
                  }

                  this.init_AiOperation = this.init_AiOperation(p);
                  return this.init_AiOperation_Result_3("Pouring the pool at " + p.toShortString());
               }
            }

            this.init_D_CachePool = D.Inner_CachePool.pop;
            this.write_I = 0;
            return this.init_AiOperation_Result_3("Pool poured");
         }

         AiOperation.Result pop() {
            if (this.init_Z_2()) {
               this.init_D_CachePool = D.Inner_CachePool.tick;
               return this.init_AiOperation_Result_3("The pool is an infinite source");
            } else {
               return ++this.write_I > 60
                  ? this.init(AiOperation.Status.FAILED, "The pool did not become an infinite source")
                  : this.init_AiOperation_Result_3("Waiting for the pool to fill");
            }
         }

         boolean init_Z_2() {
            for (BlockPos p : this.init_D_ConfigStore.poolCells()) {
               if (!D.init(mc.level.getBlockState(p))) {
                  return false;
               }
            }

            return true;
         }

         AiOperation.Result run() {
            List<BlockPos> centers = this.init_D_ConfigStore.waterCells();

            while (this.load_I < centers.size() && this.write(centers.get(this.load_I))) {
               this.load_I++;
            }

            if (this.load_I >= centers.size()) {
               this.init_D_CachePool = D.Inner_CachePool.put;
               return this.init_AiOperation_Result_3("Every source in place");
            }

            if (this.read(centers.get(this.load_I))) {
               this.init_D_CachePool = D.Inner_CachePool.set;
               return this.init_AiOperation_Result_3("Covering the source");
            }

            if (e.init_I("minecraft:water_bucket") > 0) {
               this.init_D_CachePool = D.Inner_CachePool.run;
               return this.init_AiOperation_Result_3("Water in hand");
            }

            List<BlockPos> sources = this.init_D_ConfigStore.pool()
               ? this.init_D_ConfigStore.poolCells()
               : (this.load_BlockPos != null ? List.of(this.load_BlockPos) : cu.init(mc, Blocks.WATER, 160, 64, true));
            if (sources.isEmpty()) {
               return this.init(AiOperation.Status.FAILED, "No water source to fill a bucket from");
            }

            this.init_AiOperation = this.init(sources);
            return this.init_AiOperation_Result_3("Filling a bucket for the field");
         }

         AiOperation.Result get() {
            BlockPos c = this.init_D_ConfigStore.waterCells().get(this.load_I);
            if (this.load(c)) {
               if (this.read_J == 0L) {
                  this.read_J = System.nanoTime() + T.init_I(mc) * 50000000L;
                  return this.init_AiOperation_Result_3("Confirming the water hole with the server");
               }

               if (System.nanoTime() < this.read_J) {
                  return this.init_AiOperation_Result_3("Confirming the water hole with the server");
               }

               this.read_J = 0L;
               this.init_D_CachePool = D.Inner_CachePool.get;
               return this.init_AiOperation_Result_3("Water hole open");
            } else {
               this.init_AiOperation = autismclient.ai.a.ConfigStore.init(c, 30, false, this.init_Set(c));
               return this.init_AiOperation_Result_3("Opening the water hole at " + c.toShortString());
            }
         }

         AiOperation.Result set() {
            BlockPos c = this.init_D_ConfigStore.waterCells().get(this.load_I);
            if (this.read(c)) {
               this.init_D_CachePool = D.Inner_CachePool.set;
               return this.init_AiOperation_Result_3("Source in place");
            }

            Map<Long, TableIndex.Inner_StreamUtil> shape = new HashMap<>();

            for (Direction d : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
               shape.put(c.relative(d).asLong(), new TableIndex.Inner_StreamUtil(0, 0, 0, "minecraft:dirt"));
            }

            String leak = bQ.init(c, mc.level, shape);
            if (leak != null) {
               return this.init(AiOperation.Status.FAILED, "The field's water would spill: " + leak);
            }

            this.init_AiOperation = this.init_AiOperation(c);
            return this.init_AiOperation_Result_3("Pouring the field's water at " + c.toShortString());
         }

         AiOperation.Result put() {
            BlockPos c = this.init_D_ConfigStore.waterCells().get(this.load_I);
            if (this.write(c)) {
               this.load_I++;
               this.init_D_CachePool = D.Inner_CachePool.tick;
               return this.init_AiOperation_Result_3("Source covered");
            } else {
               String slab = this.read_Str();
               if (slab == null) {
                  this.init_V("no slab to cover the water at " + c.toShortString());
                  this.load_I++;
                  this.init_D_CachePool = D.Inner_CachePool.tick;
                  return this.init_AiOperation_Result_3("No slab");
               } else {
                  this.init_AiOperation = TableIndex.init(
                     new TableIndex.Inner_DataUtil(c, List.of(new TableIndex.Inner_StreamUtil(0, 0, 0, slab, Map.of("type", "bottom", "waterlogged", "true")))),
                     60
                  );
                  return this.init_AiOperation_Result_3("Covering the source with a slab");
               }
            }
         }

         AiOperation.Result add() {
            int tills = this.load_I();
            int uses = this.read_I();
            if (uses < tills && this.open++ < 4) {
               this.init_AiOperation = this.init(this.init_Str_3(), e.init_I(this.init_Str_3()) + 1, 600);
               this.write_Z = true;
               this.update_Z = true;
               return this.init_AiOperation_Result_3("Making another hoe: " + uses + " uses left for " + tills + " plots");
            } else if (this.update_Z) {
               this.update_Z = false;
               this.init_List.clear();
               this.init_I = 0;
               this.init_D_CachePool = D.Inner_CachePool.flush;
               return this.init_AiOperation_Result_3("Checking the site after the refill");
            } else {
               List<BlockPos> plots = new ArrayList<>(this.init_D_ConfigStore.interior());
               plots.removeAll(this.init_D_ConfigStore.waterCells());
               this.init_AiOperation = I.init(plots, this.init_D_ConfigStore.origin(), layout.width(), layout.length(), cropBlock, seedItem);
               return this.init_AiOperation_Result_3("Tilling and sowing " + layout.plots() + " plots on the move");
            }
         }

         String init_Str_3() {
            return e.init_I("minecraft:cobblestone") + e.init_I("minecraft:cobbled_deepslate") >= 2 ? "minecraft:stone_hoe" : "minecraft:wooden_hoe";
         }

         int load_I() {
            if (this.init_Z) {
               return 0;
            }

            int n = 0;

            for (BlockPos p : this.init_D_ConfigStore.interior()) {
               if (!this.init_D_ConfigStore.waterCells().contains(p) && !mc.level.getBlockState(p).is(Blocks.FARMLAND)) {
                  n++;
               }
            }

            return n;
         }

         int read_I() {
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

         boolean load_Z_2() {
            Block soil = BuiltInRegistries.BLOCK.getValue(Identifier.parse(C.read(this.init_Str)));

            for (BlockPos p : this.init_D_ConfigStore.interior()) {
               if (!this.init_D_ConfigStore.waterCells().contains(p) && !mc.level.getBlockState(p).is(soil)) {
                  return false;
               }
            }

            return true;
         }

         AiOperation.Result clear() {
            if (this.load_J == 0L) {
               this.load_J = System.nanoTime() + T.init_I(mc) * 50000000L;
               return this.init_AiOperation_Result_3("Confirming the field with the server");
            }

            if (System.nanoTime() < this.load_J) {
               return this.init_AiOperation_Result_3("Confirming the field with the server");
            }

            int farmland = 0;
            int crops = 0;
            int sources = 0;
            List<String> problems = new ArrayList<>();
            Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
            Block soil = BuiltInRegistries.BLOCK.getValue(Identifier.parse(C.read(this.init_Str)));

            for (BlockPos p : this.init_D_ConfigStore.interior()) {
               if (this.init_D_ConfigStore.waterCells().contains(p)) {
                  if (this.read(p)) {
                     sources++;
                  } else {
                     problems.add("no water at " + p.toShortString());
                  }
               } else {
                  if (mc.level.getBlockState(p).is(soil)) {
                     farmland++;
                  } else if (problems.size() < 8) {
                     problems.add("not " + C.read(this.init_Str) + " at " + p.toShortString());
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
               + D.read(cropBlock)
               + " farm at "
               + this.init_D_ConfigStore.origin().toShortString()
               + ": "
               + crops
               + " planted, "
               + farmland
               + "/"
               + layout.plots()
               + (
                  this.init_Z
                     ? " soul-sand plots"
                     : " plots tilled and watered by " + sources + " covered source" + (sources == 1 ? "" : "s") + ", water from " + this.read_Str
               );
            this.init_V("verify: " + summary + (problems.isEmpty() ? "" : "; " + String.join(", ", problems)));
            if (!problems.isEmpty()) {
               return this.init(AiOperation.Status.FAILED, summary + "; " + String.join(", ", problems));
            }

            if (crops < layout.plots()) {
               summary = summary + " (" + (layout.plots() - crops) + " plots wait for more seeds)";
            }

            return this.init(AiOperation.Status.SUCCEEDED, summary);
         }

         AiOperation init(String item, int count, int seconds) {
            return aE.init(
               ag.init_JsonObject_ObjArr(
                  "item",
                  item,
                  "count",
                  Math.max(1, count),
                  "timeoutSeconds",
                  (int)Math.max(30L, Math.min(seconds, (this.init_J - System.nanoTime()) / 1000000000L))
               )
            );
         }

         AiOperation init(final List<BlockPos> sources) {
            return new AiOperation() {
               AiOperation init_AiOperation = HashUtil.init(sources, 120);
               boolean init_Z;
               BlockPos init_BlockPos = sources.getFirst();

               @Override
               public AiOperation.Result tick() {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.init_AiOperation.cancel();
                  if (r.status() == AiOperation.Status.SUCCEEDED && !this.init_Z) {
                     JsonObject data = ag.init_JsonObject_Str(r.dataJson());
                     if (data.has("fluidSourceX")) {
                        this.init_BlockPos = new BlockPos(
                           data.get("fluidSourceX").getAsInt(), data.get("fluidSourceY").getAsInt(), data.get("fluidSourceZ").getAsInt()
                        );
                     }

                     this.init_Z = true;
                     this.init_AiOperation = TextFormat.init(
                        ag.init_JsonObject_ObjArr(
                           "action",
                           "fill",
                           "fluid",
                           "water",
                           "x",
                           this.init_BlockPos.getX(),
                           "y",
                           this.init_BlockPos.getY(),
                           "z",
                           this.init_BlockPos.getZ(),
                           "timeoutSeconds",
                           10
                        )
                     );
                     return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Filling the bucket", new JsonObject());
                  } else {
                     return r;
                  }
               }

               @Override
               public void cancel() {
                  this.init_AiOperation.cancel();
               }
            };
         }

         AiOperation init_AiOperation(final BlockPos cell) {
            return new AiOperation() {
               AiOperation init_AiOperation;
               boolean init_Z;

               @Override
               public AiOperation.Result tick() {
                  if (this.init_AiOperation == null) {
                     BlockHitResult floor = autismclient.ai.a.CachePool.init(mc, cell.below(), Direction.UP);
                     this.init_AiOperation = floor != null
                           && !(mc.player.getEyePosition().distanceTo(floor.getLocation()) > mc.player.blockInteractionRange() - 1.0)
                        ? this.init()
                        : HashUtil.init_AiOperation_BlockPos_3(cell.below(), 30, init_Set(cell), true, Direction.UP, 1.5);
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  } else {
                     this.init_AiOperation.cancel();
                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return r;
                     } else if (D.init(mc.level.getBlockState(cell))) {
                        return r;
                     } else if (!this.init_Z) {
                        this.init_AiOperation = this.init();
                        return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Pouring", new JsonObject());
                     } else {
                        return R.init_AiOperation_Result(
                           AiOperation.Status.UNCERTAIN,
                           "The bucket changed, but the water source was not confirmed; reobserve before pouring again",
                           new JsonObject()
                        );
                     }
                  }
               }

               AiOperation init() {
                  this.init_Z = true;
                  return TextFormat.init(
                     ag.init_JsonObject_ObjArr("action", "empty", "fluid", "water", "x", cell.getX(), "y", cell.getY(), "z", cell.getZ(), "timeoutSeconds", 15)
                  );
               }

               @Override
               public void cancel() {
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                  }
               }
            };
         }

         BlockPos init_BlockPos(BlockPos from) {
            double limit = 150.5;

            for (BlockPos p : cu.init(mc, Blocks.WATER, (int)Math.min(160.0, limit + 8.0), 64)) {
               if (!(Math.sqrt(p.distSqr(from)) > limit) && mc.level.getBlockState(p.above()).isAir() && D.init(mc.level::getBlockState, p)) {
                  return p;
               }
            }

            return null;
         }

         Map<Long, TableIndex.Inner_StreamUtil> init_Map() {
            Map<Long, TableIndex.Inner_StreamUtil> shape = new HashMap<>();

            for (BlockPos w : this.init_D_ConfigStore.poolWalls()) {
               shape.put(w.asLong(), new TableIndex.Inner_StreamUtil(0, 0, 0, "minecraft:dirt"));
            }

            for (BlockPos c : this.init_D_ConfigStore.poolCells()) {
               shape.put(c.asLong(), new TableIndex.Inner_StreamUtil(0, 0, 0, "minecraft:water"));
               shape.put(c.below().asLong(), new TableIndex.Inner_StreamUtil(0, 0, 0, "minecraft:dirt"));
            }

            return shape;
         }

         Set<Long> init_Set() {
            Set<Long> out = new HashSet<>();
            if (this.init_D_ConfigStore == null) {
               return out;
            }

            int g = this.init_D_ConfigStore.ground();

            for (BlockPos p : this.init_D_ConfigStore.interior()) {
               out.add(p.asLong());
               out.add(p.below().asLong());
            }

            for (BlockPos p : this.init_D_ConfigStore.ring()) {
               out.add(p.asLong());
               out.add(p.above().asLong());
            }

            if (this.init_D_ConfigStore.pool()) {
               for (BlockPos p : this.init_D_ConfigStore.poolWalls()) {
                  out.add(p.asLong());
               }
            }

            for (BlockPos p : this.init_D_ConfigStore.waterCells()) {
               out.add(p.below().asLong());
            }

            out.removeIf(key -> BlockPos.of(key).getY() > g + 1);
            return out;
         }

         Set<Long> init_Set(BlockPos p) {
            Set<Long> out = this.init_Set();
            out.remove(p.asLong());
            return out;
         }

         boolean load(BlockPos p) {
            BlockState s = mc.level.getBlockState(p);
            return s.isAir() || s.canBeReplaced() || s.getFluidState().isSource() && s.getCollisionShape(mc.level, p).isEmpty();
         }

         boolean read(BlockPos p) {
            return D.init(mc.level.getBlockState(p));
         }

         boolean write(BlockPos p) {
            BlockState s = mc.level.getBlockState(p);
            return s.getBlock() instanceof SlabBlock && D.init(s);
         }

         Map<String, Integer> load_Map() {
            Map<String, Integer> out = new HashMap<>();
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
               ItemStack stack = inv.getItem(i);
               if (!stack.isEmpty()) {
                  String sid = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                  String sp = D.init(sid);
                  if (sp != null) {
                     int planks = sid.endsWith("_planks") ? stack.getCount() : (sid.endsWith("_fence") ? stack.getCount() * 2 : stack.getCount() * 4);
                     out.merge(sp, planks, Integer::sum);
                  }
               }
            }

            return out;
         }

         String load_Str() {
            BlockPos feet = mc.player.blockPosition();
            String best = null;
            double bestSq = 1.7976931348623157E308;
            MutableBlockPos p = new MutableBlockPos();

            for (int dx = -32; dx <= 32; dx++) {
               for (int dz = -32; dz <= 32; dz++) {
                  for (int dy = -6; dy <= 16; dy++) {
                     p.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                     double sq = dx * dx + dz * dz + dy * dy;
                     if (!(sq >= bestSq) && cu.init(mc.level, p)) {
                        BlockState s = mc.level.getBlockState(p);
                        if (s.is(BlockTags.LOGS)) {
                           String sp = D.init(BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString());
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

         int write_I() {
            int n = 0;
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
               if (inv.getItem(i).is(BlockItemTags.SLABS.item())) {
                  n += inv.getItem(i).getCount();
               }
            }

            return n;
         }

         String read_Str() {
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
               if (inv.getItem(i).is(BlockItemTags.SLABS.item())) {
                  return BuiltInRegistries.ITEM.getKey(inv.getItem(i).getItem()).toString();
               }
            }

            return null;
         }

         String init_Str_2(String path) {
            return "minecraft:" + path;
         }

         void init_V(String line) {
            if (this.init_JsonArray.size() < 60) {
               this.init_JsonArray.add(line);
            }

            AutismClientAddon.LOG.info("[AI] farm: {}", line);
         }

         JsonObject init_JsonObject_5() {
            JsonObject out = ag.init_JsonObject_ObjArr(
               "phase",
               this.init_D_CachePool.name(),
               "crop",
               cropBlock,
               "substrate",
               this.init_Str,
               "width",
               layout.width(),
               "length",
               layout.length(),
               "plots",
               layout.plots(),
               "sources",
               layout.water().size(),
               "water",
               this.read_Str,
               "planted",
               this.flush,
               "log",
               this.init_JsonArray
            );
            if (this.init_C_DataUtil != null) {
               out.add("area", this.init_C_DataUtil.arguments(this.init_Str));
            }

            if (this.init_D_ConfigStore != null) {
               out.add(
                  "site",
                  ag.init_JsonObject_ObjArr(
                     "x",
                     this.init_D_ConfigStore.origin().getX(),
                     "y",
                     this.init_D_ConfigStore.origin().getY(),
                     "z",
                     this.init_D_ConfigStore.origin().getZ(),
                     "gate",
                     this.init_D_ConfigStore.gateSide().getName(),
                     "pool",
                     this.init_D_ConfigStore.pool()
                  )
               );
            }

            return out;
         }

         AiOperation.Result init_AiOperation_Result_3(String message) {
            return R.init_AiOperation_Result(
               AiOperation.Status.RUNNING, "Farm (" + this.init_D_CachePool.name().toLowerCase(Locale.ROOT) + "): " + message, this.init_JsonObject_5()
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            this.init_AiOperation = null;
            return R.init_AiOperation_Result(status, message, this.init_JsonObject_5());
         }

         @Override
         public void cancel() {
            this.sync_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }
         }
      };
   }

   static String load(String message) {
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

   static String read(String id) {
      return id.substring(id.indexOf(58) + 1).replace('_', ' ');
   }

   record Inner_BufferHelper(Direction out, int length) {
   }

   enum Inner_CachePool {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push,
      pop,
      tick,
      run,
      get,
      set,
      put,
      add;
   }

   record Inner_ConfigStore(BlockPos origin, D.Inner_StreamUtil layout, Direction gateSide, boolean pool, double cost) {
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

         return ag.init_JsonObject_ObjArr(
            "minX", minX - 1, "minY", this.ground() - 3, "minZ", minZ - 1, "maxX", maxX + 1, "maxY", this.ground() + 3, "maxZ", maxZ + 1
         );
      }
   }

   record Inner_DataUtil(int top, boolean natural, boolean tillable, boolean fluid, boolean lava, boolean obstacle) {
      static final D.Inner_DataUtil NONE = new D.Inner_DataUtil(-2147483648, false, false, false, false, true);
   }

   record Inner_StreamUtil(int width, int length, List<int[]> water) {
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
