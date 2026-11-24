package autismclient.ai.b;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public interface BitSetUtil {
   BlockPos position();

   Item item();

   String description();

   String detail();

   boolean matchesSource(BlockState var1);

   BitSetUtil.Inner_DataUtil prepare();

   boolean beginDispatch();

   BitSetUtil.Inner_DataUtil poll_BitSetUtil_DataUti();

   void cancel();

   default boolean c_() {
      return false;
   }

   default BitSetUtil init_BitSetUtil() {
      return this;
   }

   default boolean sync() {
      return false;
   }

   default boolean needsAccess() {
      return true;
   }

   default boolean b_() {
      return false;
   }

   default Set<BlockPos> init_Set() {
      return Set.of(this.position());
   }

   default Set<BlockPos> load_Set() {
      return Set.of();
   }

   default boolean init_Z_BlockPos_BlockState(BlockPos position, BlockState actual) {
      return false;
   }

   default boolean init(AABB body) {
      return true;
   }

   default BitSetUtil.Inner_DataUtil init_BitSetUtil_DataUti() {
      return BitSetUtil.Inner_DataUtil.update;
   }

   default int load_I() {
      return 1;
   }

   default int read_I() {
      return 0;
   }

   default int write_I() {
      return 0;
   }

   default boolean close() {
      return false;
   }

   default String init_Str() {
      return "{}";
   }

   default void init(Supplier<String> guard) {
   }

   default void init(Map<Item, Integer> removed) {
   }

   default void init(StateCache order) {
   }

   default boolean load_Z() {
      return false;
   }

   default boolean read_Z() {
      return false;
   }

   default void init_V() {
   }

   default boolean write_Z() {
      return false;
   }

   default boolean update_Z() {
      return false;
   }

   default ItemStack init_ItemStack() {
      return ItemStack.EMPTY;
   }

   default void load_V() {
   }

   default void read_V() {
   }

   default int init_I() {
      return 0;
   }

   default int skippedSlots() {
      return 0;
   }

   default List<String> skippedReasons() {
      return List.of();
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close;
   }
}
