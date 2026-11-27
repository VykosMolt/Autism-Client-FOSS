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

final class AiFarmPlant {
   static final Set<String> TILLABLE_BLOCKS = Set.of("minecraft:grass_block", "minecraft:dirt", "minecraft:dirt_path");
   static final int init_I = 4;
   static final int load = 128;
   static final int read = 15;

   private AiFarmPlant() {
   }

   static boolean init(String blockId) {
      return TILLABLE_BLOCKS.contains(blockId);
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

      String substrate = AiFarmArea.substrateFor(args, cropBlock);
      final boolean soulSand = AiFarmArea.isSoulSand(substrate);
      final int width = Math.max(0, Math.min(15, AiJson.getInt(args, "width", 0)));
      final int length = Math.max(0, Math.min(15, AiJson.getInt(args, "length", 0)));
      final int requested = Math.max(1, Math.min(128, AiJson.getInt(args, "count", width > 0 && length > 0 ? width * length : 9)));
      final int radius = Math.max(4, Math.min(32, AiJson.getInt(args, "radius", 16)));
      final int seeds = AiGameState.countInventoryItem(seedItem);
      if (seeds <= 0) {
         throw new IllegalArgumentException("No " + seedItem + " to plant; obtain some first (missing " + seedItem + ": " + requested + ")");
      }

      if (!soulSand && AiGameState.countInventoryItem("#minecraft:hoes") <= 0 && !init(client)) {
         throw new IllegalArgumentException("A hoe is needed to till; craft one first (missing minecraft:wooden_hoe: 1)");
      }

      final int plots = Math.min(requested, seeds);
      return new AiOperation() {
         final Minecraft mc = Minecraft.getInstance();
         final long deadline = System.nanoTime() + Math.max(10, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 600))) * 1000000000L;
         final List<JsonObject> skipped = new ArrayList<>();
         List<BlockPos> candidates;
         BlockPos water;
         BlockPos ice;
         int planted;
         int tilled;
         int cleared;
         AiOperation breakOp;
         AiOperation fieldWork;
         List<BlockPos> plotCells;
         int write;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && this.mc.player != null && this.mc.level != null) {
               if (System.nanoTime() > this.deadline) {
                  return this.init(this.planted > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.TIMED_OUT, "Planting ran out of time");
               }

               if (this.candidates == null) {
                  this.layOut();
               }

               if (this.breakOp != null) {
                  AiOperation.Result r = this.breakOp.tick();
                  if (!r.terminal()) {
                     return this.init(this.read());
                  }

                  this.breakOp = null;
                  return this.plotCells != null ? this.init(this.read()) : this.init(r);
               } else {
                  if (this.ice != null) {
                     this.breakOp = AiGameOperations.blockAction("break", AiFarmPlant.load(this.ice));
                     return this.init("Breaking ice at " + this.ice.toShortString() + " to open water");
                  }

                  if (this.plotCells == null) {
                     this.plotCells = new ArrayList<>();

                     for (BlockPos c : this.candidates) {
                        if (this.plotCells.size() < plots) {
                           this.plotCells.add(c);
                        }
                     }
                  }

                  if (this.plotCells.isEmpty()) {
                     return this.init(AiOperation.Status.FAILED, "No tillable ground (grass, dirt or path with open air above) within " + radius + " blocks");
                  }

                  while (this.write < this.plotCells.size()) {
                     BlockPos c = this.plotCells.get(this.write);
                     if (AiFarmPlant.init(this.mc.level.getBlockState(c.above()))) {
                        this.breakOp = AiGameOperations.blockAction("break", AiFarmPlant.load(c.above()));
                        this.write++;
                        this.cleared++;
                        return this.init("Clearing " + c.above().toShortString());
                     }

                     this.write++;
                  }

                  if (this.fieldWork == null) {
                     int minX = 2147483647;
                     int minZ = 2147483647;
                     int maxX = -2147483648;
                     int maxZ = -2147483648;
                     int y = this.plotCells.getFirst().getY();
                     List<BlockPos> level = new ArrayList<>();

                     for (BlockPos c : this.plotCells) {
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

                     this.fieldWork = AiFieldWork.init(level, new BlockPos(minX, y, minZ), maxX - minX + 1, maxZ - minZ + 1, cropBlock, seedItem);
                  }

                  AiOperation.Result w = this.fieldWork.tick();
                  JsonObject data = AiJson.parseObject(w.dataJson());
                  if (data.has("sown")) {
                     this.planted = data.get("sown").getAsInt();
                  }

                  if (data.has("tilled")) {
                     this.tilled = data.get("tilled").getAsInt();
                  }

                  if (!w.terminal()) {
                     return this.init(w.message());
                  } else {
                     return this.planted == 0 && w.status() != AiOperation.Status.SUCCEEDED
                        ? this.init(w.status(), "No cell could be tilled and sown: " + w.message())
                        : this.init(
                           AiOperation.Status.SUCCEEDED,
                           "Planted "
                              + this.planted
                              + " "
                              + this.load()
                              + (this.planted < requested ? " (asked for " + requested + ": " + this.shortfallReason() + ")" : "")
                        );
                  }
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Planting cancelled");
            }
         }

         AiOperation.Result init(AiOperation.Result r) {
            if (this.ice != null) {
               BlockPos opened = this.ice;
               this.ice = null;
               if (this.mc.level.getBlockState(opened).is(Blocks.WATER)) {
                  this.water = opened;
                  this.candidates = this.init(opened);
               }

               return this.init("Water opened; laying out the field");
            } else {
               return this.init(this.read());
            }
         }

         void layOut() {
            BlockPos feet = autismclient.ai.engine.AiStance.init(this.mc);
            if (soulSand) {
               this.candidates = this.init(feet);
            } else {
               this.water = this.init(feet, s -> s.is(Blocks.WATER) && s.getFluidState().isSource());
               if (this.water == null) {
                  BlockPos frozen = this.init(feet, s -> s.is(Blocks.ICE));
                  if (frozen != null && !this.mc.level.getBlockState(frozen.below()).isAir()) {
                     this.ice = frozen;
                  }
               }

               this.candidates = this.init(this.water != null ? this.water : (this.ice != null ? this.ice : feet));
            }
         }

         List<BlockPos> init(BlockPos anchor) {
            boolean rectangle = width > 0 && length > 0;
            boolean hydrate = !soulSand && !rectangle && (this.water != null || this.ice != null);
            List<BlockPos> out = new ArrayList<>();
            int span = hydrate ? 4 : Math.min(radius, 6);
            int fromX = rectangle ? -(width - 1) / 2 : -span;
            int toX = rectangle ? width / 2 : span;
            int fromZ = rectangle ? -(length - 1) / 2 : -span;
            int toZ = rectangle ? length / 2 : span;
            if (rectangle && (this.water != null || this.ice != null) && width % 2 == 1 && length % 2 == 1 && width * length - 1 < requested) {
               toX++;
               toZ++;
            }

            for (int dx = fromX; dx <= toX; dx++) {
               for (int dz = fromZ; dz <= toZ; dz++) {
                  for (int dy = -2; dy <= 1; dy++) {
                     BlockPos p = anchor.offset(dx, dy, dz);
                     if (AiTerrain.isLoaded(this.mc.level, p)) {
                        BlockState ground = this.mc.level.getBlockState(p);
                        BlockState above = this.mc.level.getBlockState(p.above());
                        boolean farmland = soulSand ? ground.is(Blocks.SOUL_SAND) : ground.is(Blocks.FARMLAND);
                        if ((farmland || !soulSand && AiFarmPlant.init(BuiltInRegistries.BLOCK.getKey(ground.getBlock()).toString()))
                           && (above.isAir() || AiFarmPlant.init(above))
                           && (!hydrate || AiFarmPlant.init(p, anchor))) {
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
                  .thenComparingDouble(px -> px.distSqr(this.mc.player.blockPosition()))
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
                     if (AiTerrain.isLoaded(this.mc.level, p) && match.test(this.mc.level.getBlockState(p))) {
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

         String shortfallReason() {
            return seeds < requested
               ? "only " + seeds + " " + seedItem + " in the bag"
               : "only " + this.planted + " cells could be prepared" + (this.water == null ? " (no water nearby, the field is dry)" : "");
         }

         String load() {
            return this.planted == 1 ? "crop" : "crops";
         }

         String read() {
            return "Farming: " + this.planted + "/" + plots + " planted";
         }

         JsonObject progressJson() {
            JsonArray skips = new JsonArray();
            this.skipped.forEach(skips::add);
            JsonObject out = AiJson.objectOf(
               "planted",
               this.planted,
               "tilled",
               this.tilled,
               "cleared",
               this.cleared,
               "requested",
               requested,
               "seedsLeft",
               AiGameState.countInventoryItem(seedItem),
               "hydrated",
               this.water != null,
               "skipped",
               skips
            );
            if (this.water != null) {
               out.add("water", AiJson.objectOf("x", this.water.getX(), "y", this.water.getY(), "z", this.water.getZ()));
            }

            return out;
         }

         AiOperation.Result init(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return AiGameOperations.result(status, message, this.progressJson());
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.breakOp != null) {
               this.breakOp.cancel();
            }

            if (this.fieldWork != null) {
               this.fieldWork.cancel();
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
      return AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ());
   }

   private static JsonObject load(BlockPos p) {
      JsonObject out = init(p);
      out.addProperty("requireDrops", false);
      return out;
   }
}
