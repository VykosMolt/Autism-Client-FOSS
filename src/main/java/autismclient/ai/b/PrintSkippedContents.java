package autismclient.ai.b;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public record PrintSkippedContents(BlockPos position, BlockState source, int skippedSlots, String reason) implements PrintMachineJob {
   public PrintSkippedContents(BlockPos position, BlockState source, int skippedSlots, String reason) {
      position = position.immutable();
      Objects.requireNonNull(source);
      if (skippedSlots >= 1 && reason != null && !reason.isBlank()) {
         this.position = position;
         this.source = source;
         this.skippedSlots = skippedSlots;
         this.reason = reason;
      } else {
         throw new IllegalArgumentException("Missing skipped inventory receipt");
      }
   }

   @Override
   public Item item() {
      return Items.AIR;
   }

   @Override
   public String description() {
      return "unavailable saved contents";
   }

   @Override
   public String detail() {
      return this.position.toShortString() + ": " + this.reason;
   }

   @Override
   public boolean matchesSource(BlockState state) {
      return state == this.source;
   }

   @Override
   public PrintMachineJob.Inner_DataUtil prepare() {
      return PrintMachineJob.Inner_DataUtil.sync;
   }

   @Override
   public PrintMachineJob.Inner_DataUtil poll_BitSetUtil_DataUti() {
      return PrintMachineJob.Inner_DataUtil.sync;
   }

   @Override
   public boolean beginDispatch() {
      return false;
   }

   @Override
   public boolean needsAccess() {
      return false;
   }

   @Override
   public void cancel() {
   }

   @Override
   public List<String> skippedReasons() {
      return List.of(this.reason);
   }
}
