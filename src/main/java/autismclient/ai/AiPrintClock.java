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
         private Boolean creative;

         @Override
         public autismclient.ai.print.PrintMachineJob init(BlockPos at, BlockState source) {
            return AiPrintContainer.forClock(env, new AiSchematicMachinePlan.ContainerPlan(at, Map.of(), source));
         }

         @Override
         public autismclient.ai.print.PrintMachineJob init(boolean powered) {
            return new AiPrintClock.ClockControlJob(env, spec, powered);
         }

         @Override
         public autismclient.ai.print.PrintMachineJob init(BlockPos at, BlockState source, Map<Integer, ItemStack> contents) {
            return AiPrintContainer.load(env, new AiSchematicMachinePlan.ContainerPlan(at, contents, source));
         }

         @Override
         public int init_I(ItemStack material) {
            return mc.player == originalPlayer && mc.player != null ? AiInventoryController.countMatching(mc.player.getInventory(), material) : 0;
         }

         @Override
         public boolean init() {
            return env.isCreativeMode();
         }

         @Override
         public boolean init_Z(ItemStack material) {
            return mc.player == originalPlayer && env.giveCreativeStack(material);
         }

         @Override
         public boolean load(ItemStack material) {
            return mc.player == originalPlayer && mc.player != null && !AiCarriedShulker.init(mc.player.getInventory(), material, List.of()).isEmpty();
         }

         @Override
         public boolean load() {
            if (mc.player == originalPlayer && originalPlayer != null) {
               if (this.creative == null) {
                  this.creative = env.isCreativeMode();
               }

               return this.creative == env.isCreativeMode();
            } else {
               return false;
            }
         }
      });
   }

   private static final class ClockControlJob implements autismclient.ai.print.PrintMachineJob {
      final autismclient.ai.print.LivePrintEnv env;
      final autismclient.ai.print.PrintClockSpec spec;
      final Object worldToken;
      final Object player;
      final boolean powered;
      final boolean creative;
      final BlockState sourceState;
      final BlockState targetState;
      autismclient.ai.print.PrintMachineJob.JobStatus status = autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING;
      long blockRevision;
      long startNanos;
      String message = "Preparing the source clock control";

      ClockControlJob(autismclient.ai.print.LivePrintEnv env, autismclient.ai.print.PrintClockSpec spec, boolean powered) {
         this.env = env;
         this.spec = spec;
         this.worldToken = env.worldToken();
         this.player = Minecraft.getInstance().player;
         this.creative = env.isCreativeMode();
         this.powered = powered;
         this.sourceState = spec.source().get(spec.lever());
         this.targetState = this.sourceState.setValue(BlockStateProperties.POWERED, powered);
      }

      boolean isSameSession() {
         return this.player != null
            && Minecraft.getInstance().player == this.player
            && this.env.worldToken() == this.worldToken
            && this.env.isCreativeMode() == this.creative;
      }

      @Override
      public BlockPos position() {
         return this.spec.lever();
      }

      @Override
      public Item item() {
         return Items.AIR;
      }

      @Override
      public String description() {
         return this.powered ? "clock hold control" : "clock control restoration";
      }

      @Override
      public String detail() {
         return this.message;
      }

      @Override
      public boolean matchesSource(BlockState state) {
         return state == this.sourceState;
      }

      @Override
      public autismclient.ai.print.PrintMachineJob.JobStatus prepare() {
         if (this.status != autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING
            && this.status != autismclient.ai.print.PrintMachineJob.JobStatus.READY) {
            return this.status;
         }

         if (this.isSameSession() && this.env.hasChunkAt(this.position())) {
            BlockState actual = this.env.getBlockState(this.position());
            if (actual != this.sourceState && actual != this.sourceState.setValue(BlockStateProperties.POWERED, true)) {
               return this.status = autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT;
            } else if (actual == this.targetState) {
               this.message = "Clock control changed without this action's receipt";
               return this.status = autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT;
            } else {
               Minecraft mc = Minecraft.getInstance();
               return mc.player != null
                     && mc.gui.screen() == null
                     && mc.player.containerMenu == mc.player.inventoryMenu
                     && mc.player.inventoryMenu.getCarried().isEmpty()
                  ? (this.status = autismclient.ai.print.PrintMachineJob.JobStatus.READY)
                  : (this.status = autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING);
            }
         } else {
            this.message = "Clock control world, player or game mode changed";
            return this.status = autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT;
         }
      }

      @Override
      public boolean beginDispatch() {
         if (this.prepare() == autismclient.ai.print.PrintMachineJob.JobStatus.READY
            && !this.env.isServerSneaking()
            && !this.spec.sweptVolume().intersects(this.env.serverPlayerBox())) {
            this.blockRevision = AiServerEvidence.blockRevision(this.position());
            this.startNanos = this.env.nanoTime();
            this.status = autismclient.ai.print.PrintMachineJob.JobStatus.SENT;
            this.message = "Waiting for the clock lever's native server receipt";
            return true;
         } else {
            return false;
         }
      }

      @Override
      public autismclient.ai.print.PrintMachineJob.JobStatus pollStatus() {
         if (this.status != autismclient.ai.print.PrintMachineJob.JobStatus.SENT) {
            return this.status;
         }

         if (!this.isSameSession()) {
            this.message = "Clock control world, player or game mode changed after dispatch";
            return this.status = autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN;
         }

         AiServerEvidence.BlockUpdate reply = AiServerEvidence.lastBlockUpdate(this.position());
         if (reply == null
            || reply.revision() <= this.blockRevision
            || reply.state() != this.targetState
            || this.env.getBlockState(this.position()) != this.targetState
            || this.powered && !this.spec.holding(this.env::getBlockState)) {
            if (this.env.nanoTime() - this.startNanos >= 5000000000L) {
               this.message = "Clock control was not acknowledged; do not repeat the toggle";
               return this.status = autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN;
            } else {
               return this.status;
            }
         } else {
            this.message = "Native clock control and holding geometry acknowledged";
            return this.status = autismclient.ai.print.PrintMachineJob.JobStatus.SUCCEEDED;
         }
      }

      @Override
      public void cancel() {
         if (this.status == autismclient.ai.print.PrintMachineJob.JobStatus.SENT) {
            this.status = autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN;
            this.message = "Clock control dispatch interrupted without a receipt";
         }
      }
   }
}
