package autismclient.ai.print;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public interface PrintMachineJob {
   BlockPos position();

   Item item();

   String description();

   String detail();

   boolean matchesSource(BlockState var1);

   PrintMachineJob.JobStatus prepare();

   boolean beginDispatch();

   PrintMachineJob.JobStatus pollStatus();

   void cancel();

   default boolean hasFurtherSteps() {
      return false;
   }

   default PrintMachineJob currentAction() {
      return this;
   }

   default boolean canFillEarly() {
      return false;
   }

   default boolean needsAccess() {
      return true;
   }

   default boolean hasSkippableContents() {
      return false;
   }

   default Set<BlockPos> accessCells() {
      return Set.of(this.position());
   }

   default Set<BlockPos> dynamicCells() {
      return Set.of();
   }

   default boolean allowsDynamicState(BlockPos position, BlockState actual) {
      return false;
   }

   default boolean bodyClear(AABB body) {
      return true;
   }

   default PrintMachineJob.JobStatus verifyStatus() {
      return PrintMachineJob.JobStatus.SUCCEEDED;
   }

   default int fillCount() {
      return 1;
   }

   default int verifiedItems() {
      return 0;
   }

   default int completedPhases() {
      return 0;
   }

   default boolean resultUncertain() {
      return false;
   }

   default String receiptJson() {
      return "{}";
   }

   default void setGuard(Supplier<String> guard) {
   }

   default void acceptRecoveredMaterial(Map<Item, Integer> removed) {
   }

   default void setOrder(PrintContainerOrder order) {
   }

   default boolean isVerifiedSuccess() {
      return false;
   }

   default boolean reopenForCapacityRecheck() {
      return false;
   }

   default void invalidateCapacityProof() {
   }

   default boolean write_Z() {
      return false;
   }

   default boolean needsRevisit() {
      return false;
   }

   default ItemStack extraMaterial() {
      return ItemStack.EMPTY;
   }

   default void onContentsAvailable() {
   }

   default void onContentsUnavailable() {
   }

   default int verifiedSlots() {
      return 0;
   }

   default int skippedSlots() {
      return 0;
   }

   default List<String> skippedReasons() {
      return List.of();
   }

   enum JobStatus {
      PREPARING,
      READY,
      SENT,
      NEEDS_SUPPLY,
      SUCCEEDED,
      SKIPPED,
      CONFLICT,
      UNCERTAIN;
   }
}
