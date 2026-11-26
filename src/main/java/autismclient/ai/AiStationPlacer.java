package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

final class AiStationPlacer {
   static final int init_I = 60;
   private final Item init_Item;
   private final String init_Str;
   private final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
   private List<BlockPos> init_List;
   private int load_I;
   private int read_I;
   private boolean init_Z;
   private boolean load_Z;
   private AiOperation init_AiOperation;
   private AiOperation load_AiOperation;
   private boolean read_Z;
   private BlockPos init_BlockPos;
   private BlockPos load_BlockPos;
   private String load_Str = "";
   private String read_Str = "";

   AiStationPlacer(Item item, String name) {
      this.init_Item = item;
      this.init_Str = name;
   }

   AiStationPlacer.Inner_DataUtil init_cn_DataUtil() {
      if (this.load_BlockPos != null) {
         return AiStationPlacer.Inner_DataUtil.PLACED;
      }

      if (this.load_AiOperation != null) {
         AiOperation.Result r = this.load_AiOperation.tick();
         if (!r.terminal()) {
            this.read_Str = "Making room for the " + this.init_Str + ": " + r.message();
            return AiStationPlacer.Inner_DataUtil.WORKING;
         }

         this.load_AiOperation.cancel();
         this.load_AiOperation = null;
         this.init_List = null;
         this.load_I = 0;
      }

      if (this.init_AiOperation != null) {
         AiOperation.Result r = this.init_AiOperation.tick();
         if (!r.terminal()) {
            this.read_Str = "Moving to open ground for the " + this.init_Str + ": " + r.message();
            return AiStationPlacer.Inner_DataUtil.WORKING;
         }

         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
         this.init_List = null;
      }

      if (!this.init_Z) {
         if (this.init_List == null && !this.load_Z && AiCraft.read() && this.read()) {
            return AiStationPlacer.Inner_DataUtil.WORKING;
         }

         if (this.init_List == null) {
            this.init_List = new ArrayList<>(AiCraft.init_List());
            this.load_I = 0;
            this.init_List.removeIf(spotx -> !spotx.equals(this.init_BlockPos) && !autismclient.ai.engine.AiPlacer.init_Z_BlockPos_Item(spotx, this.init_Item));
            if (this.init_BlockPos != null && this.init_List.remove(this.init_BlockPos)) {
               this.init_List = new ArrayList<>(this.init_List);
               this.init_List.add(0, this.init_BlockPos);
            }

            boolean allOurs = !this.init_List.isEmpty();

            for (BlockPos spot : this.init_List) {
               if (!autismclient.ai.engine.AiEditLedger.write_Z(spot)) {
                  allOurs = false;
                  break;
               }
            }

            if (allOurs && this.write()) {
               this.init_List = null;
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }
         }

         if (this.load_I >= Math.min(this.init_List.size(), 8)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null
               && mc.level != null
               && init(mc.player.blockPosition(), cell -> !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty())
               && this.write()) {
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }

            if (this.read()) {
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }

            if (this.write()) {
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }

            if (this.load_Str.isEmpty()) {
               this.load_Str = "no free supported spot within two blocks";
            }

            return AiStationPlacer.Inner_DataUtil.FAILED;
         }

         this.init_DataPipe.init_V_BlockPos_Item(this.init_List.get(this.load_I), this.init_Item);
         this.init_Z = true;
         this.read_I = 0;
      }

      autismclient.ai.engine.AiPlacer.Inner_DataUtil status = this.init_DataPipe.init_DataPipe_DataUtil();
      if (status == autismclient.ai.engine.AiPlacer.Inner_DataUtil.PLACED) {
         this.init_Z = false;
         this.load_BlockPos = this.init_DataPipe.init_BlockPos();
         return AiStationPlacer.Inner_DataUtil.PLACED;
      } else if (status != autismclient.ai.engine.AiPlacer.Inner_DataUtil.FAILED && ++this.read_I <= 60) {
         this.read_Str = "Placing a " + this.init_Str;
         return AiStationPlacer.Inner_DataUtil.WORKING;
      } else {
         this.init_Z = false;
         this.load_Str = status == autismclient.ai.engine.AiPlacer.Inner_DataUtil.FAILED
            ? this.init_DataPipe.init_Str()
            : "not placed within 60 ticks: " + this.init_DataPipe.load_Str();
         this.init_DataPipe.init_V();
         AutismClientAddon.LOG
            .info("[AI] {} spot {} unusable after {} ticks: {}", this.init_Str, this.init_List.get(this.load_I).toShortString(), this.read_I, this.load_Str);
         this.load_I++;
         this.read_Str = this.init_Str + " spot unusable (" + this.load_Str + "); trying the next spot";
         return AiStationPlacer.Inner_DataUtil.WORKING;
      }
   }

   static boolean init(BlockPos feet, Predicate<BlockPos> collision) {
      for (Direction side : Plane.HORIZONTAL) {
         BlockPos beside = feet.relative(side);
         if (!collision.test(beside) && !collision.test(beside.above())) {
            return false;
         }
      }

      return true;
   }

   private boolean read() {
      if (this.load_Z) {
         return false;
      }

      this.load_Z = true;
      BlockPos open = AiCraft.init(6);
      if (open == null) {
         open = AiCraft.load(3);
      }

      if (open == null) {
         return false;
      }

      AutismClientAddon.LOG.info("[AI] no spot for a {} here, moving to {}", this.init_Str, open.toShortString());
      this.init(open);
      this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(open, 0.6, true, false, false, 30);
      this.read_Str = "No room for a " + this.init_Str + " here; moving to " + open.toShortString();
      return true;
   }

   private void init(BlockPos open) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         ArrayList<Direction> sides = new ArrayList<>();
         Plane.HORIZONTAL.forEach(sides::add);
         sides.sort(Comparator.comparingDouble(sidex -> feet.relative(sidex).distSqr(open)));

         for (Direction side : sides) {
            BlockPos cell = feet.relative(side);
            BlockPos above = cell.above();
            boolean lowerLeaf = mc.level.getBlockState(cell).is(BlockTags.LEAVES);
            boolean upperLeaf = mc.level.getBlockState(above).is(BlockTags.LEAVES);
            if ((lowerLeaf || upperLeaf)
               && (lowerLeaf || mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty())
               && (upperLeaf || mc.level.getBlockState(above).getCollisionShape(mc.level, above).isEmpty())
               && mc.level.getFluidState(cell).isEmpty()
               && mc.level.getFluidState(above).isEmpty()) {
               boolean landing = false;

               for (int drop = 1; drop <= 3; drop++) {
                  BlockPos below = cell.below(drop);
                  if (AiGoto.init_Z(below)) {
                     landing = true;
                     break;
                  }

                  if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty() || !mc.level.getFluidState(below).isEmpty()) {
                     break;
                  }
               }

               if (landing) {
                  AutismClientAddon.LOG.info("[AI] clearing a leaf exit for the {} at {}", this.init_Str, cell.toShortString());
                  this.load_AiOperation = lowerLeaf && upperLeaf ? init(above, cell) : autismclient.ai.engine.AiBreakBlock.init(upperLeaf ? above : cell, 10);
                  return;
               }
            }
         }
      }
   }

   private boolean write() {
      if (this.read_Z) {
         return false;
      }

      this.read_Z = true;
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();

         for (Direction side : Plane.HORIZONTAL) {
            BlockPos cell = feet.relative(side);
            BlockState state = mc.level.getBlockState(cell);
            if (init(mc, cell) && mc.level.getBlockState(cell.below()).isCollisionShapeFullBlock(mc.level, cell.below())) {
               BlockPos above = cell.above();
               boolean clearHead = !mc.level.getBlockState(above).getCollisionShape(mc.level, above).isEmpty();
               if (!clearHead || init(mc, above)) {
                  AutismClientAddon.LOG.info("[AI] no spot for a {} here, digging one out at {}", this.init_Str, cell.toShortString());
                  this.load_AiOperation = clearHead ? init(above, cell) : autismclient.ai.engine.AiBreakBlock.init(cell, 10);
                  this.init_BlockPos = cell.immutable();
                  this.read_Str = "Digging out a spot for the " + this.init_Str;
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean init(Minecraft mc, BlockPos cell) {
      BlockState state = mc.level.getBlockState(cell);
      if (!state.isAir() && !state.hasBlockEntity() && !(state.getDestroySpeed(mc.level, cell) < 0.0F) && state.getFluidState().isEmpty()) {
         if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("_ore")) {
            return false;
         }

         if (autismclient.ai.engine.MiningCost.load(mc, state, false) < 0) {
            return false;
         }

         for (Direction side : Direction.values()) {
            if (!mc.level.getFluidState(cell.relative(side)).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static AiOperation init(final BlockPos above, final BlockPos cell) {
      return new AiOperation() {
         AiOperation init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(above, 10);
         boolean init_Z = true;

         @Override
         public AiOperation.Result tick() {
            AiOperation.Result result = this.init_AiOperation.tick();
            if (result.terminal() && result.status() == AiOperation.Status.SUCCEEDED && this.init_Z) {
               this.init_AiOperation.cancel();
               this.init_Z = false;
               this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(cell, 10);
               return this.init_AiOperation.tick();
            } else {
               return result;
            }
         }

         @Override
         public void cancel() {
            this.init_AiOperation.cancel();
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation.requiresReconciliationAfterEmergency();
         }
      };
   }

   BlockPos init_BlockPos() {
      return this.load_BlockPos;
   }

   String init_Str() {
      return this.read_Str;
   }

   String load_Str() {
      return this.load_Str;
   }

   boolean init_Z() {
      return !this.init_DataPipe.load_Z()
         && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
         && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
   }

   boolean load_Z() {
      return this.init_DataPipe.load_Z()
         || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
         || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
   }

   void init_V() {
      this.init_DataPipe.init_V();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
      }

      if (this.load_AiOperation != null) {
         this.load_AiOperation.cancel();
         this.load_AiOperation = null;
      }
   }

   enum Inner_DataUtil {
      WORKING,
      PLACED,
      FAILED;
   }
}
