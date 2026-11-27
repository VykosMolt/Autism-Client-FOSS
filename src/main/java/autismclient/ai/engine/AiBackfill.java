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
   private static final ItemStack[] init_ItemStackArr = new ItemStack[9];
   private static final boolean init_Z = Boolean.getBoolean("autism.navigation.debug");
   private final Minecraft init_Minecraft;
   private final AiPlacer init_DataPipe = new AiPlacer();
   private boolean load_Z;
   private final Deque<BlockPos> init_Deque = new ArrayDeque<>();
   private final Set<Long> init_Set = new HashSet<>();
   private BlockPos init_BlockPos;
   private int write;
   private int update = -1;
   private int sync;

   public AiBackfill(Minecraft mc) {
      this.init_Minecraft = mc;
   }

   public int init_I() {
      return this.sync;
   }

   boolean init_Z() {
      return this.init_BlockPos != null;
   }

   boolean load_Z() {
      return this.init_BlockPos == null && this.init_Deque.isEmpty();
   }

   int load_I() {
      return this.init_Deque.size();
   }

   public void init_V(BlockPos cell) {
      BlockPos immutable = cell.immutable();
      if (this.init_Set.add(immutable.asLong())) {
         this.init_Deque.addLast(immutable);

         while (this.init_Deque.size() > 256) {
            this.init_Set.remove(this.init_Deque.pollFirst().asLong());
         }
      }
   }

   static boolean init(double dot, double distanceSqr, boolean open, boolean supported, boolean reserved) {
      return dot < -0.2 && distanceSqr >= 4.0 && open && supported && !reserved;
   }

   static double init(Vec3 toCell, Vec3 lookVec) {
      return toCell.normalize().dot(lookVec.normalize());
   }

   public void init_V() {
      if (this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
         if (this.init_BlockPos != null) {
            this.read_V();
         } else {
            Vec3 look = this.init_Minecraft.player.getLookAngle();
            Vec3 pos = this.init_Minecraft.player.position();
            int scanned = 0;
            Iterator<BlockPos> iterator = this.init_Deque.iterator();

            while (iterator.hasNext() && scanned < 32) {
               scanned++;
               BlockPos cell = iterator.next();
               Vec3 center = Vec3.atCenterOf(cell);
               AiPathfinder.CellInfo probe = this.init_ClassCache_FileCac(cell);
               boolean open = probe.passable() && !probe.water() && !probe.lava();
               if (open && this.init_Z(cell)) {
                  double distanceSqr = center.distanceToSqr(pos);
                  if (!init(init(center.subtract(pos), look), distanceSqr, true, true, false)) {
                     if (init_Z) {
                        AiNavDebug.init(
                           "backfill skip-geometry cell="
                              + cell
                              + " dot="
                              + String.format("%.2f", init(center.subtract(pos), look))
                              + " dist2="
                              + String.format("%.1f", distanceSqr)
                        );
                     }
                  } else {
                     if (!(this.init_Minecraft.player.getEyePosition().distanceToSqr(center) > 22.5625)) {
                        iterator.remove();
                        this.init_BlockPos = cell;
                        this.write = 0;
                        this.load_Z = false;
                        this.read_V();
                        return;
                     }

                     if (init_Z) {
                        AiNavDebug.init("backfill skip-reach cell=" + cell);
                     }
                  }
               } else {
                  iterator.remove();
                  this.init_Set.remove(cell.asLong());
               }
            }
         }
      }
   }

   private void read_V() {
      if (++this.write > 40) {
         this.init(false);
      } else {
         if (!this.load_Z) {
            if (!this.init_ClassCache_FileCac(this.init_BlockPos).passable()) {
               if (init_Z) {
                  AiNavDebug.init("backfill fail occupied cell=" + this.init_BlockPos);
               }

               this.init(false);
               return;
            }

            int slot = this.read_I();
            if (slot < 0) {
               if (init_Z) {
                  AiNavDebug.init("backfill fail no-throwaway cell=" + this.init_BlockPos);
               }

               this.init(false);
               return;
            }

            this.update = this.init_Minecraft.player.getInventory().getSelectedSlot();
            this.init_DataPipe.start(this.init_BlockPos, this.init_Minecraft.player.getInventory().getItem(slot).getItem());
            this.load_Z = true;
         }

         switch (this.init_DataPipe.tick()) {
            case PLACED:
               this.init(true);
               break;
            case FAILED:
               if (init_Z) {
                  AiNavDebug.init("backfill placer-failed cell=" + this.init_BlockPos);
               }

               this.init(false);
            case WORKING:
         }
      }
   }

   private void init(boolean verified) {
      if (verified) {
         this.sync++;
      }

      this.init_Set.remove(this.init_BlockPos.asLong());
      this.init_BlockPos = null;
      this.write = 0;
      this.init_DataPipe.reset();
      this.load_Z = false;
      if (this.update >= 0 && this.init_Minecraft.player != null) {
         this.init_Minecraft.player.getInventory().setSelectedSlot(this.update);
         this.update = -1;
      }
   }

   public void load_V() {
      this.init_Deque.clear();
      this.init_Set.clear();
      if (this.init_BlockPos != null) {
         this.init(false);
      }

      this.init_DataPipe.reset();
      this.load_Z = false;
   }

   private int read_I() {
      List<AiRestock.Inner_CachePool> hotbar = new ArrayList<>(9);

      for (int slot = 0; slot < 9; slot++) {
         hotbar.add(this.init(this.init_Minecraft.player.getInventory().getItem(slot)));
      }

      return AiRestock.pickBestBackpackSlot(hotbar, null);
   }

   private AiRestock.Inner_CachePool init(ItemStack stack) {
      return stack.getItem() instanceof BlockItem block && AiRestock.canSpendStack(stack, this.init_Minecraft.player.getInventory())
         ? new AiRestock.Inner_CachePool(
            BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
            stack.getCount(),
            block.getBlock() instanceof FallingBlock,
            block.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition()),
            block.getBlock().defaultBlockState().hasBlockEntity()
         )
         : new AiRestock.Inner_CachePool(null, 0, false, false, false);
   }

   private boolean init_Z(BlockPos cell) {
      for (Direction direction : Direction.values()) {
         if (!this.init_ClassCache_FileCac(cell.relative(direction)).passable()) {
            return true;
         }
      }

      return false;
   }

   private AiPathfinder.CellInfo init_ClassCache_FileCac(BlockPos cell) {
      return AiPathfinder.LiveGridView.init_ClassCache_FileCac_Minecraft(this.init_Minecraft, this.init_Minecraft.level, cell, init_ItemStackArr);
   }
}
