package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class F {
   static final Set<String> init_Set = Set.of("minecraft:grass_block", "minecraft:dirt", "minecraft:dirt_path");
   static final int init_I = 4;
   static final int load = 128;
   static final int read = 15;

   private F() {
   }

   static boolean init(String blockId) {
      return init_Set.contains(blockId);
   }

   static boolean init(BlockPos ground, BlockPos water) {
      int dy = water.getY() - ground.getY();
      return (dy == 0 || dy == 1) && Math.abs(water.getX() - ground.getX()) <= 4 && Math.abs(water.getZ() - ground.getZ()) <= 4;
   }

   static boolean init(BlockState state) {
      return !state.isAir() && state.getFluidState().isEmpty() ? state.is(Blocks.SNOW) || state.canBeReplaced() || state.is(BlockTags.SMALL_FLOWERS) : false;
   }

   static AiOperation init(final JsonObject args, final String cropBlock, final String seedItem) {
      Minecraft client = Minecraft.getInstance();
      if (client.player == null || client.level == null) {
         throw new IllegalArgumentException("Farming requires a connected living player");
      }

      if (seedItem == null) {
         throw new IllegalArgumentException(cropBlock + " is not planted from a seed item; plant wheat, carrots, potatoes, beetroots or nether wart");
      }

      String substrate = C.init_Str(args, cropBlock);
      final boolean soulSand = C.init_Z(substrate);
      final int width = Math.max(0, Math.min(15, ag.init_I_JsonObject(args, "width", 0)));
      final int length = Math.max(0, Math.min(15, ag.init_I_JsonObject(args, "length", 0)));
      final int requested = Math.max(1, Math.min(128, ag.init_I_JsonObject(args, "count", width > 0 && length > 0 ? width * length : 9)));
      final int radius = Math.max(4, Math.min(32, ag.init_I_JsonObject(args, "radius", 16)));
      final int seeds = S.init_I_Str(seedItem);
      if (seeds <= 0) {
         throw new IllegalArgumentException("No " + seedItem + " to plant; obtain some first (missing " + seedItem + ": " + requested + ")");
      }

      if (!soulSand && S.init_I_Str("#minecraft:hoes") <= 0 && !init(client)) {
         throw new IllegalArgumentException("A hoe is needed to till; craft one first (missing minecraft:wooden_hoe: 1)");
      }

      final int plots = Math.min(requested, seeds);
      return new AiOperation() {
         final Minecraft init_Minecraft = Minecraft.getInstance();
         final long init_J = System.nanoTime() + Math.max(10, Math.min(1800, ag.init_I_JsonObject(args, "timeoutSeconds", 600))) * 1000000000L;
         final List<JsonObject> init_List = new ArrayList<>();
         List<BlockPos> load_List;
         BlockPos init_BlockPos;
         BlockPos load_BlockPos;
         int init_I;
         int load_I;
         int read_I;
         AiOperation init_AiOperation;
         AiOperation load_AiOperation;
         List<BlockPos> read_List;
         int write;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(this.init_I > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.TIMED_OUT, "Planting ran out of time");
               }

               if (this.load_List == null) {
                  this.init_V_3();
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init(this.read());
                  }

                  this.init_AiOperation = null;
                  return this.read_List != null ? this.init(this.read()) : this.init(r);
               } else {
                  if (this.load_BlockPos != null) {
                     this.init_AiOperation = R.init("break", F.load(this.load_BlockPos));
                     return this.init("Breaking ice at " + this.load_BlockPos.toShortString() + " to open water");
                  }

                  if (this.read_List == null) {
                     this.read_List = new ArrayList<>();

                     for (BlockPos c : this.load_List) {
                        if (this.read_List.size() < plots) {
                           this.read_List.add(c);
                        }
                     }
                  }

                  if (this.read_List.isEmpty()) {
                     return this.init(AiOperation.Status.FAILED, "No tillable ground (grass, dirt or path with open air above) within " + radius + " blocks");
                  }

                  while (this.write < this.read_List.size()) {
                     BlockPos c = this.read_List.get(this.write);
                     if (F.init(this.init_Minecraft.level.getBlockState(c.above()))) {
                        this.init_AiOperation = R.init("break", F.load(c.above()));
                        this.write++;
                        this.read_I++;
                        return this.init("Clearing " + c.above().toShortString());
                     }

                     this.write++;
                  }

                  if (this.load_AiOperation == null) {
                     int minX = 2147483647;
                     int minZ = 2147483647;
                     int maxX = -2147483648;
                     int maxZ = -2147483648;
                     int y = this.read_List.getFirst().getY();
                     List<BlockPos> level = new ArrayList<>();

                     for (BlockPos c : this.read_List) {
                        if (c.getY() == y) {
                           level.add(c);
                        }
                     }

                     for (BlockPos c : level) {
                        minX = Math.min(minX, c.getX());
                        minZ = Math.min(minZ, c.getZ());
                        maxX = Math.max(maxX, c.getX());
                        maxZ = Math.max(maxZ, c.getZ());
                     }

                     this.load_AiOperation = I.init(level, new BlockPos(minX, y, minZ), maxX - minX + 1, maxZ - minZ + 1, cropBlock, seedItem);
                  }

                  AiOperation.Result w = this.load_AiOperation.tick();
                  JsonObject data = ag.init_JsonObject_Str(w.dataJson());
                  if (data.has("sown")) {
                     this.init_I = data.get("sown").getAsInt();
                  }

                  if (data.has("tilled")) {
                     this.load_I = data.get("tilled").getAsInt();
                  }

                  if (!w.terminal()) {
                     return this.init(w.message());
                  } else {
                     return this.init_I == 0 && w.status() != AiOperation.Status.SUCCEEDED
                        ? this.init(w.status(), "No cell could be tilled and sown: " + w.message())
                        : this.init(
                           AiOperation.Status.SUCCEEDED,
                           "Planted "
                              + this.init_I
                              + " "
                              + this.load()
                              + (this.init_I < requested ? " (asked for " + requested + ": " + this.init_Str_4() + ")" : "")
                        );
                  }
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Planting cancelled");
            }
         }

         AiOperation.Result init(AiOperation.Result r) {
            if (this.load_BlockPos != null) {
               BlockPos opened = this.load_BlockPos;
               this.load_BlockPos = null;
               if (this.init_Minecraft.level.getBlockState(opened).is(Blocks.WATER)) {
                  this.init_BlockPos = opened;
                  this.load_List = this.init(opened);
               }

               return this.init("Water opened; laying out the field");
            } else {
               return this.init(this.read());
            }
         }

         void init_V_3() {
            BlockPos feet = autismclient.ai.a.i.init(this.init_Minecraft);
            if (soulSand) {
               this.load_List = this.init(feet);
            } else {
               this.init_BlockPos = this.init(feet, s -> s.is(Blocks.WATER) && s.getFluidState().isSource());
               if (this.init_BlockPos == null) {
                  BlockPos frozen = this.init(feet, s -> s.is(Blocks.ICE));
                  if (frozen != null && !this.init_Minecraft.level.getBlockState(frozen.below()).isAir()) {
                     this.load_BlockPos = frozen;
                  }
               }

               this.load_List = this.init(this.init_BlockPos != null ? this.init_BlockPos : (this.load_BlockPos != null ? this.load_BlockPos : feet));
            }
         }

         List<BlockPos> init(BlockPos anchor) {
            boolean rectangle = width > 0 && length > 0;
            boolean hydrate = !soulSand && !rectangle && (this.init_BlockPos != null || this.load_BlockPos != null);
            List<BlockPos> out = new ArrayList<>();
            int span = hydrate ? 4 : Math.min(radius, 6);
            int fromX = rectangle ? -(width - 1) / 2 : -span;
            int toX = rectangle ? width / 2 : span;
            int fromZ = rectangle ? -(length - 1) / 2 : -span;
            int toZ = rectangle ? length / 2 : span;
            if (rectangle && (this.init_BlockPos != null || this.load_BlockPos != null) && width % 2 == 1 && length % 2 == 1 && width * length - 1 < requested) {
               toX++;
               toZ++;
            }

            for (int dx = fromX; dx <= toX; dx++) {
               for (int dz = fromZ; dz <= toZ; dz++) {
                  for (int dy = -2; dy <= 1; dy++) {
                     BlockPos p = anchor.offset(dx, dy, dz);
                     if (cu.init(this.init_Minecraft.level, p)) {
                        BlockState ground = this.init_Minecraft.level.getBlockState(p);
                        BlockState above = this.init_Minecraft.level.getBlockState(p.above());
                        boolean farmland = soulSand ? ground.is(Blocks.SOUL_SAND) : ground.is(Blocks.FARMLAND);
                        if ((farmland || !soulSand && F.init(BuiltInRegistries.BLOCK.getKey(ground.getBlock()).toString()))
                           && (above.isAir() || F.init(above))
                           && (!hydrate || F.init(p, anchor))) {
                           out.add(p.immutable());
                        }
                     }
                  }
               }
            }

            int level = hydrate ? anchor.getY() : anchor.getY() - 1;
            out.sort(
               Comparator.<BlockPos>comparingInt(px -> Math.abs(px.getY() - level) > 0 ? 1 : 0)
                  .thenComparingInt(px -> Math.max(Math.abs(px.getX() - anchor.getX()), Math.abs(px.getZ() - anchor.getZ())))
                  .thenComparingDouble(px -> px.distSqr(this.init_Minecraft.player.blockPosition()))
            );
            return out;
         }

         BlockPos init(BlockPos from, Predicate<BlockState> match) {
            BlockPos best = null;
            double bestSq = 1.7976931348623157E308;
            MutableBlockPos p = new MutableBlockPos();

            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  for (int dy = -4; dy <= 3; dy++) {
                     p.set(from.getX() + dx, from.getY() + dy, from.getZ() + dz);
                     if (cu.init(this.init_Minecraft.level, p) && match.test(this.init_Minecraft.level.getBlockState(p))) {
                        double sq = p.distSqr(from);
                        if (sq < bestSq) {
                           bestSq = sq;
                           best = p.immutable();
                        }
                     }
                  }
               }
            }

            return best;
         }

         String init_Str_4() {
            return seeds < requested
               ? "only " + seeds + " " + seedItem + " in the bag"
               : "only " + this.init_I + " cells could be prepared" + (this.init_BlockPos == null ? " (no water nearby, the field is dry)" : "");
         }

         String load() {
            return this.init_I == 1 ? "crop" : "crops";
         }

         String read() {
            return "Farming: " + this.init_I + "/" + plots + " planted";
         }

         JsonObject init_JsonObject_6() {
            JsonArray skips = new JsonArray();
            this.init_List.forEach(skips::add);
            JsonObject out = ag.init_JsonObject_ObjArr(
               "planted",
               this.init_I,
               "tilled",
               this.load_I,
               "cleared",
               this.read_I,
               "requested",
               requested,
               "seedsLeft",
               S.init_I_Str(seedItem),
               "hydrated",
               this.init_BlockPos != null,
               "skipped",
               skips
            );
            if (this.init_BlockPos != null) {
               out.add("water", ag.init_JsonObject_ObjArr("x", this.init_BlockPos.getX(), "y", this.init_BlockPos.getY(), "z", this.init_BlockPos.getZ()));
            }

            return out;
         }

         AiOperation.Result init(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_6());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return R.init_AiOperation_Result(status, message, this.init_JsonObject_6());
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            if (this.load_AiOperation != null) {
               this.load_AiOperation.cancel();
            }
         }
      };
   }

   static boolean init(Minecraft mc) {
      Inventory inventory = mc.player.getInventory();

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         if (inventory.getItem(i).is(ItemTags.HOES)) {
            return true;
         }
      }

      return false;
   }

   private static JsonObject init(BlockPos p) {
      return ag.init_JsonObject_ObjArr("x", p.getX(), "y", p.getY(), "z", p.getZ());
   }

   private static JsonObject load(BlockPos p) {
      JsonObject out = init(p);
      out.addProperty("requireDrops", false);
      return out;
   }
}
