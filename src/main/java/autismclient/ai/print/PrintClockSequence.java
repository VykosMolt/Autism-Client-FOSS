package autismclient.ai.print;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class PrintClockSequence implements PrintMachineJob {
   private final PrintEnv init_EventBus;
   private final Object init_Obj;
   private final PrintClockSpec init_NetHelper;
   private final PrintClockSequence.Inner_DataUtil init_TaskQueue_DataUtil;
   private PrintClockSequence.Inner_StreamUtil init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.INSPECT_FIRST;
   private PrintMachineJob init_BitSetUtil;
   private PrintMachineJob.Inner_DataUtil init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update;
   private int init_I;
   private int load_I;
   private int read_I;
   private int write_I = -1;
   private long init_J;
   private long load_J;
   private long read_J = -9223372036854775808L;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private String init_Str = "Inspecting the closed hopper clock before activation";
   private Supplier<String> init_Supplier = () -> null;
   private final ArrayList<JsonObject> init_ArrayList = new ArrayList<>();

   public PrintClockSequence(PrintEnv env, PrintClockSpec spec, PrintClockSequence.Inner_DataUtil actions) {
      this.init_EventBus = env;
      this.init_Obj = env.init_Obj();
      this.init_NetHelper = spec;
      this.init_TaskQueue_DataUtil = actions;
      this.init_BitSetUtil = actions.init(spec.preload(), spec.source().get(spec.preload()));
   }

   public PrintClockSpec init_NetHelper() {
      return this.init_NetHelper;
   }

   public boolean flush() {
      return this.init_TaskQueue_StreamUt == PrintClockSequence.Inner_StreamUtil.AUDIT && this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.PREPARING;
   }

   public BlockPos init_BlockPos_2() {
      for (BlockPos at : this.init_NetHelper.source().keySet()) {
         if (!this.init_EventBus.load_Z(at)) {
            return at;
         }
      }

      return null;
   }

   @Override
   public boolean init(AABB body) {
      AABB footing = body.expandTowards(0.0, -0.1, 0.0);
      if (this.init_NetHelper.sweptVolume().intersects(footing)) {
         return false;
      }

      for (BlockPos at : this.init_NetHelper.outputs()) {
         Block block = this.init_NetHelper.source().get(at).getBlock();
         if ((block instanceof TrapDoorBlock || block instanceof DoorBlock) && new AABB(at).intersects(footing)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public BlockPos position() {
      return this.init_NetHelper.preload();
   }

   @Override
   public Item item() {
      return this.init_BitSetUtil == null ? Items.AIR : this.init_BitSetUtil.item();
   }

   @Override
   public PrintMachineJob init_BitSetUtil() {
      return this.init_BitSetUtil == null ? this : this.init_BitSetUtil;
   }

   @Override
   public Set<BlockPos> init_Set() {
      return Set.of(this.init_NetHelper.preload(), this.init_NetHelper.other(), this.init_NetHelper.lever());
   }

   @Override
   public String description() {
      return "closed hopper-clock startup";
   }

   @Override
   public String detail() {
      return this.init_Str;
   }

   @Override
   public boolean matchesSource(BlockState source) {
      return source == this.init_NetHelper.source().get(this.init_NetHelper.preload());
   }

   @Override
   public int load_I() {
      return 2;
   }

   @Override
   public int skippedSlots() {
      return this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.SKIPPED ? this.init_NetHelper.savedSlots() : 0;
   }

   @Override
   public List<String> skippedReasons() {
      return this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.SKIPPED ? List.of(this.init_Str) : List.of();
   }

   @Override
   public int read_I() {
      return this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.SUCCEEDED ? this.init_NetHelper.quantity() : 0;
   }

   @Override
   public int write_I() {
      return this.init_I;
   }

   @Override
   public Set<BlockPos> load_Set() {
      return this.init_NetHelper.dynamicCells();
   }

   @Override
   public boolean init_Z_BlockPos_BlockState(BlockPos at, BlockState actual) {
      return !this.init_Z || !this.open() || !this.init_NetHelper.allows(at, actual, !this.load_Z, true)
         ? false
         : !actual.is(Blocks.MOVING_PISTON)
            || this.init_EventBus.init_BlockEntity_BlockPos(at) instanceof PistonMovingBlockEntity moving
               && this.init_NetHelper.allowsMovingPayload(at, moving.getMovedState());
   }

   @Override
   public void init(Supplier<String> guard) {
      this.init_Supplier = Objects.requireNonNull(guard);
   }

   @Override
   public PrintMachineJob.Inner_DataUtil prepare() {
      if (this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.SENT || this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.NEEDS_SUPPLY || this.poll_Z()) {
         return this.init_BitSetUtil_DataUti;
      } else if (!this.open()) {
         return this.init("Clock world changed before the next native action");
      } else {
         String changed = this.load_Str();
         if (changed != null) {
            return this.init(changed);
         } else if (this.init_TaskQueue_StreamUt == PrintClockSequence.Inner_StreamUtil.STOCK) {
            return this.read_BitSetUtil_DataUti();
         } else if (this.init_TaskQueue_StreamUt == PrintClockSequence.Inner_StreamUtil.AUDIT) {
            return this.write_BitSetUtil_DataUti();
         } else {
            PrintMachineJob.Inner_DataUtil result = this.init_BitSetUtil.prepare();
            if (result == PrintMachineJob.Inner_DataUtil.SUCCEEDED) {
               return this.load_BitSetUtil_DataUti_2();
            } else if (result != PrintMachineJob.Inner_DataUtil.CONFLICT && result != PrintMachineJob.Inner_DataUtil.UNCERTAIN) {
               this.init_Str = this.init_BitSetUtil.detail();
               return this.init_BitSetUtil_DataUti = result;
            } else {
               return this.init(this.init_BitSetUtil.detail());
            }
         }
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.prepare() != PrintMachineJob.Inner_DataUtil.READY
         || this.init_BitSetUtil == null
         || this.init_NetHelper.sweptVolume().intersects(this.init_EventBus.init_AABB())
         || this.init_NetHelper.sweptVolume().intersects(this.init_EventBus.init_NodeWalker_StreamU().box(this.init_EventBus.load_Z()))) {
         return false;
      }

      if (this.init_TaskQueue_StreamUt == PrintClockSequence.Inner_StreamUtil.HOLD
         && this.init_TaskQueue_DataUtil.init_I(this.init_NetHelper.material()) < this.init_NetHelper.quantity()) {
         this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.STOCK;
         this.init_BitSetUtil = null;
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
         return false;
      }

      if ((this.init_TaskQueue_StreamUt == PrintClockSequence.Inner_StreamUtil.FILL || this.init_TaskQueue_StreamUt == PrintClockSequence.Inner_StreamUtil.INSPECT_HELD_SECOND)
         && !this.init_NetHelper.holding(this.init_EventBus::init_BlockState_BlockPos)) {
         this.init("The protected holding phase changed before clock preload");
         return false;
      }

      if (!this.init_BitSetUtil.beginDispatch()) {
         return false;
      }

      if (this.init_TaskQueue_StreamUt == PrintClockSequence.Inner_StreamUtil.HOLD) {
         this.init_Z = true;
         this.load_J = this.init_EventBus.init_J();
      }

      this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.SENT;
      return true;
   }

   @Override
   public PrintMachineJob.Inner_DataUtil poll_BitSetUtil_DataUti() {
      if (this.init_BitSetUtil_DataUti != PrintMachineJob.Inner_DataUtil.SENT) {
         return this.init_BitSetUtil_DataUti;
      } else if (!this.open()) {
         return this.init("Clock world changed with a native action outstanding");
      } else {
         String changed = this.load_Str();
         if (changed != null) {
            return this.init(changed);
         } else {
            PrintMachineJob.Inner_DataUtil result = this.init_BitSetUtil.poll_BitSetUtil_DataUti();
            this.init_Str = this.init_BitSetUtil.detail();
            if (result == PrintMachineJob.Inner_DataUtil.SENT) {
               return this.init_BitSetUtil_DataUti;
            } else {
               return result == PrintMachineJob.Inner_DataUtil.SUCCEEDED
                  ? this.load_BitSetUtil_DataUti_2()
                  : this.init("Clock action did not complete: " + this.init_BitSetUtil.detail());
            }
         }
      }
   }

   private PrintMachineJob.Inner_DataUtil load_BitSetUtil_DataUti_2() {
      if (this.init_BitSetUtil != null) {
         this.init_ArrayList.add(this.init(this.init_BitSetUtil));
      }

      this.init_BitSetUtil = null;
      this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
      switch (this.init_TaskQueue_StreamUt) {
         case INSPECT_FIRST:
            this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.INSPECT_SECOND;
            this.init_BitSetUtil = this.init_TaskQueue_DataUtil
               .init(this.init_NetHelper.other(), this.init_NetHelper.source().get(this.init_NetHelper.other()));
            break;
         case INSPECT_SECOND:
            this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.STOCK;
            break;
         case STOCK:
         default:
            return this.init("Unexpected clock action boundary");
         case HOLD:
            if (!this.init_NetHelper.holding(this.init_EventBus::init_BlockState_BlockPos)) {
               return this.init("Native lever receipt did not establish the protected clock phase");
            }

            this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.INSPECT_HELD_SECOND;
            this.init_BitSetUtil = this.init_TaskQueue_DataUtil
               .init(this.init_NetHelper.other(), this.init_NetHelper.source().get(this.init_NetHelper.other()));
            break;
         case INSPECT_HELD_SECOND:
            this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.FILL;
            this.init_BitSetUtil = this.init_TaskQueue_DataUtil
               .init(this.init_NetHelper.preload(), this.init_NetHelper.source().get(this.init_NetHelper.preload()), this.init_NetHelper.preloadSlots());
            this.init_BitSetUtil
               .init(
                  () -> {
                     String changed = this.load_Str();
                     if (changed != null) {
                        return changed;
                     } else {
                        return !this.init_NetHelper.holding(this.init_EventBus::init_BlockState_BlockPos)
                           ? "Clock preload lost its opposing head or hopper lock"
                           : this.init_Supplier.get();
                     }
                  }
               );
            break;
         case FILL:
            this.read_Z = true;
            this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.RESTORE;
            this.init_BitSetUtil = this.init_TaskQueue_DataUtil.init(false);
            break;
         case RESTORE:
            this.load_Z = true;
            this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.AUDIT;
            this.init_J = this.init_EventBus.init_J();
            this.load_I = -1;
            this.init_I = 0;
      }

      this.init_Str = "Clock startup: " + this.init_TaskQueue_StreamUt.name().toLowerCase(Locale.ROOT);
      return this.init_BitSetUtil_DataUti;
   }

   private PrintMachineJob.Inner_DataUtil read_BitSetUtil_DataUti() {
      if (this.init_EventBus.init_Z_BlockPos_Map(this.init_NetHelper.preload(), this.init_NetHelper.preloadSlots())) {
         this.init_Str = "Waiting for the qualification inventory supply receipt";
         return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
      }

      int available = this.init_TaskQueue_DataUtil.init_I(this.init_NetHelper.material());
      int missing = this.init_NetHelper.quantity() - available;
      if (missing <= 0) {
         this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.HOLD;
         this.init_BitSetUtil = this.init_TaskQueue_DataUtil.init(true);
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
         return this.prepare();
      }

      if (this.init_TaskQueue_DataUtil.init()) {
         if (available != this.write_I
            && ++this.read_I <= 128
            && this.init_TaskQueue_DataUtil
               .init_Z(this.init_NetHelper.material().copyWithCount(Math.min(missing, this.init_NetHelper.material().getMaxStackSize())))) {
            this.write_I = available;
            this.init_Str = "Preparing the complete exact clock stock";
            return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
         } else {
            return this.init("Free inventory space for exact clock stock");
         }
      } else if (!this.write_Z && this.init_TaskQueue_DataUtil.load(this.init_NetHelper.material())) {
         this.init_ItemStack = this.init_NetHelper.material().copyWithCount(Math.min(missing, this.init_NetHelper.material().getMaxStackSize()));
         this.init_Str = "Fetching exact clock stock before touching its control";
         return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.NEEDS_SUPPLY;
      } else {
         this.init_Str = "Clock not started: missing "
            + missing
            + " of "
            + this.init_NetHelper.quantity()
            + " exact "
            + BuiltInRegistries.ITEM.getKey(this.init_NetHelper.material().getItem());
         return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.SKIPPED;
      }
   }

   @Override
   public ItemStack init_ItemStack() {
      return this.init_ItemStack.copy();
   }

   @Override
   public void load_V() {
      if (this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.NEEDS_SUPPLY) {
         this.write_V();
      }
   }

   @Override
   public void read_V() {
      if (this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.NEEDS_SUPPLY) {
         this.write_Z = true;
         this.write_V();
      }
   }

   private void write_V() {
      this.init_ItemStack = ItemStack.EMPTY;
      this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.INSPECT_FIRST;
      this.init_BitSetUtil = this.init_TaskQueue_DataUtil.init(this.init_NetHelper.preload(), this.init_NetHelper.source().get(this.init_NetHelper.preload()));
      this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
   }

   private PrintMachineJob.Inner_DataUtil write_BitSetUtil_DataUti() {
      int phase = this.init_NetHelper.phase(this.init_EventBus::init_BlockState_BlockPos);
      if (phase < 0) {
         if (this.read_J == -9223372036854775808L) {
            this.read_J = this.init_EventBus.init_J();
         }

         if (this.init_EventBus.init_J() - this.read_J > 20L) {
            return this.init("Clock motion did not settle into either complete phase");
         }
      } else {
         this.read_J = -9223372036854775808L;
         if (phase != this.load_I) {
            this.load_I = phase;
            this.init_I++;
         }
      }

      if (this.init_I >= 5) {
         this.init_TaskQueue_StreamUt = PrintClockSequence.Inner_StreamUtil.COMPLETE;
         this.init_Str = "Exact pooled clock stock loaded, source control restored, two native cycles observed";
         return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.SUCCEEDED;
      }

      if (this.init_EventBus.init_J() - this.init_J > this.init_NetHelper.quantity() * 32L + 200L) {
         return this.init("Clock did not complete two native cycles within its stock-derived bound");
      }

      this.init_Str = "Observing native clock phases "
         + this.init_I
         + "/5; "
         + this.init_NetHelper.quantity()
         + " exact items were acknowledged before release";
      return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.PREPARING;
   }

   @Override
   public PrintMachineJob.Inner_DataUtil init_BitSetUtil_DataUti() {
      if (this.init_BitSetUtil_DataUti != PrintMachineJob.Inner_DataUtil.SUCCEEDED) {
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.open()) {
         return this.init("Clock world changed before final audit");
      }

      String changed = this.load_Str();
      if (changed != null) {
         return this.init(changed);
      }

      if (this.init_NetHelper.phase(this.init_EventBus::init_BlockState_BlockPos) >= 0) {
         this.read_J = -9223372036854775808L;
         return PrintMachineJob.Inner_DataUtil.SUCCEEDED;
      }

      if (this.read_J == -9223372036854775808L) {
         this.read_J = this.init_EventBus.init_J();
      }

      return this.init_EventBus.init_J() - this.read_J > 20L ? this.init("Clock phase became invalid during final audit") : PrintMachineJob.Inner_DataUtil.PREPARING;
   }

   private String load_Str() {
      for (Entry<BlockPos, BlockState> entry : this.init_NetHelper.source().entrySet()) {
         BlockPos at = entry.getKey();
         if (!this.init_EventBus.load_Z(at)) {
            return "Clock source chunk is unavailable at " + at.toShortString();
         }

         BlockState actual = this.init_EventBus.init_BlockState_BlockPos(at);
         if (this.init_Z && this.init_NetHelper.movingCell(at) && actual.is(Blocks.MOVING_PISTON)) {
            if (!(this.init_EventBus.init_BlockEntity_BlockPos(at) instanceof PistonMovingBlockEntity moving)) {
               return "Clock piston motion has no native block-entity receipt";
            }

            BlockState var7 = moving.getMovedState();
            if (!this.init_NetHelper.allowsMovingPayload(at, var7)) {
               return "Clock piston is moving a foreign payload";
            }
         }

         if ((
               !this.init_Z
                  || this.init_TaskQueue_StreamUt != PrintClockSequence.Inner_StreamUtil.HOLD
                  || this.init_EventBus.init_J() - this.load_J > 20L
                  || !at.equals(this.init_NetHelper.secondPayload())
                  || !actual.isAir()
            )
            && (this.init_Z || !at.equals(this.init_NetHelper.secondPayload()) || !actual.isAir() && actual != entry.getValue())) {
            if (!this.init_Z && at.equals(this.init_NetHelper.firstPayload()) && actual != entry.getValue()) {
               return "Clock redstone payload is absent or changed";
            }

            if ((
                  !this.init_Z
                     || !at.equals(this.init_NetHelper.lever())
                     || this.init_TaskQueue_StreamUt != PrintClockSequence.Inner_StreamUtil.HOLD && this.init_TaskQueue_StreamUt != PrintClockSequence.Inner_StreamUtil.RESTORE
                     || actual != entry.getValue()
               )
               && !this.init_NetHelper.allows(at, actual, this.init_Z && !this.load_Z, this.init_Z)) {
               return "Clock source changed at " + at.toShortString();
            }
         }
      }

      return null;
   }

   private boolean open() {
      return !this.update && this.init_Obj != null && this.init_EventBus.init_Obj() == this.init_Obj && this.init_TaskQueue_DataUtil.load();
   }

   private boolean poll_Z() {
      return this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.SUCCEEDED
         || this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.SKIPPED
         || this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.CONFLICT
         || this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.UNCERTAIN;
   }

   private PrintMachineJob.Inner_DataUtil init(String message) {
      this.init_Str = message;
      return this.init_BitSetUtil_DataUti = !this.init_Z && this.init_BitSetUtil_DataUti != PrintMachineJob.Inner_DataUtil.SENT
         ? PrintMachineJob.Inner_DataUtil.CONFLICT
         : PrintMachineJob.Inner_DataUtil.UNCERTAIN;
   }

   @Override
   public void cancel() {
      if (!this.update) {
         this.update = true;
         if (this.init_BitSetUtil != null) {
            this.init_BitSetUtil.cancel();
         }

         if (this.init_Z && !this.load_Z || this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.SENT) {
            this.init("Clock startup interrupted; inspect the retained control and hopper receipts before restarting");
         }
      }
   }

   @Override
   public boolean close() {
      return this.init_Z && !this.load_Z || this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.UNCERTAIN;
   }

   @Override
   public String init_Str() {
      JsonObject data = new JsonObject();
      data.addProperty("clockStartup", true);
      data.addProperty("stage", this.init_TaskQueue_StreamUt.name());
      data.addProperty("recoveryRequired", this.close());
      data.addProperty("originalControlRestored", this.load_Z);
      data.addProperty("pooledStockVerified", this.read_Z);
      data.addProperty("pooledQuantity", this.init_NetHelper.quantity());
      data.addProperty("phasesObserved", this.init_I);
      data.addProperty("lever", this.init_NetHelper.lever().toShortString());
      data.addProperty("preloadHopper", this.init_NetHelper.preload().toShortString());
      data.addProperty("otherHopper", this.init_NetHelper.other().toShortString());
      data.addProperty("requiredOriginalLeverPowered", false);
      data.addProperty("detail", this.init_Str);
      JsonArray receipts = new JsonArray();
      this.init_ArrayList.forEach(receipts::add);
      data.add("completedNativeActions", receipts);
      if (this.init_BitSetUtil != null) {
         data.add("currentNativeAction", this.init(this.init_BitSetUtil));
      }

      return data.toString();
   }

   private JsonObject init(PrintMachineJob action) {
      JsonObject receipt = new JsonObject();
      receipt.addProperty("stage", this.init_TaskQueue_StreamUt.name());
      receipt.addProperty("position", action.position().toShortString());
      receipt.addProperty("description", action.description());
      receipt.addProperty("detail", action.detail());
      receipt.addProperty("verifiedSlots", action.init_I());
      receipt.add("nativeReceipt", JsonParser.parseString(action.init_Str()));
      return receipt;
   }

   public interface Inner_DataUtil {
      PrintMachineJob init(BlockPos var1, BlockState var2);

      PrintMachineJob init(boolean var1);

      PrintMachineJob init(BlockPos var1, BlockState var2, Map<Integer, ItemStack> var3);

      int init_I(ItemStack var1);

      boolean init();

      boolean init_Z(ItemStack var1);

      boolean load(ItemStack var1);

      boolean load();
   }

   enum Inner_StreamUtil {
      INSPECT_FIRST,
      INSPECT_SECOND,
      STOCK,
      HOLD,
      INSPECT_HELD_SECOND,
      FILL,
      RESTORE,
      AUDIT,
      COMPLETE;
   }
}
