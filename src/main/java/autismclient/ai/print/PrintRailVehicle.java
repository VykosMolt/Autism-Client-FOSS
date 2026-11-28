package autismclient.ai.print;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PrintRailVehicle implements PrintMachineJob {
   private final PrintEnv env;
   private final PrintRailVehicle.Inner_StreamUtil spec;
   private final Object worldToken;
   private final Item item;
   private final EntityType<?> entityType;
   private Set<UUID> existingIds = Set.of();
   private UUID vehicleId;
   private PrintMachineJob.JobStatus status = PrintMachineJob.JobStatus.PREPARING;
   private long dispatchNanos;
   private long lastSeenNanos = -9223372036854775808L;
   private Vec3 targetPosition;
   private AABB bounds;
   private String failure = "Waiting for observed rail and entity state";

   public PrintRailVehicle(PrintEnv env, PrintRailVehicle.Inner_StreamUtil spec) {
      this.env = env;
      this.spec = spec;
      this.worldToken = env.worldToken();
      Identifier id = Identifier.tryParse(spec.entityId());
      this.item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
      this.entityType = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
      if (!(this.item instanceof MinecartItem) || this.entityType == null) {
         throw new IllegalArgumentException("Saved rail vehicle has no native placeable minecart item: " + spec.entityId());
      }
   }

   public PrintRailVehicle.Inner_StreamUtil getSpec() {
      return this.spec;
   }

   @Override
   public BlockPos position() {
      return this.spec.rail();
   }

   @Override
   public String description() {
      return this.spec.railPlacement() ? "rail vehicle" : "off-rail minecart";
   }

   @Override
   public Item item() {
      return this.spec.railPlacement() ? this.item : Items.AIR;
   }

   public PrintMachineJob.JobStatus status() {
      return this.status;
   }

   @Override
   public String detail() {
      return this.failure;
   }

   public UUID vehicleId() {
      return this.vehicleId;
   }

   public AABB bounds() {
      return this.bounds;
   }

   public BlockPos firstUnloadedCell() {
      BlockPos rail = this.spec.rail();

      for (int x = rail.getX() - 2 >> 4; x <= rail.getX() + 2 >> 4; x++) {
         for (int z = rail.getZ() - 2 >> 4; z <= rail.getZ() + 2 >> 4; z++) {
            BlockPos at = new BlockPos(x << 4, rail.getY(), z << 4);
            if (!this.env.hasChunkAt(at)) {
               return at;
            }
         }
      }

      return null;
   }

   @Override
   public boolean matchesSource(BlockState source) {
      if (!this.spec.railPlacement()) {
         return !source.isAir() && !(source.getBlock() instanceof BaseRailBlock);
      } else {
         return !(source.getBlock() instanceof BaseRailBlock) ? false : !source.is(Blocks.DETECTOR_RAIL) || source.getValue(BlockStateProperties.POWERED);
      }
   }

   private boolean isCurrent() {
      return this.worldToken != null && this.worldToken == this.env.worldToken();
   }

   private boolean matchesSpec(PrintRailVehicle.Inner_DataUtil observed) {
      if (observed.uuid() != null && this.spec.entityId().equals(observed.entityId()) && observed.position() != null) {
         return this.spec.railPlacement()
            ? observed.position().distanceToSqr(this.targetPosition) <= 0.25
            : this.spec.matchesSavedPosition(observed.position(), observed.bounds());
      } else {
         return false;
      }
   }

   static Vec3 cartPosition(BlockPos position, BlockState state, BaseRailBlock rail) {
      double height = state.getValue(rail.getShapeProperty()).isSlope() ? 0.5 : 0.0;
      return new Vec3(position.getX() + 0.5, position.getY() + 0.0625 + height, position.getZ() + 0.5);
   }

   @Override
   public PrintMachineJob.JobStatus prepare() {
      if (this.status != PrintMachineJob.JobStatus.PREPARING && this.status != PrintMachineJob.JobStatus.READY) {
         return this.status;
      }

      if (!this.isCurrent()) {
         this.status = PrintMachineJob.JobStatus.CONFLICT;
         this.failure = "World changed before vehicle placement";
         return this.status;
      }

      if (!this.env.hasChunkAt(this.spec.rail())) {
         this.status = PrintMachineJob.JobStatus.PREPARING;
         return this.status;
      }

      BlockState rail = this.env.getBlockState(this.spec.rail());
      if (this.spec.railPlacement()) {
         if (!(rail.getBlock() instanceof BaseRailBlock block)) {
            this.status = PrintMachineJob.JobStatus.CONFLICT;
            this.failure = "The saved vehicle's native rail is absent";
            return this.status;
         }

         this.targetPosition = cartPosition(this.spec.rail(), rail, block);
      } else {
         if (!this.matchesSource(rail)) {
            this.status = PrintMachineJob.JobStatus.CONFLICT;
            this.failure = "The off-rail minecart's saved block is absent";
            return this.status;
         }

         this.targetPosition = this.spec.position();
      }

      this.bounds = this.entityType.getDimensions().makeBoundingBox(this.targetPosition);
      List<PrintRailVehicle.Inner_DataUtil> observations = this.env.nearbyMinecarts(this.spec.rail());
      if (observations == null) {
         this.status = PrintMachineJob.JobStatus.PREPARING;
         return this.status;
      }

      HashSet<UUID> ids = new HashSet<>();
      PrintRailVehicle.Inner_DataUtil existing = null;

      for (PrintRailVehicle.Inner_DataUtil observed : observations) {
         if (observed.uuid() == null || observed.position() == null || observed.bounds() == null) {
            this.status = PrintMachineJob.JobStatus.PREPARING;
            return this.status;
         }

         ids.add(observed.uuid());
         if (this.matchesSpec(observed)) {
            if (existing != null) {
               this.status = PrintMachineJob.JobStatus.CONFLICT;
               this.failure = "Multiple matching vehicles already occupy the saved position";
               return this.status;
            }

            existing = observed;
         } else if (observed.bounds().intersects(this.bounds)) {
            this.status = PrintMachineJob.JobStatus.CONFLICT;
            this.failure = "Another vehicle occupies the placement space";
            return this.status;
         }
      }

      this.existingIds = Set.copyOf(ids);
      if (existing != null) {
         this.vehicleId = existing.uuid();
         this.status = PrintMachineJob.JobStatus.SUCCEEDED;
         this.failure = "Existing native vehicle verified";
         return this.status;
      } else if (!this.spec.railPlacement()) {
         this.failure = "Saved off-rail minecart not placed at " + this.spec.rail().toShortString() + "; native assembly required";
         return this.status = PrintMachineJob.JobStatus.CONFLICT;
      } else {
         this.status = PrintMachineJob.JobStatus.READY;
         this.failure = "Native vehicle placement is ready";
         return this.status;
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.spec.railPlacement() && this.prepare() == PrintMachineJob.JobStatus.READY) {
         if (this.bounds.intersects(this.env.serverPlayerBox())
            || this.bounds.intersects(this.env.playerMotionState().box(this.env.isServerSneaking()))) {
            this.failure = "Worker must leave the vehicle's native spawn space";
            return false;
         } else if (!this.env.getInventoryItem(this.env.getSelectedHotbarSlot()).is(this.item)) {
            this.failure = "The native vehicle item is not held";
            return false;
         } else {
            this.dispatchNanos = this.env.nanoTime();
            this.status = PrintMachineJob.JobStatus.SENT;
            this.failure = "Waiting for a fresh native vehicle identity";
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public PrintMachineJob.JobStatus pollStatus() {
      if (this.status != PrintMachineJob.JobStatus.SENT) {
         return this.status;
      }

      if (!this.isCurrent()) {
         this.status = PrintMachineJob.JobStatus.UNCERTAIN;
         this.failure = "World changed after the vehicle attempt; do not repeat it";
         return this.status;
      }

      List<PrintRailVehicle.Inner_DataUtil> observations = this.env.nearbyMinecarts(this.spec.rail());
      PrintRailVehicle.Inner_DataUtil created = null;
      if (observations != null) {
         for (PrintRailVehicle.Inner_DataUtil observed : observations) {
            if (this.matchesSpec(observed) && !this.existingIds.contains(observed.uuid())) {
               if (created != null) {
                  this.status = PrintMachineJob.JobStatus.UNCERTAIN;
                  this.failure = "Ambiguous native vehicle receipt; do not repeat the attempt";
                  return this.status;
               }

               created = observed;
            }
         }
      }

      if (created != null) {
         this.vehicleId = created.uuid();
         this.status = PrintMachineJob.JobStatus.SUCCEEDED;
         this.failure = "Fresh native vehicle identity verified";
         return this.status;
      }

      if (this.env.nanoTime() - this.dispatchNanos >= 5000000000L) {
         this.status = PrintMachineJob.JobStatus.UNCERTAIN;
         this.failure = "No fresh vehicle receipt; do not repeat the attempt";
      }

      return this.status;
   }

   @Override
   public PrintMachineJob.JobStatus verifyStatus() {
      if (this.status != PrintMachineJob.JobStatus.SUCCEEDED) {
         return this.status;
      }

      if (!this.isCurrent()) {
         this.failure = "World changed before final vehicle verification";
         return this.status = PrintMachineJob.JobStatus.UNCERTAIN;
      }

      if (this.firstUnloadedCell() != null) {
         this.lastSeenNanos = -9223372036854775808L;
         return PrintMachineJob.JobStatus.PREPARING;
      }

      List<PrintRailVehicle.Inner_DataUtil> observations = this.env.nearbyMinecarts(this.spec.rail());
      if (observations != null) {
         for (PrintRailVehicle.Inner_DataUtil observed : observations) {
            if (this.vehicleId.equals(observed.uuid())) {
               if (!this.matchesSpec(observed)) {
                  this.failure = "The verified vehicle left its saved placement at " + this.spec.rail().toShortString();
                  return this.status = PrintMachineJob.JobStatus.CONFLICT;
               }

               this.lastSeenNanos = -9223372036854775808L;
               this.failure = "Native vehicle identity reverified after access recovery";
               return PrintMachineJob.JobStatus.SUCCEEDED;
            }
         }
      }

      if (this.lastSeenNanos == -9223372036854775808L) {
         this.lastSeenNanos = this.env.nanoTime();
      }

      if (this.env.nanoTime() - this.lastSeenNanos >= 5000000000L) {
         this.failure = "The verified vehicle is unavailable at " + this.spec.rail().toShortString() + "; do not repeat placement";
         return this.status = PrintMachineJob.JobStatus.UNCERTAIN;
      } else {
         this.failure = "Waiting for the verified vehicle's loaded identity at " + this.spec.rail().toShortString();
         return PrintMachineJob.JobStatus.PREPARING;
      }
   }

   @Override
   public void cancel() {
      if (this.status == PrintMachineJob.JobStatus.SENT) {
         this.status = PrintMachineJob.JobStatus.UNCERTAIN;
         this.failure = "Vehicle attempt cancelled without a receipt; do not repeat it";
      } else if (this.status == PrintMachineJob.JobStatus.PREPARING || this.status == PrintMachineJob.JobStatus.READY) {
         this.status = PrintMachineJob.JobStatus.CONFLICT;
         this.failure = "Vehicle placement cancelled before dispatch";
      }
   }

   public record Inner_DataUtil(UUID uuid, String entityId, Vec3 position, AABB bounds) {
   }

   public record Inner_StreamUtil(String entityId, Vec3 position, boolean railPlacement) {
      public Inner_StreamUtil(String entityId, Vec3 position) {
         this(entityId, position, true);
      }

      public Inner_StreamUtil(String entityId, Vec3 position, boolean railPlacement) {
         if (entityId != null
            && position != null
            && Double.isFinite(position.x)
            && Double.isFinite(position.y)
            && Double.isFinite(position.z)
            && !(Math.abs(position.x) > 3.0E7)
            && !(Math.abs(position.y) > 3.0E7)
            && !(Math.abs(position.z) > 3.0E7)) {
            this.entityId = entityId;
            this.position = position;
            this.railPlacement = railPlacement;
         } else {
            throw new IllegalArgumentException("Invalid saved rail vehicle");
         }
      }

      public BlockPos rail() {
         return BlockPos.containing(this.position);
      }

      public boolean matchesSavedPosition(Vec3 actual, AABB bounds) {
         if (actual != null && bounds != null) {
            return this.railPlacement
               ? actual.distanceToSqr(this.position) <= 0.25
               : BlockPos.containing(actual).equals(this.rail()) && bounds.intersects(new AABB(this.rail())) && actual.distanceToSqr(this.position) <= 0.0625;
         } else {
            return false;
         }
      }
   }
}
