package autismclient.ai.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;

public final class AiBackfill {
   private static final int init_I = 256;
   private static final int load_I = 32;
   private static final int read = 40;
   private static final double init_D = 22.5625;
   private static final ItemStack[] EMPTY_HOTBAR = new ItemStack[9];
   private static final boolean DEBUG = Boolean.getBoolean("autism.navigation.debug");
   private final Minecraft client;
   private final AiPlacer placer = new AiPlacer();
   private boolean load_Z;
   private final Deque<BlockPos> pendingCells = new ArrayDeque<>();
   private final Set<Long> visitedCells = new HashSet<>();
   private BlockPos currentCell;
   private int cellTicks;
   private int previousSlot = -1;
   private int placedCount;

   public AiBackfill(Minecraft mc) {
      this.client = mc;
   }

   public int getBackfilledCount() {
      return this.placedCount;
   }

   boolean isBackfilling() {
      return this.currentCell != null;
   }

   boolean isIdle() {
      return this.currentCell == null && this.pendingCells.isEmpty();
   }

   int pendingCount() {
      return this.pendingCells.size();
   }

   public void recordDugCell(BlockPos cell) {
      BlockPos immutable = cell.immutable();
      if (this.visitedCells.add(immutable.asLong())) {
         this.pendingCells.addLast(immutable);

         while (this.pendingCells.size() > 256) {
            this.visitedCells.remove(this.pendingCells.pollFirst().asLong());
         }
      }
   }

   static boolean isBehindCandidate(double dot, double distanceSqr, boolean open, boolean supported, boolean reserved) {
      return dot < -0.2 && distanceSqr >= 4.0 && open && supported && !reserved;
   }

   static double facingDot(Vec3 toCell, Vec3 lookVec) {
      return toCell.normalize().dot(lookVec.normalize());
   }

   public void tick() {
      if (this.client.player != null && this.client.level != null) {
         if (this.currentCell != null) {
            this.read_V();
         } else {
            Vec3 look = this.client.player.getLookAngle();
            Vec3 pos = this.client.player.position();
            int scanned = 0;
            Iterator<BlockPos> iterator = this.pendingCells.iterator();

            while (iterator.hasNext() && scanned < 32) {
               scanned++;
               BlockPos cell = iterator.next();
               Vec3 center = Vec3.atCenterOf(cell);
               AiPathfinder.CellInfo probe = this.classifyCell(cell);
               boolean open = probe.passable() && !probe.water() && !probe.lava();
               if (open && this.init_Z(cell)) {
                  double distanceSqr = center.distanceToSqr(pos);
                  if (!isBehindCandidate(facingDot(center.subtract(pos), look), distanceSqr, true, true, false)) {
                     if (DEBUG) {
                        AiNavDebug.log(
                           "backfill skip-geometry cell="
                              + cell
                              + " dot="
                              + String.format("%.2f", facingDot(center.subtract(pos), look))
                              + " dist2="
                              + String.format("%.1f", distanceSqr)
                        );
                     }
                  } else {
                     if (!(this.client.player.getEyePosition().distanceToSqr(center) > 22.5625)) {
                        iterator.remove();
                        this.currentCell = cell;
                        this.cellTicks = 0;
                        this.load_Z = false;
                        this.read_V();
                        return;
                     }

                     if (DEBUG) {
                        AiNavDebug.log("backfill skip-reach cell=" + cell);
                     }
                  }
               } else {
                  iterator.remove();
                  this.visitedCells.remove(cell.asLong());
               }
            }
         }
      }
   }

   private void read_V() {
      if (++this.cellTicks > 40) {
         this.finishCell(false);
      } else {
         if (!this.load_Z) {
            if (!this.classifyCell(this.currentCell).passable()) {
               if (DEBUG) {
                  AiNavDebug.log("backfill fail occupied cell=" + this.currentCell);
               }

               this.finishCell(false);
               return;
            }

            int slot = this.read_I();
            if (slot < 0) {
               if (DEBUG) {
                  AiNavDebug.log("backfill fail no-throwaway cell=" + this.currentCell);
               }

               this.finishCell(false);
               return;
            }

            this.previousSlot = this.client.player.getInventory().getSelectedSlot();
            this.placer.start(this.currentCell, this.client.player.getInventory().getItem(slot).getItem());
            this.load_Z = true;
         }

         switch (this.placer.tick()) {
            case PLACED:
               this.finishCell(true);
               break;
            case FAILED:
               if (DEBUG) {
                  AiNavDebug.log("backfill placer-failed cell=" + this.currentCell);
               }

               this.finishCell(false);
            case WORKING:
         }
      }
   }

   private void finishCell(boolean verified) {
      if (verified) {
         this.placedCount++;
      }

      this.visitedCells.remove(this.currentCell.asLong());
      this.currentCell = null;
      this.cellTicks = 0;
      this.placer.reset();
      this.load_Z = false;
      if (this.previousSlot >= 0 && this.client.player != null) {
         this.client.player.getInventory().setSelectedSlot(this.previousSlot);
         this.previousSlot = -1;
      }
   }

   public void reset() {
      this.pendingCells.clear();
      this.visitedCells.clear();
      if (this.currentCell != null) {
         this.finishCell(false);
      }

      this.placer.reset();
      this.load_Z = false;
   }

   private int read_I() {
      List<AiRestock.BackpackSlotView> hotbar = new ArrayList<>(9);

      for (int slot = 0; slot < 9; slot++) {
         hotbar.add(this.restockOption(this.client.player.getInventory().getItem(slot)));
      }

      return AiRestock.pickBestBackpackSlot(hotbar, null);
   }

   private AiRestock.BackpackSlotView restockOption(ItemStack stack) {
      return stack.getItem() instanceof BlockItem block && AiRestock.canSpendStack(stack, this.client.player.getInventory())
         ? new AiRestock.BackpackSlotView(
            BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
            stack.getCount(),
            block.getBlock() instanceof FallingBlock,
            block.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.client.level, this.client.player.blockPosition()),
            block.getBlock().defaultBlockState().hasBlockEntity()
         )
         : new AiRestock.BackpackSlotView(null, 0, false, false, false);
   }

   private boolean init_Z(BlockPos cell) {
      for (Direction direction : Direction.values()) {
         if (!this.classifyCell(cell.relative(direction)).passable()) {
            return true;
         }
      }

      return false;
   }

   private AiPathfinder.CellInfo classifyCell(BlockPos cell) {
      return AiPathfinder.LiveGridView.classifyCellCompat(this.client, this.client.level, cell, EMPTY_HOTBAR);
   }
}
