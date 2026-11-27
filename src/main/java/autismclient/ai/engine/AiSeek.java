package autismclient.ai.engine;

import autismclient.ai.AiFailure;
import autismclient.ai.AiJson;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class AiSeek {
   private static final int init_I = 256;
   private static final int load = 8;
   private static final int read = 56;
   private static final int write = 12;
   private static final int update = 64;
   private static final int[][] NEIGHBOR_OFFSETS = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
   private static final ItemStack[] EMPTY_HOTBAR = new ItemStack[9];

   private AiSeek() {
   }

   public static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         int timeoutSeconds = Math.max(5, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 180)));
         return new AiSeek.Inner_ConfigStore(timeoutSeconds, AiPathfinder.RouteStrategy.parseCompat(AiJson.getString(args, "strategy", "fastest")));
      } else {
         throw new IllegalArgumentException("Surface travel requires a connected living player");
      }
   }

   public static AiOperation load(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         int timeoutSeconds = Math.max(5, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 180)));
         Integer maxY = args.has("maxY") ? AiJson.getInt(args, "maxY", 0) : null;
         return new AiSeek.Inner_StreamUtil(timeoutSeconds, AiPathfinder.RouteStrategy.parseCompat(AiJson.getString(args, "strategy", "fastest")), maxY);
      } else {
         throw new IllegalArgumentException("Cave travel requires a connected living player");
      }
   }

   static int init(AiSeek.Inner_DataUtil air, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int sx, int sy, int sz, int cap) {
      if (sx >= minX && sx <= maxX && sy >= minY && sy <= maxY && sz >= minZ && sz <= maxZ && air.test(sx, sy, sz)) {
         ArrayDeque<BlockPos> queue = new ArrayDeque<>();
         HashSet<Long> visited = new HashSet<>();
         queue.add(new BlockPos(sx, sy, sz));
         int count = 0;

         while (!queue.isEmpty() && count < cap) {
            BlockPos cell = queue.poll();
            if (visited.add(cell.asLong())) {
               count++;

               for (int[] d : NEIGHBOR_OFFSETS) {
                  int nx = cell.getX() + d[0];
                  int ny = cell.getY() + d[1];
                  int nz = cell.getZ() + d[2];
                  if (nx >= minX && nx <= maxX && ny >= minY && ny <= maxY && nz >= minZ && nz <= maxZ) {
                     BlockPos next = new BlockPos(nx, ny, nz);
                     if (!visited.contains(next.asLong()) && air.test(nx, ny, nz)) {
                        queue.add(next);
                     }
                  }
               }
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   private static AiPathfinder.CellInfo classifyCell(Minecraft mc, BlockPos p) {
      return AiPathfinder.LiveGridView.classifyCellCompat(mc, mc.level, p, EMPTY_HOTBAR);
   }

   private static boolean isStandableAt(Minecraft mc, BlockPos feet) {
      return AiPathfinder.MoveGenerator.isStandableCompat(
         classifyCell(mc, feet), classifyCell(mc, feet.above()), classifyCell(mc, feet.below())
      );
   }

   private abstract static class Inner_BufferHelper implements AiOperation {
      final Minecraft client = Minecraft.getInstance();
      final long deadlineNanos;
      final AiPathfinder.RouteStrategy strategy;
      final AiSeek.Inner_CachePool columnSearch;
      final int columnsPerTick;
      BlockPos candidate;
      AiOperation travel;
      int load;
      int read;
      boolean init_Z;
      static final int write = 3;

      Inner_BufferHelper(int timeoutSeconds, AiPathfinder.RouteStrategy strategy, int maxRadius, int columnsPerTick) {
         this.deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
         this.strategy = strategy;
         this.columnSearch = new AiSeek.Inner_CachePool(this.client.player.blockPosition(), maxRadius);
         this.columnsPerTick = columnsPerTick;
      }

      abstract BlockPos init(BlockPos var1);

      boolean isNearSurface() {
         return false;
      }

      abstract String travelLabel();

      abstract String load();

      abstract String read();

      @Override
      public AiOperation.Result tick() {
         if (!this.init_Z && this.client.player != null && this.client.level != null) {
            if (System.nanoTime() > this.deadlineNanos) {
               return this.init(AiOperation.Status.TIMED_OUT, this.travelLabel() + " deadline reached");
            }

            if (this.client.player.getHealth() <= 4.0F && !AiLowHealth.isOverrideActive()) {
               return this.init(
                  AiOperation.Status.FAILED,
                  AiFailure.LOW_HEALTH,
                  AiLowHealth.init("travel", this.client.player.getHealth(), this.client.player.getMaxHealth())
               );
            }

            if (this.candidate == null) {
               for (int budget = 0; budget < this.columnsPerTick && this.candidate == null; budget++) {
                  BlockPos column = this.columnSearch.nextColumn();
                  if (column == null) {
                     return this.init(AiOperation.Status.FAILED, AiFailure.UNREACHABLE, this.load());
                  }

                  this.candidate = this.init(column);
               }

               if (this.candidate == null) {
                  return this.init("Scanning (radius " + this.columnSearch.currentRadius() + ")");
               }
            }

            if (this.travel != null) {
               AiOperation.Result r = this.travel.tick();
               if (!r.terminal()) {
                  return this.init("Travelling to " + this.candidate.toShortString());
               }

               this.travel.cancel();
               this.travel = null;
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  return this.init(AiOperation.Status.SUCCEEDED, this.read());
               }

               if (r.status() == AiOperation.Status.TIMED_OUT) {
                  return this.init(AiOperation.Status.TIMED_OUT, r.message());
               }

               if (++this.load > 1) {
                  if (++this.read < 3) {
                     this.candidate = null;
                     this.load = 0;
                     return this.init("No way to that column; scanning on");
                  } else {
                     return this.init(
                        AiOperation.Status.FAILED, AiFailure.UNREACHABLE, this.candidate.toShortString() + " unreachable even with digging (" + r.message() + ")"
                     );
                  }
               } else {
                  return this.init("Walk-only route failed; retrying with digging allowed");
               }
            } else {
               this.travel = AiGoto.travelToCompat(
                  this.candidate, 2.0, this.load > 0 || this.isNearSurface(), false, false, this.remainingSeconds(), this.strategy
               );
               return this.init(this.load == 0 ? "Travelling to " + this.candidate.toShortString() : "Retrying with digging allowed");
            }
         } else {
            return this.init(AiOperation.Status.CANCELLED, this.travelLabel() + " cancelled");
         }
      }

      int remainingSeconds() {
         return (int)Math.max(1L, (this.deadlineNanos - System.nanoTime()) / 1000000000L);
      }

      AiOperation.Result init(String message) {
         return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressData().toString(), List.of());
      }

      AiOperation.Result init(AiOperation.Status status, String message) {
         this.cancel();
         return new AiOperation.Result(status, message, this.progressData().toString(), List.of());
      }

      AiOperation.Result init(AiOperation.Status status, AiFailure code, String message) {
         this.cancel();
         return AiOperation.Result.failed(code, message, this.progressData().toString());
      }

      JsonObject progressData() {
         JsonObject out = new JsonObject();
         if (this.candidate != null) {
            out.add("candidate", AiJson.objectOf("x", this.candidate.getX(), "y", this.candidate.getY(), "z", this.candidate.getZ()));
         }

         out.addProperty("attempt", this.load + 1);
         return out;
      }

      @Override
      public void cancel() {
         if (!this.init_Z) {
            this.init_Z = true;
            if (this.travel != null) {
               this.travel.cancel();
               this.travel = null;
            }
         }
      }
   }

   private static final class Inner_CachePool {
      final BlockPos origin;
      private final int maxRadius;
      private List<BlockPos> ring = List.of();
      private int load;
      private int read = 24;
      private boolean first = true;

      Inner_CachePool(BlockPos origin, int maxRadius) {
         this.origin = origin;
         this.maxRadius = maxRadius;
      }

      BlockPos nextColumn() {
         if (this.first) {
            this.first = false;
            return this.origin;
         }

         if (this.load >= this.ring.size()) {
            if (this.read > this.maxRadius) {
               return null;
            }

            this.ring = init(this.origin, this.read);
            this.read += 24;
            this.load = 0;
         }

         return this.ring.get(this.load++);
      }

      int currentRadius() {
         return Math.min(this.read, this.maxRadius);
      }

      private static List<BlockPos> init(BlockPos origin, int radius) {
         List<int[]> ring = new ArrayList<>();
         int inner = radius - 24;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) > inner) {
                  ring.add(new int[]{dx, dz});
               }
            }
         }

         ring.sort(Comparator.comparingInt(cx -> cx[0] * cx[0] + cx[1] * cx[1]));
         List<BlockPos> out = new ArrayList<>(ring.size());

         for (int[] c : ring) {
            out.add(origin.offset(c[0], 0, c[1]));
         }

         return out;
      }
   }

   private static final class Inner_ConfigStore extends AiSeek.Inner_BufferHelper {
      static final int update = 12;
      static final int sync = 16;
      final boolean nearSurface;

      Inner_ConfigStore(int timeoutSeconds, AiPathfinder.RouteStrategy strategy) {
         super(timeoutSeconds, strategy, 96, 256);
         BlockPos origin = this.columnSearch.origin;
         this.nearSurface = this.client.level.getHeight(Types.MOTION_BLOCKING, origin.getX(), origin.getZ()) - origin.getY() <= 12;
      }

      @Override
      BlockPos init(BlockPos column) {
         int topY = this.client.level.getHeight(Types.MOTION_BLOCKING, column.getX(), column.getZ());
         BlockPos feet = new BlockPos(column.getX(), topY, column.getZ());
         if (!AiSeek.isStandableAt(this.client, feet)) {
            return null;
         } else {
            return this.nearSurface && topY > this.columnSearch.origin.getY() + 3 && this.columnSearch.currentRadius() <= 16 ? null : feet;
         }
      }

      @Override
      boolean isNearSurface() {
         BlockPos origin = this.columnSearch.origin;
         return this.client.level.getHeight(Types.MOTION_BLOCKING, origin.getX(), origin.getZ()) - origin.getY() > 8;
      }

      @Override
      String travelLabel() {
         return "Surface travel";
      }

      @Override
      String load() {
         return "No open-sky column within 96 blocks";
      }

      @Override
      String read() {
         return "Arrived under open sky";
      }
   }

   interface Inner_DataUtil {
      boolean test(int var1, int var2, int var3);
   }

   private static final class Inner_StreamUtil extends AiSeek.Inner_BufferHelper {
      private final int update;

      Inner_StreamUtil(int timeoutSeconds, AiPathfinder.RouteStrategy strategy, Integer maxY) {
         super(timeoutSeconds, strategy, 64, 8);
         this.update = maxY != null ? maxY : this.columnSearch.origin.getY() + 8;
      }

      @Override
      BlockPos init(BlockPos column) {
         int topY = this.client.level.getHeight(Types.MOTION_BLOCKING, column.getX(), column.getZ());
         int yTop = Math.min(this.update, topY - 2);
         int yBottom = Math.max(this.client.level.getMinY(), yTop - 56);

         for (int y = yTop; y >= yBottom; y--) {
            BlockPos cell = new BlockPos(column.getX(), y, column.getZ());
            AiPathfinder.CellInfo feet = AiSeek.classifyCell(this.client, cell);
            if (feet.loaded()
               && feet.passable()
               && !feet.water()
               && !feet.lava()
               && !feet.hazardFeet()
               && AiSeek.classifyCell(this.client, cell.above()).passable()) {
               AiPathfinder.CellInfo floor = AiSeek.classifyCell(this.client, cell.below());
               if (!floor.passable()
                  && !floor.hazardFloor()
                  && !floor.lava()
                  && AiSeek.init(
                        this::init,
                        column.getX() - 8,
                        yBottom,
                        column.getZ() - 8,
                        column.getX() + 8,
                        yTop + 2,
                        column.getZ() + 8,
                        cell.getX(),
                        y,
                        cell.getZ(),
                        64
                     )
                     >= 12) {
                  return cell;
               }
            }
         }

         return null;
      }

      boolean init(int x, int y, int z) {
         AiPathfinder.CellInfo probe = AiSeek.classifyCell(this.client, new BlockPos(x, y, z));
         return probe.loaded() && probe.passable() && !probe.water() && !probe.lava();
      }

      @Override
      String travelLabel() {
         return "Cave travel";
      }

      @Override
      String load() {
         return "No cave air pocket within 64 blocks";
      }

      @Override
      String read() {
         return "Arrived in a cave air pocket";
      }
   }
}
