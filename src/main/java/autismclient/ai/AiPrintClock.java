package autismclient.ai;

import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class AiPrintClock {
   private AiPrintClock() {
   }

   static autismclient.ai.print.PrintClockSequence init(final autismclient.ai.print.LivePrintEnv env, final autismclient.ai.print.PrintClockSpec spec) {
      final Minecraft mc = Minecraft.getInstance();
      final LocalPlayer originalPlayer = mc.player;
      return new autismclient.ai.print.PrintClockSequence(env, spec, new autismclient.ai.print.PrintClockSequence.Inner_DataUtil() {
         private Boolean init;

         @Override
         public autismclient.ai.print.PrintMachineJob init(BlockPos at, BlockState source) {
            return AiPrintContainer.init_bd_DataUtil_bV_DataUtil(env, new AiSchematicMachinePlan.Inner_DataUtil(at, Map.of(), source));
         }

         @Override
         public autismclient.ai.print.PrintMachineJob init(boolean powered) {
            return new AiPrintClock.Inner_DataUtil(env, spec, powered);
         }

         @Override
         public autismclient.ai.print.PrintMachineJob init(BlockPos at, BlockState source, Map<Integer, ItemStack> contents) {
            return AiPrintContainer.load(env, new AiSchematicMachinePlan.Inner_DataUtil(at, contents, source));
         }

         @Override
         public int init_I(ItemStack material) {
            return mc.player == originalPlayer && mc.player != null ? AiInventoryController.init_I_Container_ItemStack_2(mc.player.getInventory(), material) : 0;
         }

         @Override
         public boolean init() {
            return env.update_Z();
         }

         @Override
         public boolean init_Z(ItemStack material) {
            return mc.player == originalPlayer && env.load(material);
         }

         @Override
         public boolean load(ItemStack material) {
            return mc.player == originalPlayer && mc.player != null && !AiCarriedShulker.init(mc.player.getInventory(), material, List.of()).isEmpty();
         }

         @Override
         public boolean load() {
            if (mc.player == originalPlayer && originalPlayer != null) {
               if (this.init == null) {
                  this.init = env.update_Z();
               }

               return this.init == env.update_Z();
            } else {
               return false;
            }
         }
      });
   }

   private static final class Inner_DataUtil implements autismclient.ai.print.PrintMachineJob {
      final autismclient.ai.print.LivePrintEnv init_DataUtil;
      final autismclient.ai.print.PrintClockSpec init_NetHelper;
      final Object init_Obj;
      final Object load_Obj;
      final boolean init_Z;
      final boolean load_Z;
      final BlockState init_BlockState;
      final BlockState load_BlockState;
      autismclient.ai.print.PrintMachineJob.Inner_DataUtil init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init;
      long init_J;
      long load_J;
      String init_Str = "Preparing the source clock control";

      Inner_DataUtil(autismclient.ai.print.LivePrintEnv env, autismclient.ai.print.PrintClockSpec spec, boolean powered) {
         this.init_DataUtil = env;
         this.init_NetHelper = spec;
         this.init_Obj = env.init_Obj();
         this.load_Obj = Minecraft.getInstance().player;
         this.load_Z = env.update_Z();
         this.init_Z = powered;
         this.init_BlockState = spec.source().get(spec.lever());
         this.load_BlockState = this.init_BlockState.setValue(BlockStateProperties.POWERED, powered);
      }

      boolean init_Z() {
         return this.load_Obj != null
            && Minecraft.getInstance().player == this.load_Obj
            && this.init_DataUtil.init_Obj() == this.init_Obj
            && this.init_DataUtil.update_Z() == this.load_Z;
      }

      @Override
      public BlockPos position() {
         return this.init_NetHelper.lever();
      }

      @Override
      public Item item() {
         return Items.AIR;
      }

      @Override
      public String description() {
         return this.init_Z ? "clock hold control" : "clock control restoration";
      }

      @Override
      public String detail() {
         return this.init_Str;
      }

      @Override
      public boolean matchesSource(BlockState state) {
         return state == this.init_BlockState;
      }

      @Override
      public autismclient.ai.print.PrintMachineJob.Inner_DataUtil prepare() {
         if (this.init_BitSetUtil_DataUti != autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init
            && this.init_BitSetUtil_DataUti != autismclient.ai.print.PrintMachineJob.Inner_DataUtil.load) {
            return this.init_BitSetUtil_DataUti;
         }

         if (this.init_Z() && this.init_DataUtil.load_Z(this.position())) {
            BlockState actual = this.init_DataUtil.init_BlockState_BlockPos(this.position());
            if (actual != this.init_BlockState && actual != this.init_BlockState.setValue(BlockStateProperties.POWERED, true)) {
               return this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush;
            } else if (actual == this.load_BlockState) {
               this.init_Str = "Clock control changed without this action's receipt";
               return this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush;
            } else {
               Minecraft mc = Minecraft.getInstance();
               return mc.player != null
                     && mc.gui.screen() == null
                     && mc.player.containerMenu == mc.player.inventoryMenu
                     && mc.player.inventoryMenu.getCarried().isEmpty()
                  ? (this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.load)
                  : (this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init);
            }
         } else {
            this.init_Str = "Clock control world, player or game mode changed";
            return this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush;
         }
      }

      @Override
      public boolean beginDispatch() {
         if (this.prepare() == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.load
            && !this.init_DataUtil.load_Z()
            && !this.init_NetHelper.sweptVolume().intersects(this.init_DataUtil.init_AABB())) {
            this.init_J = AiServerEvidence.init_J_BlockPos(this.position());
            this.load_J = this.init_DataUtil.load_J();
            this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.read;
            this.init_Str = "Waiting for the clock lever's native server receipt";
            return true;
         } else {
            return false;
         }
      }

      @Override
      public autismclient.ai.print.PrintMachineJob.Inner_DataUtil poll_BitSetUtil_DataUti() {
         if (this.init_BitSetUtil_DataUti != autismclient.ai.print.PrintMachineJob.Inner_DataUtil.read) {
            return this.init_BitSetUtil_DataUti;
         }

         if (!this.init_Z()) {
            this.init_Str = "Clock control world, player or game mode changed after dispatch";
            return this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close;
         }

         AiServerEvidence.Inner_DataUtil reply = AiServerEvidence.init_ca_DataUtil_BlockPos(this.position());
         if (reply == null
            || reply.revision() <= this.init_J
            || reply.state() != this.load_BlockState
            || this.init_DataUtil.init_BlockState_BlockPos(this.position()) != this.load_BlockState
            || this.init_Z && !this.init_NetHelper.holding(this.init_DataUtil::init_BlockState_BlockPos)) {
            if (this.init_DataUtil.load_J() - this.load_J >= 5000000000L) {
               this.init_Str = "Clock control was not acknowledged; do not repeat the toggle";
               return this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close;
            } else {
               return this.init_BitSetUtil_DataUti;
            }
         } else {
            this.init_Str = "Native clock control and holding geometry acknowledged";
            return this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.update;
         }
      }

      @Override
      public void cancel() {
         if (this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.read) {
            this.init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close;
            this.init_Str = "Clock control dispatch interrupted without a receipt";
         }
      }
   }
}
