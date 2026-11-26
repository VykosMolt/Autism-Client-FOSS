package autismclient.ai.b;

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
   private final PrintEnv init_EventBus;
   private final PrintRailVehicle.Inner_StreamUtil init_MethodUtil_StreamU;
   private final Object init_Obj;
   private final Item init_Item;
   private final EntityType<?> init_EntityType;
   private Set<UUID> init_Set = Set.of();
   private UUID init_UUID;
   private PrintMachineJob.Inner_DataUtil init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.init;
   private long init_J;
   private long load = -9223372036854775808L;
   private Vec3 init_Vec3;
   private AABB init_AABB;
   private String init_Str = "Waiting for observed rail and entity state";

   public PrintRailVehicle(PrintEnv env, PrintRailVehicle.Inner_StreamUtil spec) {
      this.init_EventBus = env;
      this.init_MethodUtil_StreamU = spec;
      this.init_Obj = env.init_Obj();
      Identifier id = Identifier.tryParse(spec.entityId());
      this.init_Item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
      this.init_EntityType = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
      if (!(this.init_Item instanceof MinecartItem) || this.init_EntityType == null) {
         throw new IllegalArgumentException("Saved rail vehicle has no native placeable minecart item: " + spec.entityId());
      }
   }

   public PrintRailVehicle.Inner_StreamUtil init_MethodUtil_StreamU() {
      return this.init_MethodUtil_StreamU;
   }

   @Override
   public BlockPos position() {
      return this.init_MethodUtil_StreamU.rail();
   }

   @Override
   public String description() {
      return this.init_MethodUtil_StreamU.railPlacement() ? "rail vehicle" : "off-rail minecart";
   }

   @Override
   public Item item() {
      return this.init_MethodUtil_StreamU.railPlacement() ? this.init_Item : Items.AIR;
   }

   public PrintMachineJob.Inner_DataUtil load_BitSetUtil_DataUti() {
      return this.init_BitSetUtil_DataUti;
   }

   @Override
   public String detail() {
      return this.init_Str;
   }

   public UUID init_UUID() {
      return this.init_UUID;
   }

   public AABB init_AABB() {
      return this.init_AABB;
   }

   public BlockPos init_BlockPos() {
      BlockPos rail = this.init_MethodUtil_StreamU.rail();

      for (int x = rail.getX() - 2 >> 4; x <= rail.getX() + 2 >> 4; x++) {
         for (int z = rail.getZ() - 2 >> 4; z <= rail.getZ() + 2 >> 4; z++) {
            BlockPos at = new BlockPos(x << 4, rail.getY(), z << 4);
            if (!this.init_EventBus.load_Z(at)) {
               return at;
            }
         }
      }

      return null;
   }

   @Override
   public boolean matchesSource(BlockState source) {
      if (!this.init_MethodUtil_StreamU.railPlacement()) {
         return !source.isAir() && !(source.getBlock() instanceof BaseRailBlock);
      } else {
         return !(source.getBlock() instanceof BaseRailBlock) ? false : !source.is(Blocks.DETECTOR_RAIL) || source.getValue(BlockStateProperties.POWERED);
      }
   }

   private boolean open() {
      return this.init_Obj != null && this.init_Obj == this.init_EventBus.init_Obj();
   }

   private boolean init(PrintRailVehicle.Inner_DataUtil observed) {
      if (observed.uuid() != null && this.init_MethodUtil_StreamU.entityId().equals(observed.entityId()) && observed.position() != null) {
         return this.init_MethodUtil_StreamU.railPlacement()
            ? observed.position().distanceToSqr(this.init_Vec3) <= 0.25
            : this.init_MethodUtil_StreamU.matchesSavedPosition(observed.position(), observed.bounds());
      } else {
         return false;
      }
   }

   static Vec3 init(BlockPos position, BlockState state, BaseRailBlock rail) {
      double height = state.getValue(rail.getShapeProperty()).isSlope() ? 0.5 : 0.0;
      return new Vec3(position.getX() + 0.5, position.getY() + 0.0625 + height, position.getZ() + 0.5);
   }

   @Override
   public PrintMachineJob.Inner_DataUtil prepare() {
      if (this.init_BitSetUtil_DataUti != PrintMachineJob.Inner_DataUtil.init && this.init_BitSetUtil_DataUti != PrintMachineJob.Inner_DataUtil.load) {
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.open()) {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
         this.init_Str = "World changed before vehicle placement";
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.init_EventBus.load_Z(this.init_MethodUtil_StreamU.rail())) {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.init;
         return this.init_BitSetUtil_DataUti;
      }

      BlockState rail = this.init_EventBus.init_BlockState_BlockPos(this.init_MethodUtil_StreamU.rail());
      if (this.init_MethodUtil_StreamU.railPlacement()) {
         if (!(rail.getBlock() instanceof BaseRailBlock block)) {
            this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
            this.init_Str = "The saved vehicle's native rail is absent";
            return this.init_BitSetUtil_DataUti;
         }

         this.init_Vec3 = init(this.init_MethodUtil_StreamU.rail(), rail, block);
      } else {
         if (!this.matchesSource(rail)) {
            this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
            this.init_Str = "The off-rail minecart's saved block is absent";
            return this.init_BitSetUtil_DataUti;
         }

         this.init_Vec3 = this.init_MethodUtil_StreamU.position();
      }

      this.init_AABB = this.init_EntityType.getDimensions().makeBoundingBox(this.init_Vec3);
      List<PrintRailVehicle.Inner_DataUtil> observations = this.init_EventBus.load_List(this.init_MethodUtil_StreamU.rail());
      if (observations == null) {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.init;
         return this.init_BitSetUtil_DataUti;
      }

      HashSet<UUID> ids = new HashSet<>();
      PrintRailVehicle.Inner_DataUtil existing = null;

      for (PrintRailVehicle.Inner_DataUtil observed : observations) {
         if (observed.uuid() == null || observed.position() == null || observed.bounds() == null) {
            this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.init;
            return this.init_BitSetUtil_DataUti;
         }

         ids.add(observed.uuid());
         if (this.init(observed)) {
            if (existing != null) {
               this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
               this.init_Str = "Multiple matching vehicles already occupy the saved position";
               return this.init_BitSetUtil_DataUti;
            }

            existing = observed;
         } else if (observed.bounds().intersects(this.init_AABB)) {
            this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
            this.init_Str = "Another vehicle occupies the placement space";
            return this.init_BitSetUtil_DataUti;
         }
      }

      this.init_Set = Set.copyOf(ids);
      if (existing != null) {
         this.init_UUID = existing.uuid();
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.update;
         this.init_Str = "Existing native vehicle verified";
         return this.init_BitSetUtil_DataUti;
      } else if (!this.init_MethodUtil_StreamU.railPlacement()) {
         this.init_Str = "Saved off-rail minecart not placed at " + this.init_MethodUtil_StreamU.rail().toShortString() + "; native assembly required";
         return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
      } else {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.load;
         this.init_Str = "Native vehicle placement is ready";
         return this.init_BitSetUtil_DataUti;
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.init_MethodUtil_StreamU.railPlacement() && this.prepare() == PrintMachineJob.Inner_DataUtil.load) {
         if (this.init_AABB.intersects(this.init_EventBus.init_AABB())
            || this.init_AABB.intersects(this.init_EventBus.init_NodeWalker_StreamU().box(this.init_EventBus.load_Z()))) {
            this.init_Str = "Worker must leave the vehicle's native spawn space";
            return false;
         } else if (!this.init_EventBus.init_ItemStack_I(this.init_EventBus.read_I()).is(this.init_Item)) {
            this.init_Str = "The native vehicle item is not held";
            return false;
         } else {
            this.init_J = this.init_EventBus.load_J();
            this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.read;
            this.init_Str = "Waiting for a fresh native vehicle identity";
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public PrintMachineJob.Inner_DataUtil poll_BitSetUtil_DataUti() {
      if (this.init_BitSetUtil_DataUti != PrintMachineJob.Inner_DataUtil.read) {
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.open()) {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.close;
         this.init_Str = "World changed after the vehicle attempt; do not repeat it";
         return this.init_BitSetUtil_DataUti;
      }

      List<PrintRailVehicle.Inner_DataUtil> observations = this.init_EventBus.load_List(this.init_MethodUtil_StreamU.rail());
      PrintRailVehicle.Inner_DataUtil created = null;
      if (observations != null) {
         for (PrintRailVehicle.Inner_DataUtil observed : observations) {
            if (this.init(observed) && !this.init_Set.contains(observed.uuid())) {
               if (created != null) {
                  this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.close;
                  this.init_Str = "Ambiguous native vehicle receipt; do not repeat the attempt";
                  return this.init_BitSetUtil_DataUti;
               }

               created = observed;
            }
         }
      }

      if (created != null) {
         this.init_UUID = created.uuid();
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.update;
         this.init_Str = "Fresh native vehicle identity verified";
         return this.init_BitSetUtil_DataUti;
      }

      if (this.init_EventBus.load_J() - this.init_J >= 5000000000L) {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.close;
         this.init_Str = "No fresh vehicle receipt; do not repeat the attempt";
      }

      return this.init_BitSetUtil_DataUti;
   }

   @Override
   public PrintMachineJob.Inner_DataUtil init_BitSetUtil_DataUti() {
      if (this.init_BitSetUtil_DataUti != PrintMachineJob.Inner_DataUtil.update) {
         return this.init_BitSetUtil_DataUti;
      }

      if (!this.open()) {
         this.init_Str = "World changed before final vehicle verification";
         return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.close;
      }

      if (this.init_BlockPos() != null) {
         this.load = -9223372036854775808L;
         return PrintMachineJob.Inner_DataUtil.init;
      }

      List<PrintRailVehicle.Inner_DataUtil> observations = this.init_EventBus.load_List(this.init_MethodUtil_StreamU.rail());
      if (observations != null) {
         for (PrintRailVehicle.Inner_DataUtil observed : observations) {
            if (this.init_UUID.equals(observed.uuid())) {
               if (!this.init(observed)) {
                  this.init_Str = "The verified vehicle left its saved placement at " + this.init_MethodUtil_StreamU.rail().toShortString();
                  return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
               }

               this.load = -9223372036854775808L;
               this.init_Str = "Native vehicle identity reverified after access recovery";
               return PrintMachineJob.Inner_DataUtil.update;
            }
         }
      }

      if (this.load == -9223372036854775808L) {
         this.load = this.init_EventBus.load_J();
      }

      if (this.init_EventBus.load_J() - this.load >= 5000000000L) {
         this.init_Str = "The verified vehicle is unavailable at " + this.init_MethodUtil_StreamU.rail().toShortString() + "; do not repeat placement";
         return this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.close;
      } else {
         this.init_Str = "Waiting for the verified vehicle's loaded identity at " + this.init_MethodUtil_StreamU.rail().toShortString();
         return PrintMachineJob.Inner_DataUtil.init;
      }
   }

   @Override
   public void cancel() {
      if (this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.read) {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.close;
         this.init_Str = "Vehicle attempt cancelled without a receipt; do not repeat it";
      } else if (this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.init || this.init_BitSetUtil_DataUti == PrintMachineJob.Inner_DataUtil.load) {
         this.init_BitSetUtil_DataUti = PrintMachineJob.Inner_DataUtil.flush;
         this.init_Str = "Vehicle placement cancelled before dispatch";
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
