package autismclient.ai.b;

import autismclient.ai.a.NodeWalker;
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

public interface EventBus {
   long init = -9223372036854775808L;
   long load = Long.getLong("autism.ai.print.workPerTick", 300000L);

   BlockState init_BlockState_BlockPos(BlockPos var1);

   default BlockEntity init_BlockEntity_BlockPos(BlockPos pos) {
      return null;
   }

   default Object init_Obj() {
      return this;
   }

   default EventBus.Inner_StreamUtil init_EventBus_StreamUti() {
      return EventBus.Inner_StreamUtil.init;
   }

   default List<MethodUtil.Inner_DataUtil> load_List(BlockPos rail) {
      return null;
   }

   default List<RingBuffer.Inner_DataUtil> init_List_BlockPos(BlockPos cell) {
      return null;
   }

   default String init_Str_BlockPos(BlockPos cell) {
      return null;
   }

   boolean load_Z(BlockPos var1);

   int init_I();

   int load_I();

   default void load(LongConsumer changed) {
   }

   default void init_V_EventBus_DataUtil(EventBus.Inner_DataUtil changed) {
      this.load(key -> changed.changed(key, null));
   }

   default boolean init_Z_EventBus_DataUtil_I(EventBus.Inner_DataUtil changed, int budget) {
      this.init_V_EventBus_DataUtil(changed);
      return true;
   }

   default void init_V_LongConsumer(LongConsumer changed) {
   }

   NodeWalker.Inner_StreamUtil init_NodeWalker_StreamU();

   NodeWalker.Inner_CachePool init_NodeWalker_CachePo();

   float init_F();

   float load_F();

   double init_D();

   float read_F();

   Vec3 init_Vec3();

   float write_F();

   float update_F();

   boolean load_Z();

   default long read_J() {
      return load;
   }

   default boolean init_Z_BlockPos_Map(BlockPos position, Map<Integer, ItemStack> saved) {
      return false;
   }

   ItemStack init_ItemStack_I(int var1);

   int read_I();

   default boolean read_Z() {
      return true;
   }

   default boolean update_Z() {
      return false;
   }

   default boolean init_Z_ItemStack(ItemStack stack) {
      return false;
   }

   default boolean sync_Z() {
      return this.flush();
   }

   default boolean flush() {
      return false;
   }

   default boolean close() {
      return false;
   }

   default boolean open() {
      return false;
   }

   default boolean poll() {
      return false;
   }

   default float sync_F() {
      return 0.05F;
   }

   default boolean push() {
      return false;
   }

   default Boolean init_Boolean_F_F(float yaw, float pitch) {
      return null;
   }

   default long load_J() {
      return this.init_J() * 50000000L;
   }

   default boolean pop() {
      return false;
   }

   default void init_V_Item(Item item) {
   }

   default String load_Str() {
      return null;
   }

   default void init_V_BlockPos_BlockState(BlockPos position, BlockState state) {
   }

   default void init_V_BlockPos(BlockPos position) {
   }

   BlockState init(Item var1, BlockHitResult var2, float var3, float var4, boolean var5, BlockPos var6);

   boolean init_Z_BlockState_BlockPos(BlockState var1, BlockPos var2);

   void init_V_SyncManager(SyncManager var1);

   long init_J();

   void init_V_Str(String var1);

   default Vec3 load_Vec3() {
      Vec3 f = this.init_Vec3();
      return new Vec3(f.x, f.y + (this.load_Z() ? 1.27 : 1.62), f.z);
   }

   default AABB init_AABB() {
      Vec3 feet = this.init_Vec3();
      return IoWorker.init(feet.x, feet.y, feet.z, this.load_Z());
   }

   default Vec3 read_Vec3() {
      NodeWalker.Inner_StreamUtil b = this.init_NodeWalker_StreamU();
      return new Vec3(b.x(), b.y() + this.init_D(), b.z());
   }

   interface Inner_DataUtil {
      void changed(long var1, BlockState var3);
   }

   enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}
