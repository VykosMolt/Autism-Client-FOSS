package autismclient.ai.print;

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

public final class PrintItemFrame implements PrintMachineJob {
   private final PrintEnv env;
   private final PrintItemFrame.FrameSpec spec;
   private final Object worldToken;
   private final Item frameItem;
   private PrintItemFrame.FrameStage stage = PrintItemFrame.FrameStage.HANG;
   private PrintMachineJob.JobStatus status = PrintMachineJob.JobStatus.PREPARING;
   private UUID frameId;
   private Set<UUID> init_Set = Set.of();
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int rotation;
   private long init_J;
   private String message = "Waiting for the frame's block and loaded entities";

   public PrintItemFrame(PrintEnv env, PrintItemFrame.FrameSpec spec) {
      this.env = env;
      this.spec = spec;
      this.worldToken = env.worldToken();
      this.frameItem = spec.entityId().equals("minecraft:glow_item_frame") ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME;
   }

   public PrintItemFrame.FrameSpec getSpec() {
      return this.spec;
   }

   public UUID frameEntityUuid() {
      return this.stage == PrintItemFrame.FrameStage.HANG ? null : this.frameId;
   }

   @Override
   public BlockPos position() {
      return this.spec.support();
   }

   @Override
   public Item item() {
      return this.stage == PrintItemFrame.FrameStage.HANG
         ? this.frameItem
         : (this.stage == PrintItemFrame.FrameStage.FILL ? this.spec.item().getItem() : Items.AIR);
   }

   @Override
   public String description() {
      return "item frame";
   }

   @Override
   public String detail() {
      return this.message;
   }

   @Override
   public boolean c_() {
      return true;
   }

   @Override
   public int fillCount() {
      return 0;
   }

   public Direction facing() {
      return this.spec.facing();
   }

   @Override
   public boolean matchesSource(BlockState source) {
      return !source.isAir() && (source.isSolid() || source.getBlock() instanceof DiodeBlock);
   }

   private boolean sameWorld() {
      return this.worldToken != null && this.worldToken == this.env.worldToken();
   }

   private static boolean sameItem(ItemStack a, ItemStack b) {
      return a.isEmpty() ? b.isEmpty() : !b.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
   }

   private PrintItemFrame.ObservedFrame findOurs(List<PrintItemFrame.ObservedFrame> seen) {
      PrintItemFrame.ObservedFrame found = null;

      for (PrintItemFrame.ObservedFrame o : seen) {
         if (o.facing() == this.spec.facing()) {
            if (found != null) {
               this.message = "Two frames face the same way in " + this.spec.cell().toShortString();
               return null;
            }

            found = o;
         }
      }

      return found;
   }

   @Override
   public PrintMachineJob.JobStatus prepare() {
      if (this.status != PrintMachineJob.JobStatus.PREPARING && this.status != PrintMachineJob.JobStatus.READY) {
         return this.status;
      }

      if (!this.sameWorld()) {
         this.message = "World changed before the frame was hung";
         return this.status = PrintMachineJob.JobStatus.CONFLICT;
      }

      if (!this.env.hasChunkAt(this.spec.cell()) || !this.env.hasChunkAt(this.spec.support())) {
         return this.status = PrintMachineJob.JobStatus.PREPARING;
      }

      if (!this.matchesSource(this.env.getBlockState(this.spec.support()))) {
         this.message = "The block the frame hangs on is missing at " + this.spec.support().toShortString();
         return this.status = PrintMachineJob.JobStatus.CONFLICT;
      }

      List<PrintItemFrame.ObservedFrame> seen = this.env.itemFramesAt(this.spec.cell());
      if (seen == null) {
         return this.status = PrintMachineJob.JobStatus.PREPARING;
      }

      PrintItemFrame.ObservedFrame ours = this.findOurs(seen);
      if (ours == null && seen.stream().filter(o -> o.facing() == this.spec.facing()).count() > 1L) {
         return this.status = PrintMachineJob.JobStatus.CONFLICT;
      }

      if (ours == null) {
         if (!this.env.getBlockState(this.spec.cell()).canBeReplaced()) {
            this.message = "A block fills the frame's cell " + this.spec.cell().toShortString();
            return this.status = PrintMachineJob.JobStatus.CONFLICT;
         } else {
            this.stage = PrintItemFrame.FrameStage.HANG;
            this.frameId = null;
            this.message = "The frame is ready to hang";
            return this.status = PrintMachineJob.JobStatus.READY;
         }
      } else if (!ours.entityId().equals(this.spec.entityId())) {
         this.message = "A different kind of frame hangs at " + this.spec.cell().toShortString();
         return this.status = PrintMachineJob.JobStatus.CONFLICT;
      } else {
         this.frameId = ours.uuid();
         if (this.spec.undecodable() || this.spec.item().isEmpty()) {
            this.message = this.spec.undecodable() ? "Frame hung; its saved item cannot be decoded in this world" : "Empty frame verified";
            return this.status = PrintMachineJob.JobStatus.SUCCEEDED;
         } else if (ours.item().isEmpty()) {
            this.stage = PrintItemFrame.FrameStage.FILL;
            this.message = "The frame is ready for its item";
            return this.status = PrintMachineJob.JobStatus.READY;
         } else if (!sameItem(ours.item(), this.spec.item())) {
            this.message = "The frame at " + this.spec.cell().toShortString() + " holds a different item";
            return this.status = PrintMachineJob.JobStatus.CONFLICT;
         } else if (ours.rotation() != this.spec.rotation()) {
            this.stage = PrintItemFrame.FrameStage.TURN;
            this.message = "Turning the frame's item";
            return this.status = PrintMachineJob.JobStatus.READY;
         } else {
            this.message = "Frame and item verified";
            return this.status = PrintMachineJob.JobStatus.SUCCEEDED;
         }
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.prepare() != PrintMachineJob.JobStatus.READY) {
         return false;
      }

      ItemStack held = this.env.getInventoryItem(this.env.getSelectedHotbarSlot());
      if (this.stage == PrintItemFrame.FrameStage.HANG && !held.is(this.frameItem)) {
         this.message = "The frame item is not held";
         return false;
      }

      if (this.stage == PrintItemFrame.FrameStage.FILL
         && !sameItem(held.copyWithCount(1), this.spec.item().copyWithCount(1))) {
         this.message = "The frame's item is not held";
         return false;
      }

      List<PrintItemFrame.ObservedFrame> seen = this.env.itemFramesAt(this.spec.cell());
      if (seen == null) {
         return false;
      }

      HashSet<UUID> ids = new HashSet<>();
      int rotation = 0;

      for (PrintItemFrame.ObservedFrame o : seen) {
         ids.add(o.uuid());
         if (o.uuid().equals(this.frameId)) {
            rotation = o.rotation();
         }
      }

      this.init_Set = Set.copyOf(ids);
      this.rotation = rotation;
      this.init_ItemStack = held.copy();
      this.init_J = this.env.nanoTime();
      this.message = this.stage == PrintItemFrame.FrameStage.HANG
         ? "Waiting for the hung frame"
         : (this.stage == PrintItemFrame.FrameStage.FILL ? "Waiting for the frame to take its item" : "Waiting for the frame's item to turn");
      this.status = PrintMachineJob.JobStatus.SENT;
      return true;
   }

   @Override
   public PrintMachineJob.JobStatus pollStatus() {
      if (this.status != PrintMachineJob.JobStatus.SENT) {
         return this.status;
      }

      if (!this.sameWorld()) {
         this.message = "World changed after a frame click; not repeating it";
         return this.status = PrintMachineJob.JobStatus.UNCERTAIN;
      }

      List<PrintItemFrame.ObservedFrame> seen = this.env.itemFramesAt(this.spec.cell());
      if (seen != null) {
         PrintItemFrame.ObservedFrame ours = this.findOurs(seen);
         boolean var10000;
         switch (this.stage) {
            case HANG:
               var10000 = ours != null && !this.init_Set.contains(ours.uuid());
               break;
            case FILL:
               var10000 = ours != null && ours.uuid().equals(this.frameId) && !ours.item().isEmpty();
               break;
            case TURN:
               var10000 = ours != null && ours.uuid().equals(this.frameId) && ours.rotation() != this.rotation;
               break;
            default:
               throw new MatchException(null, null);
         }

         boolean done = var10000;
         if (done) {
            if (this.stage == PrintItemFrame.FrameStage.HANG) {
               this.frameId = ours.uuid();
            }

            this.message = this.stage == PrintItemFrame.FrameStage.HANG
               ? "Frame hung"
               : (this.stage == PrintItemFrame.FrameStage.FILL ? "Item set in the frame" : "Item turned");
            return this.status = PrintMachineJob.JobStatus.PREPARING;
         }
      }

      if (this.env.nanoTime() - this.init_J >= 5000000000L) {
         this.message = "No receipt for the frame click at " + this.spec.cell().toShortString() + "; not repeating it";
         return this.status = PrintMachineJob.JobStatus.UNCERTAIN;
      } else {
         return this.status;
      }
   }

   @Override
   public PrintMachineJob.JobStatus verifyStatus() {
      if (this.status != PrintMachineJob.JobStatus.SUCCEEDED) {
         return this.status;
      }

      if (!this.sameWorld()) {
         this.message = "World changed before the frame's final check";
         return this.status = PrintMachineJob.JobStatus.UNCERTAIN;
      }

      List<PrintItemFrame.ObservedFrame> seen = this.env.itemFramesAt(this.spec.cell());
      if (seen == null) {
         return PrintMachineJob.JobStatus.PREPARING;
      }

      PrintItemFrame.ObservedFrame ours = this.findOurs(seen);
      if (ours != null && ours.uuid().equals(this.frameId)) {
         return PrintMachineJob.JobStatus.SUCCEEDED;
      }

      this.message = "The verified frame at " + this.spec.cell().toShortString() + " is gone";
      return this.status = PrintMachineJob.JobStatus.CONFLICT;
   }

   @Override
   public void cancel() {
      if (this.status == PrintMachineJob.JobStatus.SENT) {
         this.status = PrintMachineJob.JobStatus.UNCERTAIN;
         this.message = "Frame click cancelled without a receipt; not repeating it";
      } else if (this.status == PrintMachineJob.JobStatus.PREPARING || this.status == PrintMachineJob.JobStatus.READY) {
         this.status = PrintMachineJob.JobStatus.CONFLICT;
         this.message = "Frame cancelled before its click";
      }
   }

   @Override
   public List<String> skippedReasons() {
      ArrayList<String> out = new ArrayList<>();
      if (this.spec.invisible()) {
         out.add("frame at " + this.spec.cell().toShortString() + " is invisible in the file (needs commands)");
      }

      if (this.spec.fixed()) {
         out.add("frame at " + this.spec.cell().toShortString() + " is fixed in the file (needs commands)");
      }

      if (this.spec.undecodable()) {
         out.add("frame at " + this.spec.cell().toShortString() + " holds an item this world cannot decode");
      }

      return out;
   }

   enum FrameStage {
      HANG,
      FILL,
      TURN;
   }

   public record ObservedFrame(UUID uuid, String entityId, Direction facing, ItemStack item, int rotation) {
   }

   public record FrameSpec(
      String entityId, BlockPos cell, Direction facing, ItemStack item, int rotation, boolean invisible, boolean fixed, boolean undecodable
   ) {
      public FrameSpec(
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
