package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiFarmPlacement extends AiPlacementSession {
   private final ClientLevel level;
   private final String init_Str;
   private final String load_Str;
   private final BlockPos init_BlockPos;
   private final BlockPos load_BlockPos;
   private final boolean init_Z;
   private BlockPos read_BlockPos;
   private BlockPos write;
   private boolean load_Z;
   private boolean read_Z;
   private double init_D = 6.0;
   private Vec3 init_Vec3;

   AiFarmPlacement(String cropBlock, String substrate) {
      this(cropBlock, substrate, null);
   }

   AiFarmPlacement(String cropBlock, String substrate, AiFarmArea.Area previous) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         this.level = mc.level;
         this.init_Str = cropBlock;
         this.load_Str = AiFarmArea.substrateFor(AiJson.objectOf("substrate", substrate), cropBlock);
         this.read_BlockPos = previous == null ? null : previous.from();
         this.write = previous == null ? mc.player.blockPosition().below() : previous.to();
         this.load_Z = previous != null;
         this.init_BlockPos = this.read_BlockPos;
         this.load_BlockPos = this.write;
         this.init_Z = this.load_Z;
         this.write_V();
      } else {
         throw new IllegalArgumentException("Join a world first");
      }
   }

   String getCrop() {
      return this.init_Str;
   }

   String getSubstrate() {
      return this.load_Str;
   }

   boolean isComplete() {
      return this.load_Z;
   }

   AiFarmArea.Area getArea() {
      return this.load_Z ? new AiFarmArea.Area(this.read_BlockPos, this.write) : null;
   }

   @Override
   public boolean isReadyToConfirm() {
      AiFarmArea.Area area = this.getArea();
      return area != null && area.validate(this.load_Str).isEmpty() && this.isActive();
   }

   @Override
   public boolean isActive() {
      return !this.read_Z && Minecraft.getInstance().level == this.level;
   }

   @Override
   public BlockPos getOrigin() {
      return this.write;
   }

   @Override
   public int getRotation() {
      return 0;
   }

   @Override
   public String getMirror() {
      return "none";
   }

   @Override
   public void rotate(int steps) {
   }

   @Override
   public void cycleMirror() {
   }

   boolean confirmCorner() {
      if (!this.isActive()) {
         return false;
      } else if (!this.load_Z) {
         this.read_BlockPos = this.write.immutable();
         this.load_Z = true;
         this.write_V();
         return false;
      } else {
         return this.isReadyToConfirm();
      }
   }

   String getStatus() {
      if (!this.load_Z) {
         return "Choose the first corner";
      }

      AiFarmArea.Area area = this.getArea();
      String issue = area.validate(this.load_Str);
      return !issue.isEmpty()
         ? issue
         : area.width()
            + "x"
            + area.length()
            + " / "
            + area.layout(this.load_Str).plots()
            + " plots / "
            + (AiFarmArea.isSoulSand(this.load_Str) ? "soul sand" : "dirt and water");
   }

   String update() {
      return this.load_Z ? "Aim at the opposite corner. E: confirm | Esc: cancel" : "Aim at the first corner. E: set | Esc: cancel";
   }

   JsonObject areaJson() {
      AiFarmArea.Area area = this.getArea();
      if (area == null) {
         throw new IllegalArgumentException("Choose both farm corners first");
      } else {
         String issue = area.validate(this.load_Str);
         if (!issue.isEmpty()) {
            throw new IllegalArgumentException(issue);
         } else {
            return area.arguments(this.load_Str);
         }
      }
   }

   JsonObject statusJson() {
      AiFarmArea.Area area = this.getArea();
      return AiJson.objectOf(
         "crop",
         this.init_Str,
         "substrate",
         this.load_Str,
         "complete",
         this.isComplete(),
         "ready",
         this.isReadyToConfirm(),
         "first",
         this.read_BlockPos == null ? new JsonObject() : posJson(this.read_BlockPos),
         "second",
         posJson(this.write),
         "status",
         this.getStatus(),
         "width",
         area == null ? 0 : area.width(),
         "length",
         area == null ? 0 : area.length()
      );
   }

   @Override
   public void moveToAim(boolean surface) {
      if (this.isActive()) {
         Minecraft mc = Minecraft.getInstance();
         Camera camera = mc.gameRenderer.mainCamera();
         Vec3 eye = camera.position();
         Vec3 direction = Vec3.directionFromRotation(mc.player.getXRot(), mc.player.getYRot()).normalize();
         this.init_Vec3 = direction;
         if (surface) {
            double reach = (mc.options.getEffectiveRenderDistance() + 2) * 16.0;
            BlockHitResult hit = this.level.clip(new ClipContext(eye, eye.add(direction.scale(reach)), Block.OUTLINE, Fluid.NONE, mc.player));
            if (hit.getType() == Type.BLOCK && AiTerrain.isLoaded(this.level, hit.getBlockPos())) {
               this.write = this.init_BlockPos_BlockPos(hit.getBlockPos());
               this.init_D = Vec3.atCenterOf(this.write).distanceTo(eye);
            } else {
               this.init_D = AiPlacementControls.init(eye, direction, this.write.getY() + 1, this.init_D);
               this.write = this.clampToWorldBorder(AiPlacementControls.init(eye, direction, this.init_D));
            }
         } else {
            this.write = this.clampToWorldBorder(AiPlacementControls.init(eye, direction, this.init_D));
         }

         this.write_V();
      }
   }

   private BlockPos init_BlockPos_BlockPos(BlockPos hit) {
      BlockState state = this.level.getBlockState(hit);
      return state.getCollisionShape(this.level, hit).isEmpty()
            && state.getFluidState().isEmpty()
            && !this.level.getBlockState(hit.below()).getCollisionShape(this.level, hit.below()).isEmpty()
         ? hit.below()
         : hit;
   }

   private BlockPos clampToWorldBorder(Vec3 point) {
      WorldBorder border = this.level.getWorldBorder();
      return BlockPos.containing(
         Mth.clamp(point.x, border.getMinX(), border.getMaxX() - 1.0),
         Mth.clamp(point.y, this.level.getMinY(), this.level.getMaxY() - 1),
         Mth.clamp(point.z, border.getMinZ(), border.getMaxZ() - 1.0)
      );
   }

   @Override
   public void init(double scroll, double screenX, double screenY, double width, double height) {
      if (this.isActive()) {
         Vec3 eye = Minecraft.getInstance().gameRenderer.mainCamera().position();
         if (this.init_Vec3 == null) {
            Vec3 delta = Vec3.atCenterOf(this.write).subtract(eye);
            this.init_Vec3 = delta.lengthSqr() < 0.01 ? Minecraft.getInstance().player.getLookAngle() : delta.normalize();
         }

         this.init_D = AiPlacementControls.adjustDistance(this.init_D, scroll);
         this.write = this.clampToWorldBorder(AiPlacementControls.init(eye, this.init_Vec3, this.init_D));
         this.write_V();
      }
   }

   @Override
   public void snapToGround() {
      if (this.isActive()) {
         this.write = new BlockPos(
            this.write.getX(), this.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, this.write.getX(), this.write.getZ()) - 1, this.write.getZ()
         );
         this.write_V();
      }
   }

   @Override
   public void init(int x, int y, int z) {
      if (this.isActive()) {
         this.write = this.write.offset(x, y, z);
         this.write_V();
      }
   }

   @Override
   public void init(BlockPos position, int rotation, String mirror) {
      if (this.isActive()) {
         this.read_BlockPos = this.init_BlockPos;
         this.write = this.load_BlockPos;
         this.load_Z = this.init_Z;
         this.write_V();
      }
   }

   private void write_V() {
      List<AiBuilder.CellSpec> cells;
      BlockPos origin;
      if (!this.load_Z) {
         origin = this.write;
         cells = List.of(new AiBuilder.CellSpec(0, 0, 0, AiFarmArea.read(this.load_Str)));
      } else {
         AiFarmArea.Area area = this.getArea();
         origin = area.origin();
         cells = new ArrayList<>();
         if (!area.validate(this.load_Str).isEmpty()) {
            cells.add(new AiBuilder.CellSpec(0, 0, 0, AiFarmArea.read(this.load_Str)));
            AiVisualization.load(this);
            AiVisualization.init(this, origin, cells, 0, true);
            return;
         }

         AiFarmBuild.FarmLayout layout = area.layout(this.load_Str);

         for (int x = 0; x < area.width(); x++) {
            for (int z = 0; z < area.length(); z++) {
               boolean water = layout.water(x, z);
               cells.add(new AiBuilder.CellSpec(x, 0, z, water ? "minecraft:water" : AiFarmArea.read(this.load_Str)));
            }
         }
      }

      AiVisualization.load(this);
      AiVisualization.init(this, origin, cells, 0, true);
   }

   @Override
   public void cancel() {
      if (!this.read_Z) {
         this.read_Z = true;
         AiVisualization.clearOwner(this);
      }
   }

   private static JsonObject posJson(BlockPos p) {
      return AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ());
   }
}
