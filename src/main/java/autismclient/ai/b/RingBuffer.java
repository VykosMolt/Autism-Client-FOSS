package autismclient.ai.b;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class RingBuffer implements BitSetUtil {
   private final EventBus init_EventBus;
   private final RingBuffer.Inner_StreamUtil init_RingBuffer_StreamU;
   private final Object init_Obj;
   private final Item init_Item;
   private RingBuffer.Inner_CachePool init_RingBuffer_CachePo = RingBuffer.Inner_CachePool.init;
   private BitSetUtil.Inner_DataUtil init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.init;
   private UUID init_UUID;
   private Set<UUID> init_Set = Set.of();
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int init_I;
   private long init_J;
   private String init_Str = "Waiting for the frame's block and loaded entities";

   public RingBuffer(EventBus env, RingBuffer.Inner_StreamUtil spec) {
      this.init_EventBus = env;
      this.init_RingBuffer_StreamU = spec;
      this.init_Obj = env.init_Obj();
      this.init_Item = spec.entityId().equals("minecraft:glow_item_frame") ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME;
   }

   public RingBuffer.Inner_StreamUtil init_RingBuffer_StreamU() {
      return this.init_RingBuffer_StreamU;
   }

   public UUID init_UUID_2() {
      return this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.init ? null : this.init_UUID;
   }

   @Override
   public BlockPos position() {
      return this.init_RingBuffer_StreamU.support();
   }

   @Override
   public Item item() {
      return this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.init
         ? this.init_Item
         : (this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.load ? this.init_RingBuffer_StreamU.item().getItem() : Items.AIR);
   }

   @Override
   public String description() {
      return "item frame";
   }

   @Override
   public String detail() {
      return this.init_Str;
   }

   @Override
   public boolean c_() {
      return true;
   }

   @Override
   public int load_I() {
      return 0;
   }

   public Direction init_Direction() {
      return this.init_RingBuffer_StreamU.facing();
   }

   @Override
   public boolean matchesSource(BlockState source) {
      return !source.isAir() && (source.isSolid() || source.getBlock() instanceof DiodeBlock);
   }

   private boolean open() {
      return this.init_Obj != null && this.init_Obj == this.init_EventBus.init_Obj();
   }

   private static boolean init_Z_ItemStack_ItemStack(ItemStack a, ItemStack b) {
      return a.isEmpty() ? b.isEmpty() : !b.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
   }

   private RingBuffer.Inner_DataUtil init(List<RingBuffer.Inner_DataUtil> seen) {
      RingBuffer.Inner_DataUtil found = null;

      for (RingBuffer.Inner_DataUtil o : seen) {
         if (o.facing() == this.init_RingBuffer_StreamU.facing()) {
            if (found != null) {
               this.init_Str = "Two frames face the same way in " + this.init_RingBuffer_StreamU.cell().toShortString();
               return null;
            }

            found = o;
         }
      }

      return found;
   }

   @Override
   public BitSetUtil.Inner_DataUtil prepare() {
      if (this.init_BitSetUtil_DataUti != BitSetUtil.Inner_DataUtil.init && this.init_BitSetUtil_DataUti != BitSetUtil.Inner_DataUtil.load) {
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.open()) {
         this.init_Str = "World changed before the frame was hung";
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
      }

      if (!this.init_EventBus.load_Z(this.init_RingBuffer_StreamU.cell()) || !this.init_EventBus.load_Z(this.init_RingBuffer_StreamU.support())) {
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.init;
      }

      if (!this.matchesSource(this.init_EventBus.init_BlockState_BlockPos(this.init_RingBuffer_StreamU.support()))) {
         this.init_Str = "The block the frame hangs on is missing at " + this.init_RingBuffer_StreamU.support().toShortString();
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
      }

      List<RingBuffer.Inner_DataUtil> seen = this.init_EventBus.init_List_BlockPos(this.init_RingBuffer_StreamU.cell());
      if (seen == null) {
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.init;
      }

      RingBuffer.Inner_DataUtil ours = this.init(seen);
      if (ours == null && seen.stream().filter(o -> o.facing() == this.init_RingBuffer_StreamU.facing()).count() > 1L) {
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
      }

      if (ours == null) {
         if (!this.init_EventBus.init_BlockState_BlockPos(this.init_RingBuffer_StreamU.cell()).canBeReplaced()) {
            this.init_Str = "A block fills the frame's cell " + this.init_RingBuffer_StreamU.cell().toShortString();
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
         } else {
            this.init_RingBuffer_CachePo = RingBuffer.Inner_CachePool.init;
            this.init_UUID = null;
            this.init_Str = "The frame is ready to hang";
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.load;
         }
      } else if (!ours.entityId().equals(this.init_RingBuffer_StreamU.entityId())) {
         this.init_Str = "A different kind of frame hangs at " + this.init_RingBuffer_StreamU.cell().toShortString();
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
      } else {
         this.init_UUID = ours.uuid();
         if (this.init_RingBuffer_StreamU.undecodable() || this.init_RingBuffer_StreamU.item().isEmpty()) {
            this.init_Str = this.init_RingBuffer_StreamU.undecodable() ? "Frame hung; its saved item cannot be decoded in this world" : "Empty frame verified";
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.update;
         } else if (ours.item().isEmpty()) {
            this.init_RingBuffer_CachePo = RingBuffer.Inner_CachePool.load;
            this.init_Str = "The frame is ready for its item";
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.load;
         } else if (!init_Z_ItemStack_ItemStack(ours.item(), this.init_RingBuffer_StreamU.item())) {
            this.init_Str = "The frame at " + this.init_RingBuffer_StreamU.cell().toShortString() + " holds a different item";
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
         } else if (ours.rotation() != this.init_RingBuffer_StreamU.rotation()) {
            this.init_RingBuffer_CachePo = RingBuffer.Inner_CachePool.read;
            this.init_Str = "Turning the frame's item";
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.load;
         } else {
            this.init_Str = "Frame and item verified";
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.update;
         }
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.prepare() != BitSetUtil.Inner_DataUtil.load) {
         return false;
      }

      ItemStack held = this.init_EventBus.init_ItemStack_I(this.init_EventBus.read_I());
      if (this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.init && !held.is(this.init_Item)) {
         this.init_Str = "The frame item is not held";
         return false;
      }

      if (this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.load
         && !init_Z_ItemStack_ItemStack(held.copyWithCount(1), this.init_RingBuffer_StreamU.item().copyWithCount(1))) {
         this.init_Str = "The frame's item is not held";
         return false;
      }

      List<RingBuffer.Inner_DataUtil> seen = this.init_EventBus.init_List_BlockPos(this.init_RingBuffer_StreamU.cell());
      if (seen == null) {
         return false;
      }

      HashSet<UUID> ids = new HashSet<>();
      int rotation = 0;

      for (RingBuffer.Inner_DataUtil o : seen) {
         ids.add(o.uuid());
         if (o.uuid().equals(this.init_UUID)) {
            rotation = o.rotation();
         }
      }

      this.init_Set = Set.copyOf(ids);
      this.init_I = rotation;
      this.init_ItemStack = held.copy();
      this.init_J = this.init_EventBus.load_J();
      this.init_Str = this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.init
         ? "Waiting for the hung frame"
         : (this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.load ? "Waiting for the frame to take its item" : "Waiting for the frame's item to turn");
      this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.read;
      return true;
   }

   @Override
   public BitSetUtil.Inner_DataUtil poll_BitSetUtil_DataUti() {
      if (this.init_BitSetUtil_DataUti != BitSetUtil.Inner_DataUtil.read) {
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.open()) {
         this.init_Str = "World changed after a frame click; not repeating it";
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.close;
      }

      List<RingBuffer.Inner_DataUtil> seen = this.init_EventBus.init_List_BlockPos(this.init_RingBuffer_StreamU.cell());
      if (seen != null) {
         RingBuffer.Inner_DataUtil ours = this.init(seen);
         boolean var10000;
         switch (this.init_RingBuffer_CachePo) {
            case init:
               var10000 = ours != null && !this.init_Set.contains(ours.uuid());
               break;
            case load:
               var10000 = ours != null && ours.uuid().equals(this.init_UUID) && !ours.item().isEmpty();
               break;
            case read:
               var10000 = ours != null && ours.uuid().equals(this.init_UUID) && ours.rotation() != this.init_I;
               break;
            default:
               throw new MatchException(null, null);
         }

         boolean done = var10000;
         if (done) {
            if (this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.init) {
               this.init_UUID = ours.uuid();
            }

            this.init_Str = this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.init
               ? "Frame hung"
               : (this.init_RingBuffer_CachePo == RingBuffer.Inner_CachePool.load ? "Item set in the frame" : "Item turned");
            return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.init;
         }
      }

      if (this.init_EventBus.load_J() - this.init_J >= 5000000000L) {
         this.init_Str = "No receipt for the frame click at " + this.init_RingBuffer_StreamU.cell().toShortString() + "; not repeating it";
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.close;
      } else {
         return this.init_BitSetUtil_DataUti;
      }
   }

   @Override
   public BitSetUtil.Inner_DataUtil init_BitSetUtil_DataUti() {
      if (this.init_BitSetUtil_DataUti != BitSetUtil.Inner_DataUtil.update) {
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.open()) {
         this.init_Str = "World changed before the frame's final check";
         return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.close;
      }

      List<RingBuffer.Inner_DataUtil> seen = this.init_EventBus.init_List_BlockPos(this.init_RingBuffer_StreamU.cell());
      if (seen == null) {
         return BitSetUtil.Inner_DataUtil.init;
      }

      RingBuffer.Inner_DataUtil ours = this.init(seen);
      if (ours != null && ours.uuid().equals(this.init_UUID)) {
         return BitSetUtil.Inner_DataUtil.update;
      }

      this.init_Str = "The verified frame at " + this.init_RingBuffer_StreamU.cell().toShortString() + " is gone";
      return this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
   }

   @Override
   public void cancel() {
      if (this.init_BitSetUtil_DataUti == BitSetUtil.Inner_DataUtil.read) {
         this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.close;
         this.init_Str = "Frame click cancelled without a receipt; not repeating it";
      } else if (this.init_BitSetUtil_DataUti == BitSetUtil.Inner_DataUtil.init || this.init_BitSetUtil_DataUti == BitSetUtil.Inner_DataUtil.load) {
         this.init_BitSetUtil_DataUti = BitSetUtil.Inner_DataUtil.flush;
         this.init_Str = "Frame cancelled before its click";
      }
   }

   @Override
   public List<String> skippedReasons() {
      ArrayList<String> out = new ArrayList<>();
      if (this.init_RingBuffer_StreamU.invisible()) {
         out.add("frame at " + this.init_RingBuffer_StreamU.cell().toShortString() + " is invisible in the file (needs commands)");
      }

      if (this.init_RingBuffer_StreamU.fixed()) {
         out.add("frame at " + this.init_RingBuffer_StreamU.cell().toShortString() + " is fixed in the file (needs commands)");
      }

      if (this.init_RingBuffer_StreamU.undecodable()) {
         out.add("frame at " + this.init_RingBuffer_StreamU.cell().toShortString() + " holds an item this world cannot decode");
      }

      return out;
   }

   enum Inner_CachePool {
      init,
      load,
      read;
   }

   public record Inner_DataUtil(UUID uuid, String entityId, Direction facing, ItemStack item, int rotation) {
   }

   public record Inner_StreamUtil(
      String entityId, BlockPos cell, Direction facing, ItemStack item, int rotation, boolean invisible, boolean fixed, boolean undecodable
   ) {
      public Inner_StreamUtil(
         String entityId, BlockPos cell, Direction facing, ItemStack item, int rotation, boolean invisible, boolean fixed, boolean undecodable
      ) {
         if (entityId != null && cell != null && facing != null) {
            item = item == null ? ItemStack.EMPTY : item.copy();
            rotation = Math.floorMod(rotation, 8);
            this.entityId = entityId;
            this.cell = cell;
            this.facing = facing;
            this.item = item;
            this.rotation = rotation;
            this.invisible = invisible;
            this.fixed = fixed;
            this.undecodable = undecodable;
         } else {
            throw new IllegalArgumentException("Invalid saved item frame");
         }
      }

      public BlockPos support() {
         return this.cell.relative(this.facing.getOpposite());
      }
   }
}
