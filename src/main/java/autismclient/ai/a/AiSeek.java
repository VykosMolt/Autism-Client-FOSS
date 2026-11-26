package autismclient.ai.a;

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
   private static final int[][] init_IArr2 = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
   private static final ItemStack[] init_ItemStackArr = new ItemStack[9];

   private AiSeek() {
   }

   public static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         int timeoutSeconds = Math.max(5, Math.min(900, AiJson.init_I_JsonObject(args, "timeoutSeconds", 180)));
         return new AiSeek.Inner_ConfigStore(timeoutSeconds, AiPathfinder.Inner_IoWorker.init(AiJson.init_Str_JsonObject(args, "strategy", "fastest")));
      } else {
         throw new IllegalArgumentException("Surface travel requires a connected living player");
      }
   }

   public static AiOperation load(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         int timeoutSeconds = Math.max(5, Math.min(900, AiJson.init_I_JsonObject(args, "timeoutSeconds", 180)));
         Integer maxY = args.has("maxY") ? AiJson.init_I_JsonObject(args, "maxY", 0) : null;
         return new AiSeek.Inner_StreamUtil(timeoutSeconds, AiPathfinder.Inner_IoWorker.init(AiJson.init_Str_JsonObject(args, "strategy", "fastest")), maxY);
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

               for (int[] d : init_IArr2) {
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

   private static AiPathfinder.Inner_FileCache init_ClassCache_FileCac(Minecraft mc, BlockPos p) {
      return AiPathfinder.Inner_StreamUtil.init_ClassCache_FileCac_Minecraft(mc, mc.level, p, init_ItemStackArr);
   }

   private static boolean init_Z(Minecraft mc, BlockPos feet) {
      return AiPathfinder.Inner_RenderState.init_Z_ClassCache_FileCac(
         init_ClassCache_FileCac(mc, feet), init_ClassCache_FileCac(mc, feet.above()), init_ClassCache_FileCac(mc, feet.below())
      );
   }

   private abstract static class Inner_BufferHelper implements AiOperation {
      final Minecraft init_Minecraft = Minecraft.getInstance();
      final long init_J;
      final AiPathfinder.Inner_IoWorker init_ClassCache_IoWorke;
      final AiSeek.Inner_CachePool init_d_CachePool;
      final int init_I;
      BlockPos init_BlockPos;
      AiOperation init_AiOperation;
      int load;
      int read;
      boolean init_Z;
      static final int write = 3;

      Inner_BufferHelper(int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy, int maxRadius, int columnsPerTick) {
         this.init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         this.init_ClassCache_IoWorke = strategy;
         this.init_d_CachePool = new AiSeek.Inner_CachePool(this.init_Minecraft.player.blockPosition(), maxRadius);
         this.init_I = columnsPerTick;
      }

      abstract BlockPos init(BlockPos var1);

      boolean init_Z_14() {
         return false;
      }

      abstract String init_Str_10();

      abstract String load();

      abstract String read();

      @Override
      public AiOperation.Result tick() {
         if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
            if (System.nanoTime() > this.init_J) {
               return this.init(AiOperation.Status.TIMED_OUT, this.init_Str_10() + " deadline reached");
            }

            if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiLowHealth.init_Z()) {
               return this.init(
                  AiOperation.Status.FAILED,
                  AiFailure.start,
                  AiLowHealth.init("travel", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth())
               );
            }

            if (this.init_BlockPos == null) {
               for (int budget = 0; budget < this.init_I && this.init_BlockPos == null; budget++) {
                  BlockPos column = this.init_d_CachePool.init_BlockPos();
                  if (column == null) {
                     return this.init(AiOperation.Status.FAILED, AiFailure.close, this.load());
                  }

                  this.init_BlockPos = this.init(column);
               }

               if (this.init_BlockPos == null) {
                  return this.init("Scanning (radius " + this.init_d_CachePool.init_I() + ")");
               }
            }

            if (this.init_AiOperation != null) {
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init("Travelling to " + this.init_BlockPos.toShortString());
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  return this.init(AiOperation.Status.SUCCEEDED, this.read());
               }

               if (r.status() == AiOperation.Status.TIMED_OUT) {
                  return this.init(AiOperation.Status.TIMED_OUT, r.message());
               }

               if (++this.load > 1) {
                  if (++this.read < 3) {
                     this.init_BlockPos = null;
                     this.load = 0;
                     return this.init("No way to that column; scanning on");
                  } else {
                     return this.init(
                        AiOperation.Status.FAILED, AiFailure.close, this.init_BlockPos.toShortString() + " unreachable even with digging (" + r.message() + ")"
                     );
                  }
               } else {
                  return this.init("Walk-only route failed; retrying with digging allowed");
               }
            } else {
               this.init_AiOperation = AiGoto.init(
                  this.init_BlockPos, 2.0, this.load > 0 || this.init_Z_14(), false, false, this.init_I_16(), this.init_ClassCache_IoWorke
               );
               return this.init(this.load == 0 ? "Travelling to " + this.init_BlockPos.toShortString() : "Retrying with digging allowed");
            }
         } else {
            return this.init(AiOperation.Status.CANCELLED, this.init_Str_10() + " cancelled");
         }
      }

      int init_I_16() {
         return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
      }

      AiOperation.Result init(String message) {
         return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_29().toString(), List.of());
      }

      AiOperation.Result init(AiOperation.Status status, String message) {
         this.cancel();
         return new AiOperation.Result(status, message, this.init_JsonObject_29().toString(), List.of());
      }

      AiOperation.Result init(AiOperation.Status status, AiFailure code, String message) {
         this.cancel();
         return AiOperation.Result.failed(code, message, this.init_JsonObject_29().toString());
      }

      JsonObject init_JsonObject_29() {
         JsonObject out = new JsonObject();
         if (this.init_BlockPos != null) {
            out.add("candidate", AiJson.init_JsonObject_ObjArr("x", this.init_BlockPos.getX(), "y", this.init_BlockPos.getY(), "z", this.init_BlockPos.getZ()));
         }

         out.addProperty("attempt", this.load + 1);
         return out;
      }

      @Override
      public void cancel() {
         if (!this.init_Z) {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }
         }
      }
   }

   private static final class Inner_CachePool {
      final BlockPos init_BlockPos;
      private final int init_I;
      private List<BlockPos> init_List = List.of();
      private int load;
      private int read = 24;
      private boolean init_Z = true;

      Inner_CachePool(BlockPos origin, int maxRadius) {
         this.init_BlockPos = origin;
         this.init_I = maxRadius;
      }

      BlockPos init_BlockPos() {
         if (this.init_Z) {
            this.init_Z = false;
            return this.init_BlockPos;
         }

         if (this.load >= this.init_List.size()) {
            if (this.read > this.init_I) {
               return null;
            }

            this.init_List = init(this.init_BlockPos, this.read);
            this.read += 24;
            this.load = 0;
         }

         return this.init_List.get(this.load++);
      }

      int init_I() {
         return Math.min(this.read, this.init_I);
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
      final boolean load_Z;

      Inner_ConfigStore(int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy) {
         super(timeoutSeconds, strategy, 96, 256);
         BlockPos origin = this.init_d_CachePool.init_BlockPos;
         this.load_Z = this.init_Minecraft.level.getHeight(Types.MOTION_BLOCKING, origin.getX(), origin.getZ()) - origin.getY() <= 12;
      }

      @Override
      BlockPos init(BlockPos column) {
         int topY = this.init_Minecraft.level.getHeight(Types.MOTION_BLOCKING, column.getX(), column.getZ());
         BlockPos feet = new BlockPos(column.getX(), topY, column.getZ());
         if (!AiSeek.init_Z(this.init_Minecraft, feet)) {
            return null;
         } else {
            return this.load_Z && topY > this.init_d_CachePool.init_BlockPos.getY() + 3 && this.init_d_CachePool.init_I() <= 16 ? null : feet;
         }
      }

      @Override
      boolean init_Z_14() {
         BlockPos origin = this.init_d_CachePool.init_BlockPos;
         return this.init_Minecraft.level.getHeight(Types.MOTION_BLOCKING, origin.getX(), origin.getZ()) - origin.getY() > 8;
      }

      @Override
      String init_Str_10() {
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

      Inner_StreamUtil(int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy, Integer maxY) {
         super(timeoutSeconds, strategy, 64, 8);
         this.update = maxY != null ? maxY : this.init_d_CachePool.init_BlockPos.getY() + 8;
      }

      @Override
      BlockPos init(BlockPos column) {
         int topY = this.init_Minecraft.level.getHeight(Types.MOTION_BLOCKING, column.getX(), column.getZ());
         int yTop = Math.min(this.update, topY - 2);
         int yBottom = Math.max(this.init_Minecraft.level.getMinY(), yTop - 56);

         for (int y = yTop; y >= yBottom; y--) {
            BlockPos cell = new BlockPos(column.getX(), y, column.getZ());
            AiPathfinder.Inner_FileCache feet = AiSeek.init_ClassCache_FileCac(this.init_Minecraft, cell);
            if (feet.loaded()
               && feet.passable()
               && !feet.water()
               && !feet.lava()
               && !feet.hazardFeet()
               && AiSeek.init_ClassCache_FileCac(this.init_Minecraft, cell.above()).passable()) {
               AiPathfinder.Inner_FileCache floor = AiSeek.init_ClassCache_FileCac(this.init_Minecraft, cell.below());
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
         AiPathfinder.Inner_FileCache probe = AiSeek.init_ClassCache_FileCac(this.init_Minecraft, new BlockPos(x, y, z));
         return probe.loaded() && probe.passable() && !probe.water() && !probe.lava();
      }

      @Override
      String init_Str_10() {
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
