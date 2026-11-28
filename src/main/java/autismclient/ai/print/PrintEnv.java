package autismclient.ai.print;

import autismclient.ai.engine.AiPhysics;
import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface PrintEnv {
   long ALL_CHUNKS_SENTINEL = -9223372036854775808L;
   long DEFAULT_WORK_PER_TICK = Long.getLong("autism.ai.print.workPerTick", 300000L);

   BlockState getBlockState(BlockPos var1);

   default BlockEntity getBlockEntity(BlockPos pos) {
      return null;
   }

   default Object worldToken() {
      return this;
   }

   default PrintEnv.ReceiptState interactionReceiptState() {
      return PrintEnv.ReceiptState.NONE;
   }

   default List<PrintRailVehicle.Inner_DataUtil> nearbyMinecarts(BlockPos rail) {
      return null;
   }

   default List<PrintItemFrame.ObservedFrame> itemFramesAt(BlockPos cell) {
      return null;
   }

   default String blockingEntityName(BlockPos cell) {
      return null;
   }

   boolean hasChunkAt(BlockPos var1);

   int getMinY();

   int getMaxY();

   default void drainBlockChanges(LongConsumer changed) {
   }

   default void drainBlockChangesWithPrior(PrintEnv.BlockChangeSink changed) {
      this.drainBlockChanges(key -> changed.changed(key, null));
   }

   default boolean drainBlockChangesBudgeted(PrintEnv.BlockChangeSink changed, int budget) {
      this.drainBlockChangesWithPrior(changed);
      return true;
   }

   default void drainChunkChanges(LongConsumer changed) {
   }

   AiPhysics.MotionState playerMotionState();

   AiPhysics.PhysicsWorld livePhysicsWorld();

   float playerYaw();

   float playerPitch();

   double playerEyeHeight();

   float playerHealth();

   Vec3 serverFeetPos();

   float serverYaw();

   float serverPitch();

   boolean isServerSneaking();

   default long workBudgetPerTick() {
      return DEFAULT_WORK_PER_TICK;
   }

   default boolean preloadContainerSlots(BlockPos position, Map<Integer, ItemStack> saved) {
      return false;
   }

   ItemStack getInventoryItem(int var1);

   int getSelectedHotbarSlot();

   default boolean isHandReady() {
      return true;
   }

   default boolean isCreativeMode() {
      return false;
   }

   default boolean isInstantBreakTool(ItemStack stack) {
      return false;
   }

   default boolean isFlightPermitted() {
      return this.canUseFlight();
   }

   default boolean canUseFlight() {
      return false;
   }

   default boolean suppressFlight() {
      return false;
   }

   default boolean unsuppressFlight() {
      return false;
   }

   default boolean isFlying() {
      return false;
   }

   default float flyingSpeed() {
      return 0.05F;
   }

   default boolean canSprint() {
      return false;
   }

   default Boolean isHeadRotationAligned(float yaw, float pitch) {
      return null;
   }

   default long nanoTime() {
      return this.currentTick() * 50000000L;
   }

   default boolean isFallGuardActive() {
      return false;
   }

   default void requestCreativeItem(Item item) {
   }

   default String creativeStockError() {
      return null;
   }

   default void recordPlacedBlock(BlockPos position, BlockState state) {
   }

   default void clearPlacedBlock(BlockPos position) {
   }

   BlockState predictPlacedState(Item var1, BlockHitResult var2, float var3, float var4, boolean var5, BlockPos var6);

   boolean canBlockSurvive(BlockState var1, BlockPos var2);

   void applyControls(PrintControls var1);

   long currentTick();

   void logTrace(String var1);

   default Vec3 serverEyePos() {
      Vec3 f = this.serverFeetPos();
      return new Vec3(f.x, f.y + (this.isServerSneaking() ? 1.27 : 1.62), f.z);
   }

   default AABB serverPlayerBox() {
      Vec3 feet = this.serverFeetPos();
      return PrintGeometry.bodyBox(feet.x, feet.y, feet.z, this.isServerSneaking());
   }

   default Vec3 clientEyePos() {
      AiPhysics.MotionState b = this.playerMotionState();
      return new Vec3(b.x(), b.y() + this.playerEyeHeight(), b.z());
   }

   interface BlockChangeSink {
      void changed(long var1, BlockState var3);
   }

   enum ReceiptState {
      NONE,
      PENDING,
      UNCERTAIN;
   }
}
