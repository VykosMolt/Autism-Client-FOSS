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
   private final PrintEnv env;
   private final Object worldToken;
   private final PrintClockSpec spec;
   private final PrintClockSequence.Inner_DataUtil actions;
   private PrintClockSequence.Inner_StreamUtil stage = PrintClockSequence.Inner_StreamUtil.INSPECT_FIRST;
   private PrintMachineJob currentJob;
   private PrintMachineJob.JobStatus jobStatus = PrintMachineJob.JobStatus.PREPARING;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean stopped;
   private int init_I;
   private int load_I;
   private int read_I;
   private int write_I = -1;
   private long init_J;
   private long load_J;
   private long read_J = -9223372036854775808L;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private String detail = "Inspecting the closed hopper clock before activation";
   private Supplier<String> init_Supplier = () -> null;
   private final ArrayList<JsonObject> receipts = new ArrayList<>();

   public PrintClockSequence(PrintEnv env, PrintClockSpec spec, PrintClockSequence.Inner_DataUtil actions) {
      this.env = env;
      this.worldToken = env.worldToken();
      this.spec = spec;
      this.actions = actions;
      this.currentJob = actions.containerJob(spec.preload(), spec.source().get(spec.preload()));
   }

   public PrintClockSpec getSpec() {
      return this.spec;
   }

   public boolean auditing() {
      return this.stage == PrintClockSequence.Inner_StreamUtil.AUDIT && this.jobStatus == PrintMachineJob.JobStatus.PREPARING;
   }

   public BlockPos firstUnloadedCell() {
      for (BlockPos at : this.spec.source().keySet()) {
         if (!this.env.hasChunkAt(at)) {
            return at;
         }
      }

      return null;
   }

   @Override
   public boolean bodyClear(AABB body) {
      AABB footing = body.expandTowards(0.0, -0.1, 0.0);
      if (this.spec.sweptVolume().intersects(footing)) {
         return false;
      }

      for (BlockPos at : this.spec.outputs()) {
         Block block = this.spec.source().get(at).getBlock();
         if ((block instanceof TrapDoorBlock || block instanceof DoorBlock) && new AABB(at).intersects(footing)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public BlockPos position() {
      return this.spec.preload();
   }

   @Override
   public Item item() {
      return this.currentJob == null ? Items.AIR : this.currentJob.item();
   }

   @Override
   public PrintMachineJob currentAction() {
      return this.currentJob == null ? this : this.currentJob;
   }

   @Override
   public Set<BlockPos> accessCells() {
      return Set.of(this.spec.preload(), this.spec.other(), this.spec.lever());
   }

   @Override
   public String description() {
      return "closed hopper-clock startup";
   }

   @Override
   public String detail() {
      return this.detail;
   }

   @Override
   public boolean matchesSource(BlockState source) {
      return source == this.spec.source().get(this.spec.preload());
   }

   @Override
   public int fillCount() {
      return 2;
   }

   @Override
   public int skippedSlots() {
      return this.jobStatus == PrintMachineJob.JobStatus.SKIPPED ? this.spec.savedSlots() : 0;
   }

   @Override
   public List<String> skippedReasons() {
      return this.jobStatus == PrintMachineJob.JobStatus.SKIPPED ? List.of(this.detail) : List.of();
   }

   @Override
   public int verifiedItems() {
      return this.jobStatus == PrintMachineJob.JobStatus.SUCCEEDED ? this.spec.quantity() : 0;
   }

   @Override
   public int completedPhases() {
      return this.init_I;
   }

   @Override
   public Set<BlockPos> dynamicCells() {
      return this.spec.dynamicCells();
   }

   @Override
   public boolean allowsDynamicState(BlockPos at, BlockState actual) {
      return !this.init_Z || !this.isCurrent() || !this.spec.allows(at, actual, !this.load_Z, true)
         ? false
         : !actual.is(Blocks.MOVING_PISTON)
            || this.env.getBlockEntity(at) instanceof PistonMovingBlockEntity moving
               && this.spec.allowsMovingPayload(at, moving.getMovedState());
   }

   @Override
   public void setGuard(Supplier<String> guard) {
      this.init_Supplier = Objects.requireNonNull(guard);
   }

   @Override
   public PrintMachineJob.JobStatus prepare() {
      if (this.jobStatus == PrintMachineJob.JobStatus.SENT || this.jobStatus == PrintMachineJob.JobStatus.NEEDS_SUPPLY || this.poll_Z()) {
         return this.jobStatus;
      } else if (!this.isCurrent()) {
         return this.report("Clock world changed before the next native action");
      } else {
         String changed = this.load_Str();
         if (changed != null) {
            return this.report(changed);
         } else if (this.stage == PrintClockSequence.Inner_StreamUtil.STOCK) {
            return this.read_BitSetUtil_DataUti();
         } else if (this.stage == PrintClockSequence.Inner_StreamUtil.AUDIT) {
            return this.write_BitSetUtil_DataUti();
         } else {
            PrintMachineJob.JobStatus result = this.currentJob.prepare();
            if (result == PrintMachineJob.JobStatus.SUCCEEDED) {
               return this.load_BitSetUtil_DataUti_2();
            } else if (result != PrintMachineJob.JobStatus.CONFLICT && result != PrintMachineJob.JobStatus.UNCERTAIN) {
               this.detail = this.currentJob.detail();
               return this.jobStatus = result;
            } else {
               return this.report(this.currentJob.detail());
            }
         }
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.prepare() != PrintMachineJob.JobStatus.READY
         || this.currentJob == null
         || this.spec.sweptVolume().intersects(this.env.serverPlayerBox())
         || this.spec.sweptVolume().intersects(this.env.playerMotionState().box(this.env.isServerSneaking()))) {
         return false;
      }

      if (this.stage == PrintClockSequence.Inner_StreamUtil.HOLD
         && this.actions.countHeld(this.spec.material()) < this.spec.quantity()) {
         this.stage = PrintClockSequence.Inner_StreamUtil.STOCK;
         this.currentJob = null;
         this.jobStatus = PrintMachineJob.JobStatus.PREPARING;
         return false;
      }

      if ((this.stage == PrintClockSequence.Inner_StreamUtil.FILL || this.stage == PrintClockSequence.Inner_StreamUtil.INSPECT_HELD_SECOND)
         && !this.spec.holding(this.env::getBlockState)) {
         this.report("The protected holding phase changed before clock preload");
         return false;
      }

      if (!this.currentJob.beginDispatch()) {
         return false;
      }

      if (this.stage == PrintClockSequence.Inner_StreamUtil.HOLD) {
         this.init_Z = true;
         this.load_J = this.env.currentTick();
      }

      this.jobStatus = PrintMachineJob.JobStatus.SENT;
      return true;
   }

   @Override
   public PrintMachineJob.JobStatus pollStatus() {
      if (this.jobStatus != PrintMachineJob.JobStatus.SENT) {
         return this.jobStatus;
      } else if (!this.isCurrent()) {
         return this.report("Clock world changed with a native action outstanding");
      } else {
         String changed = this.load_Str();
         if (changed != null) {
            return this.report(changed);
         } else {
            PrintMachineJob.JobStatus result = this.currentJob.pollStatus();
            this.detail = this.currentJob.detail();
            if (result == PrintMachineJob.JobStatus.SENT) {
               return this.jobStatus;
            } else {
               return result == PrintMachineJob.JobStatus.SUCCEEDED
                  ? this.load_BitSetUtil_DataUti_2()
                  : this.report("Clock action did not complete: " + this.currentJob.detail());
            }
         }
      }
   }

   private PrintMachineJob.JobStatus load_BitSetUtil_DataUti_2() {
      if (this.currentJob != null) {
         this.receipts.add(this.receiptFor(this.currentJob));
      }

      this.currentJob = null;
      this.jobStatus = PrintMachineJob.JobStatus.PREPARING;
      switch (this.stage) {
         case INSPECT_FIRST:
            this.stage = PrintClockSequence.Inner_StreamUtil.INSPECT_SECOND;
            this.currentJob = this.actions
               .containerJob(this.spec.other(), this.spec.source().get(this.spec.other()));
            break;
         case INSPECT_SECOND:
            this.stage = PrintClockSequence.Inner_StreamUtil.STOCK;
            break;
         case STOCK:
         default:
            return this.report("Unexpected clock action boundary");
         case HOLD:
            if (!this.spec.holding(this.env::getBlockState)) {
               return this.report("Native lever receipt did not establish the protected clock phase");
            }

            this.stage = PrintClockSequence.Inner_StreamUtil.INSPECT_HELD_SECOND;
            this.currentJob = this.actions
               .containerJob(this.spec.other(), this.spec.source().get(this.spec.other()));
            break;
         case INSPECT_HELD_SECOND:
            this.stage = PrintClockSequence.Inner_StreamUtil.FILL;
            this.currentJob = this.actions
               .filledContainerJob(this.spec.preload(), this.spec.source().get(this.spec.preload()), this.spec.preloadSlots());
            this.currentJob
               .setGuard(
                  () -> {
                     String changed = this.load_Str();
                     if (changed != null) {
                        return changed;
                     } else {
                        return !this.spec.holding(this.env::getBlockState)
                           ? "Clock preload lost its opposing head or hopper lock"
                           : this.init_Supplier.get();
                     }
                  }
               );
            break;
         case FILL:
            this.read_Z = true;
            this.stage = PrintClockSequence.Inner_StreamUtil.RESTORE;
            this.currentJob = this.actions.clockControlJob(false);
            break;
         case RESTORE:
            this.load_Z = true;
            this.stage = PrintClockSequence.Inner_StreamUtil.AUDIT;
            this.init_J = this.env.currentTick();
            this.load_I = -1;
            this.init_I = 0;
      }

      this.detail = "Clock startup: " + this.stage.name().toLowerCase(Locale.ROOT);
      return this.jobStatus;
   }

   private PrintMachineJob.JobStatus read_BitSetUtil_DataUti() {
      if (this.env.preloadContainerSlots(this.spec.preload(), this.spec.preloadSlots())) {
         this.detail = "Waiting for the qualification inventory supply receipt";
         return this.jobStatus = PrintMachineJob.JobStatus.PREPARING;
      }

      int available = this.actions.countHeld(this.spec.material());
      int missing = this.spec.quantity() - available;
      if (missing <= 0) {
         this.stage = PrintClockSequence.Inner_StreamUtil.HOLD;
         this.currentJob = this.actions.clockControlJob(true);
         this.jobStatus = PrintMachineJob.JobStatus.PREPARING;
         return this.prepare();
      }

      if (this.actions.creative()) {
         if (available != this.write_I
            && ++this.read_I <= 128
            && this.actions
               .giveCreativeStack(this.spec.material().copyWithCount(Math.min(missing, this.spec.material().getMaxStackSize())))) {
            this.write_I = available;
            this.detail = "Preparing the complete exact clock stock";
            return this.jobStatus = PrintMachineJob.JobStatus.PREPARING;
         } else {
            return this.report("Free inventory space for exact clock stock");
         }
      } else if (!this.write_Z && this.actions.canFetch(this.spec.material())) {
         this.init_ItemStack = this.spec.material().copyWithCount(Math.min(missing, this.spec.material().getMaxStackSize()));
         this.detail = "Fetching exact clock stock before touching its control";
         return this.jobStatus = PrintMachineJob.JobStatus.NEEDS_SUPPLY;
      } else {
         this.detail = "Clock not started: missing "
            + missing
            + " of "
            + this.spec.quantity()
            + " exact "
            + BuiltInRegistries.ITEM.getKey(this.spec.material().getItem());
         return this.jobStatus = PrintMachineJob.JobStatus.SKIPPED;
      }
   }

   @Override
   public ItemStack extraMaterial() {
      return this.init_ItemStack.copy();
   }

   @Override
   public void onContentsAvailable() {
      if (this.jobStatus == PrintMachineJob.JobStatus.NEEDS_SUPPLY) {
         this.write_V();
      }
   }

   @Override
   public void onContentsUnavailable() {
      if (this.jobStatus == PrintMachineJob.JobStatus.NEEDS_SUPPLY) {
         this.write_Z = true;
         this.write_V();
      }
   }

   private void write_V() {
      this.init_ItemStack = ItemStack.EMPTY;
      this.stage = PrintClockSequence.Inner_StreamUtil.INSPECT_FIRST;
      this.currentJob = this.actions.containerJob(this.spec.preload(), this.spec.source().get(this.spec.preload()));
      this.jobStatus = PrintMachineJob.JobStatus.PREPARING;
   }

   private PrintMachineJob.JobStatus write_BitSetUtil_DataUti() {
      int phase = this.spec.phase(this.env::getBlockState);
      if (phase < 0) {
         if (this.read_J == -9223372036854775808L) {
            this.read_J = this.env.currentTick();
         }

         if (this.env.currentTick() - this.read_J > 20L) {
            return this.report("Clock motion did not settle into either complete phase");
         }
      } else {
         this.read_J = -9223372036854775808L;
         if (phase != this.load_I) {
            this.load_I = phase;
            this.init_I++;
         }
      }

      if (this.init_I >= 5) {
         this.stage = PrintClockSequence.Inner_StreamUtil.COMPLETE;
         this.detail = "Exact pooled clock stock loaded, source control restored, two native cycles observed";
         return this.jobStatus = PrintMachineJob.JobStatus.SUCCEEDED;
      }

      if (this.env.currentTick() - this.init_J > this.spec.quantity() * 32L + 200L) {
         return this.report("Clock did not complete two native cycles within its stock-derived bound");
      }

      this.detail = "Observing native clock phases "
         + this.init_I
         + "/5; "
         + this.spec.quantity()
         + " exact items were acknowledged before release";
      return this.jobStatus = PrintMachineJob.JobStatus.PREPARING;
   }

   @Override
   public PrintMachineJob.JobStatus verifyStatus() {
      if (this.jobStatus != PrintMachineJob.JobStatus.SUCCEEDED) {
         return this.jobStatus;
      }

      if (!this.isCurrent()) {
         return this.report("Clock world changed before final audit");
      }

      String changed = this.load_Str();
      if (changed != null) {
         return this.report(changed);
      }

      if (this.spec.phase(this.env::getBlockState) >= 0) {
         this.read_J = -9223372036854775808L;
         return PrintMachineJob.JobStatus.SUCCEEDED;
      }

      if (this.read_J == -9223372036854775808L) {
         this.read_J = this.env.currentTick();
      }

      return this.env.currentTick() - this.read_J > 20L ? this.report("Clock phase became invalid during final audit") : PrintMachineJob.JobStatus.PREPARING;
   }

   private String load_Str() {
      for (Entry<BlockPos, BlockState> entry : this.spec.source().entrySet()) {
         BlockPos at = entry.getKey();
         if (!this.env.hasChunkAt(at)) {
            return "Clock source chunk is unavailable at " + at.toShortString();
         }

         BlockState actual = this.env.getBlockState(at);
         if (this.init_Z && this.spec.movingCell(at) && actual.is(Blocks.MOVING_PISTON)) {
            if (!(this.env.getBlockEntity(at) instanceof PistonMovingBlockEntity moving)) {
               return "Clock piston motion has no native block-entity receipt";
            }

            BlockState var7 = moving.getMovedState();
            if (!this.spec.allowsMovingPayload(at, var7)) {
               return "Clock piston is moving a foreign payload";
            }
         }

         if ((
               !this.init_Z
                  || this.stage != PrintClockSequence.Inner_StreamUtil.HOLD
                  || this.env.currentTick() - this.load_J > 20L
                  || !at.equals(this.spec.secondPayload())
                  || !actual.isAir()
            )
            && (this.init_Z || !at.equals(this.spec.secondPayload()) || !actual.isAir() && actual != entry.getValue())) {
            if (!this.init_Z && at.equals(this.spec.firstPayload()) && actual != entry.getValue()) {
               return "Clock redstone payload is absent or changed";
            }

            if ((
                  !this.init_Z
                     || !at.equals(this.spec.lever())
                     || this.stage != PrintClockSequence.Inner_StreamUtil.HOLD && this.stage != PrintClockSequence.Inner_StreamUtil.RESTORE
                     || actual != entry.getValue()
               )
               && !this.spec.allows(at, actual, this.init_Z && !this.load_Z, this.init_Z)) {
               return "Clock source changed at " + at.toShortString();
            }
         }
      }

      return null;
   }

   private boolean isCurrent() {
      return !this.stopped && this.worldToken != null && this.env.worldToken() == this.worldToken && this.actions.contextValid();
   }

   private boolean poll_Z() {
      return this.jobStatus == PrintMachineJob.JobStatus.SUCCEEDED
         || this.jobStatus == PrintMachineJob.JobStatus.SKIPPED
         || this.jobStatus == PrintMachineJob.JobStatus.CONFLICT
         || this.jobStatus == PrintMachineJob.JobStatus.UNCERTAIN;
   }

   private PrintMachineJob.JobStatus report(String message) {
      this.detail = message;
      return this.jobStatus = !this.init_Z && this.jobStatus != PrintMachineJob.JobStatus.SENT
         ? PrintMachineJob.JobStatus.CONFLICT
         : PrintMachineJob.JobStatus.UNCERTAIN;
   }

   @Override
   public void cancel() {
      if (!this.stopped) {
         this.stopped = true;
         if (this.currentJob != null) {
            this.currentJob.cancel();
         }

         if (this.init_Z && !this.load_Z || this.jobStatus == PrintMachineJob.JobStatus.SENT) {
            this.report("Clock startup interrupted; inspect the retained control and hopper receipts before restarting");
         }
      }
   }

   @Override
   public boolean resultUncertain() {
      return this.init_Z && !this.load_Z || this.jobStatus == PrintMachineJob.JobStatus.UNCERTAIN;
   }

   @Override
   public String receiptJson() {
      JsonObject data = new JsonObject();
      data.addProperty("clockStartup", true);
      data.addProperty("stage", this.stage.name());
      data.addProperty("recoveryRequired", this.resultUncertain());
      data.addProperty("originalControlRestored", this.load_Z);
      data.addProperty("pooledStockVerified", this.read_Z);
      data.addProperty("pooledQuantity", this.spec.quantity());
      data.addProperty("phasesObserved", this.init_I);
      data.addProperty("lever", this.spec.lever().toShortString());
      data.addProperty("preloadHopper", this.spec.preload().toShortString());
      data.addProperty("otherHopper", this.spec.other().toShortString());
      data.addProperty("requiredOriginalLeverPowered", false);
      data.addProperty("detail", this.detail);
      JsonArray receipts = new JsonArray();
      this.receipts.forEach(receipts::add);
      data.add("completedNativeActions", receipts);
      if (this.currentJob != null) {
         data.add("currentNativeAction", this.receiptFor(this.currentJob));
      }

      return data.toString();
   }

   private JsonObject receiptFor(PrintMachineJob action) {
      JsonObject receipt = new JsonObject();
      receipt.addProperty("stage", this.stage.name());
      receipt.addProperty("position", action.position().toShortString());
      receipt.addProperty("description", action.description());
      receipt.addProperty("detail", action.detail());
      receipt.addProperty("verifiedSlots", action.verifiedSlots());
      receipt.add("nativeReceipt", JsonParser.parseString(action.receiptJson()));
      return receipt;
   }

   public interface Inner_DataUtil {
      PrintMachineJob containerJob(BlockPos var1, BlockState var2);

      PrintMachineJob clockControlJob(boolean var1);

      PrintMachineJob filledContainerJob(BlockPos var1, BlockState var2, Map<Integer, ItemStack> var3);

      int countHeld(ItemStack var1);

      boolean creative();

      boolean giveCreativeStack(ItemStack var1);

      boolean canFetch(ItemStack var1);

      boolean contextValid();
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
