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

   public static AiOperation surfaceSeek(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         int timeoutSeconds = Math.max(5, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 180)));
         return new AiSeek.SurfaceSeek(timeoutSeconds, AiPathfinder.RouteStrategy.parseCompat(AiJson.getString(args, "strategy", "fastest")));
      } else {
         throw new IllegalArgumentException("Surface travel requires a connected living player");
      }
   }

   public static AiOperation caveSeek(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         int timeoutSeconds = Math.max(5, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 180)));
         Integer maxY = args.has("maxY") ? AiJson.getInt(args, "maxY", 0) : null;
         return new AiSeek.CaveSeek(timeoutSeconds, AiPathfinder.RouteStrategy.parseCompat(AiJson.getString(args, "strategy", "fastest")), maxY);
      } else {
         throw new IllegalArgumentException("Cave travel requires a connected living player");
      }
   }

   static int countCells(AiSeek.CellPredicate air, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int sx, int sy, int sz, int cap) {
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

   private abstract static class ColumnSeekOperation implements AiOperation {
      final Minecraft client = Minecraft.getInstance();
      final long deadlineNanos;
      final AiPathfinder.RouteStrategy strategy;
      final AiSeek.ColumnRingSearch columnSearch;
      final int columnsPerTick;
      BlockPos candidate;
      AiOperation travel;
      int digAttempts;
      int skippedColumns;
      boolean cancelled;
      static final int write = 3;

      ColumnSeekOperation(int timeoutSeconds, AiPathfinder.RouteStrategy strategy, int maxRadius, int columnsPerTick) {
         this.deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
         this.strategy = strategy;
         this.columnSearch = new AiSeek.ColumnRingSearch(this.client.player.blockPosition(), maxRadius);
         this.columnsPerTick = columnsPerTick;
      }

      abstract BlockPos findTarget(BlockPos var1);

      boolean isNearSurface() {
         return false;
      }

      abstract String travelLabel();

      abstract String failureMessage();

      abstract String successMessage();

      @Override
      public AiOperation.Result tick() {
         if (!this.cancelled && this.client.player != null && this.client.level != null) {
            if (System.nanoTime() > this.deadlineNanos) {
               return this.finish(AiOperation.Status.TIMED_OUT, this.travelLabel() + " deadline reached");
            }

            if (this.client.player.getHealth() <= 4.0F && !AiLowHealth.isOverrideActive()) {
               return this.fail(
                  AiOperation.Status.FAILED,
                  AiFailure.LOW_HEALTH,
                  AiLowHealth.refusalMessage("travel", this.client.player.getHealth(), this.client.player.getMaxHealth())
               );
            }

            if (this.candidate == null) {
               for (int budget = 0; budget < this.columnsPerTick && this.candidate == null; budget++) {
                  BlockPos column = this.columnSearch.nextColumn();
                  if (column == null) {
                     return this.fail(AiOperation.Status.FAILED, AiFailure.UNREACHABLE, this.failureMessage());
                  }

                  this.candidate = this.findTarget(column);
               }

               if (this.candidate == null) {
                  return this.running("Scanning (radius " + this.columnSearch.currentRadius() + ")");
               }
            }

            if (this.travel != null) {
               AiOperation.Result r = this.travel.tick();
               if (!r.terminal()) {
                  return this.running("Travelling to " + this.candidate.toShortString());
               }

               this.travel.cancel();
               this.travel = null;
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  return this.finish(AiOperation.Status.SUCCEEDED, this.successMessage());
               }

               if (r.status() == AiOperation.Status.TIMED_OUT) {
                  return this.finish(AiOperation.Status.TIMED_OUT, r.message());
               }

               if (++this.digAttempts > 1) {
                  if (++this.skippedColumns < 3) {
                     this.candidate = null;
                     this.digAttempts = 0;
                     return this.running("No way to that column; scanning on");
                  } else {
                     return this.fail(
                        AiOperation.Status.FAILED, AiFailure.UNREACHABLE, this.candidate.toShortString() + " unreachable even with digging (" + r.message() + ")"
                     );
                  }
               } else {
                  return this.running("Walk-only route failed; retrying with digging allowed");
               }
            } else {
               this.travel = AiGoto.travelToCompat(
                  this.candidate, 2.0, this.digAttempts > 0 || this.isNearSurface(), false, false, this.remainingSeconds(), this.strategy
               );
               return this.running(this.digAttempts == 0 ? "Travelling to " + this.candidate.toShortString() : "Retrying with digging allowed");
            }
         } else {
            return this.finish(AiOperation.Status.CANCELLED, this.travelLabel() + " cancelled");
         }
      }

      int remainingSeconds() {
         return (int)Math.max(1L, (this.deadlineNanos - System.nanoTime()) / 1000000000L);
      }

      AiOperation.Result running(String message) {
         return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressData().toString(), List.of());
      }

      AiOperation.Result finish(AiOperation.Status status, String message) {
         this.cancel();
         return new AiOperation.Result(status, message, this.progressData().toString(), List.of());
      }

      AiOperation.Result fail(AiOperation.Status status, AiFailure code, String message) {
         this.cancel();
         return AiOperation.Result.failed(code, message, this.progressData().toString());
      }

      JsonObject progressData() {
         JsonObject out = new JsonObject();
         if (this.candidate != null) {
            out.add("candidate", AiJson.objectOf("x", this.candidate.getX(), "y", this.candidate.getY(), "z", this.candidate.getZ()));
         }

         out.addProperty("attempt", this.digAttempts + 1);
         return out;
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            if (this.travel != null) {
               this.travel.cancel();
               this.travel = null;
            }
         }
      }
   }

   private static final class ColumnRingSearch {
      final BlockPos origin;
      private final int maxRadius;
      private List<BlockPos> ring = List.of();
      private int cursor;
      private int radius = 24;
      private boolean first = true;

      ColumnRingSearch(BlockPos origin, int maxRadius) {
         this.origin = origin;
         this.maxRadius = maxRadius;
      }

      BlockPos nextColumn() {
         if (this.first) {
            this.first = false;
            return this.origin;
         }

         if (this.cursor >= this.ring.size()) {
            if (this.radius > this.maxRadius) {
               return null;
            }

            this.ring = ringColumns(this.origin, this.radius);
            this.radius += 24;
            this.cursor = 0;
         }

         return this.ring.get(this.cursor++);
      }

      int currentRadius() {
         return Math.min(this.radius, this.maxRadius);
      }

      private static List<BlockPos> ringColumns(BlockPos origin, int radius) {
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

   private static final class SurfaceSeek extends AiSeek.ColumnSeekOperation {
      static final int update = 12;
      static final int sync = 16;
      final boolean nearSurface;

      SurfaceSeek(int timeoutSeconds, AiPathfinder.RouteStrategy strategy) {
         super(timeoutSeconds, strategy, 96, 256);
         BlockPos origin = this.columnSearch.origin;
         this.nearSurface = this.client.level.getHeight(Types.MOTION_BLOCKING, origin.getX(), origin.getZ()) - origin.getY() <= 12;
      }

      @Override
      BlockPos findTarget(BlockPos column) {
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
      String failureMessage() {
         return "No open-sky column within 96 blocks";
      }

      @Override
      String successMessage() {
         return "Arrived under open sky";
      }
   }

   interface CellPredicate {
      boolean test(int var1, int var2, int var3);
   }

   private static final class CaveSeek extends AiSeek.ColumnSeekOperation {
      private final int maxY;

      CaveSeek(int timeoutSeconds, AiPathfinder.RouteStrategy strategy, Integer maxY) {
         super(timeoutSeconds, strategy, 64, 8);
         this.maxY = maxY != null ? maxY : this.columnSearch.origin.getY() + 8;
      }

      @Override
      BlockPos findTarget(BlockPos column) {
         int topY = this.client.level.getHeight(Types.MOTION_BLOCKING, column.getX(), column.getZ());
         int yTop = Math.min(this.maxY, topY - 2);
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
                  && AiSeek.countCells(
                        this::isAirCell,
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

      boolean isAirCell(int x, int y, int z) {
         AiPathfinder.CellInfo probe = AiSeek.classifyCell(this.client, new BlockPos(x, y, z));
         return probe.loaded() && probe.passable() && !probe.water() && !probe.lava();
      }

      @Override
      String travelLabel() {
         return "Cave travel";
      }

      @Override
      String failureMessage() {
         return "No cave air pocket within 64 blocks";
      }

      @Override
      String successMessage() {
         return "Arrived in a cave air pocket";
      }
   }
}
