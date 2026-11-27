package autismclient.ai.engine;

import autismclient.compat263.Compat263;
import autismclient.util.BlockPlacementPlanner;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class AiBridgeDriver {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final AiBridger init_JsonUtil;
   private final AiAim init_DataUtil;
   private AiLiveWorld init_BitSetUtil;
   private int init_I;
   private int load;
   private BlockPos init_BlockPos;

   public AiBridgeDriver(AiBridger.Inner_CachePool lane, AiBridger.Inner_ConfigStore style, AiAim aim) {
      this(lane, style, aim, false);
   }

   public AiBridgeDriver(AiBridger.Inner_CachePool lane, AiBridger.Inner_ConfigStore style, AiAim aim, boolean stopAtEnd) {
      this.init_JsonUtil = new AiBridger(lane, style, stopAtEnd);
      this.init_DataUtil = aim;
   }

   public AiBridger getBridger() {
      return this.init_JsonUtil;
   }

   public AiBridger.Inner_CachePool getLane() {
      return this.init_JsonUtil.getLane();
   }

   public int getTicks() {
      return this.init_I;
   }

   public int load() {
      return this.load;
   }

   public BlockPos nextSupport() {
      if (this.init_BitSetUtil == null || this.init_BitSetUtil.getLevel() != this.init_Minecraft.level) {
         this.init_BitSetUtil = new AiLiveWorld(this.init_Minecraft.level, this.init_Minecraft.player);
      }

      this.init_BitSetUtil.clearCaches();
      int next = this.init_JsonUtil.init_I_NodeWalker_CachePo(this.init_BitSetUtil);
      AiBridger.Inner_CachePool lane = this.init_JsonUtil.getLane();
      return next > lane.length() ? null : new BlockPos(lane.cellX(next), lane.floorY(), lane.cellZ(next));
   }

   public static AiBridger.Inner_ConfigStore chooseStyle(boolean telly, AiBridger.Inner_CachePool lane, LocalPlayer player, int blocks) {
      if (telly && player.getFoodData().getFoodLevel() > 6 && blocks >= lane.length() && !AiPathfinder.MoveCosts.isCautiousParkourActiveCompat()) {
         ClientLevel level = Minecraft.getInstance().level;

         for (int i = 0; i <= lane.length(); i++) {
            BlockPos head = new BlockPos(lane.cellX(i), lane.floorY() + 3, lane.cellZ(i));
            if (level == null || !level.getBlockState(head).getCollisionShape(level, head).isEmpty()) {
               return AiBridger.Inner_ConfigStore.LEGIT;
            }
         }

         return AiBridger.Inner_ConfigStore.TELLY;
      } else {
         return AiBridger.Inner_ConfigStore.LEGIT;
      }
   }

   public static int[] init(double yaw) {
      double x = -Math.sin(Math.toRadians(yaw));
      double z = Math.cos(Math.toRadians(yaw));
      return Math.abs(x) >= Math.abs(z) ? new int[]{(int)Math.signum(x), 0} : new int[]{0, (int)Math.signum(z)};
   }

   public AiBridgeDriver.Inner_DataUtil init(int slot, IntConsumer select, Consumer<BlockPos> onPlaced) {
      LocalPlayer player = this.init_Minecraft.player;
      AiBridger.Inner_CachePool lane = this.init_JsonUtil.getLane();
      this.init_I++;
      if (this.init_I == 1) {
         BlockPos footing = new BlockPos(lane.cellX(0), lane.floorY(), lane.cellZ(0));
         BlockState state = this.init_Minecraft.level.getBlockState(footing);
         if (!Block.isShapeFullBlock(state.getCollisionShape(this.init_Minecraft.level, footing))) {
            return AiBridgeDriver.Inner_DataUtil.INVALID_FOOTING;
         }
      }

      if (player.onGround() && player.getY() < lane.floorY() + 1 - 0.01) {
         return AiBridgeDriver.Inner_DataUtil.INVALID_FOOTING;
      }

      if (player.getY() < lane.floorY() + 0.4) {
         return AiBridgeDriver.Inner_DataUtil.FELL;
      }

      if (this.init_BitSetUtil == null || this.init_BitSetUtil.getLevel() != this.init_Minecraft.level) {
         this.init_BitSetUtil = new AiLiveWorld(this.init_Minecraft.level, player);
      }

      this.init_BitSetUtil.clearCaches();
      AiBridger.Inner_DataUtil currentRot = new AiBridger.Inner_DataUtil(player.getYRot(), player.getXRot());
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      AiBridger.Inner_DataUtil wireRot = autismclient.ai.AiClientTick.init()
         ? (wire.initialized() ? new AiBridger.Inner_DataUtil(wire.currentYaw(), wire.currentPitch()) : null)
         : currentRot;
      AiBridger.Inner_StreamUtil cmd = this.init_JsonUtil
         .putAt(
            AiPhysics.MotionState.of(player),
            currentRot,
            wireRot,
            player.getEyeHeight() < 1.5F,
            this.init_BitSetUtil,
            slot >= 0
         );
      if (cmd.done()) {
         return AiBridgeDriver.Inner_DataUtil.DONE;
      }

      this.init_DataUtil.setProfile(AiAim.AimProfile.TELLY);
      this.init_DataUtil.setTargetRotation(new RotationMath.Rotation(cmd.yaw(), cmd.pitch()));
      boolean failedPlace = false;
      if (cmd.place() > 0 && slot >= 0) {
         BlockPos cell = new BlockPos(lane.cellX(cmd.place()), lane.floorY(), lane.cellZ(cmd.place()));
         if (this.init_Z_LocalPlayer(player, cell, slot, select)) {
            this.load++;
            onPlaced.accept(cell);
         } else {
            failedPlace = true;
         }
      }

      this.init_DataUtil.tick();
      this.init_Minecraft.options.keyUp.setDown(cmd.forward() > 0);
      this.init_Minecraft.options.keyDown.setDown(cmd.forward() < 0);
      this.init_Minecraft.options.keyLeft.setDown(cmd.left() > 0);
      this.init_Minecraft.options.keyRight.setDown(cmd.left() < 0);
      this.init_Minecraft.options.keyJump.setDown(cmd.jump());
      this.init_Minecraft.options.keyShift.setDown(cmd.sneak() || this.init_BlockPos != null || failedPlace && player.onGround());
      if (cmd.sprint()) {
         player.setSprinting(true);
         if (!this.init_Minecraft.options.toggleSprint().get()) {
            this.init_Minecraft.options.keySprint.setDown(true);
         }
      } else {
         player.setSprinting(false);
         this.init_Minecraft.options.keySprint.setDown(false);
      }

      if (AiNavDebug.isEnabled()) {
         AiNavDebug.log(
            "bridge tick="
               + this.init_I
               + " position="
               + player.position()
               + " motion="
               + player.getDeltaMovement()
               + " grounded="
               + player.onGround()
               + " pose="
               + player.getPose()
               + " camera="
               + player.getYRot()
               + ","
               + player.getXRot()
               + " command="
               + cmd
               + " next="
               + this.init_JsonUtil.init_I_NodeWalker_CachePo(this.init_BitSetUtil)
         );
      }

      return AiBridgeDriver.Inner_DataUtil.RUNNING;
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, BlockPos cell, int slot, IntConsumer select) {
      BlockHitResult hit = AiAim.raycastLookBlock();
      if (hit != null && hit.getBlockPos().relative(hit.getDirection()).equals(cell)) {
         BlockState face = this.init_Minecraft.level.getBlockState(hit.getBlockPos());
         if (BlockPlacementPlanner.wouldInteractWithoutSneak(face, hit.getBlockPos(), true)) {
            return false;
         }

         if (BlockPlacementPlanner.requiresSneakToPlace(face, hit.getBlockPos())) {
            this.init_BlockPos = cell;
            this.init_Minecraft.options.keyShift.setDown(true);
            if (!player.isShiftKeyDown() || !player.getLastSentInput().shift()) {
               return false;
            }
         }

         select.accept(slot);
         if (!(player.getInventory().getItem(slot).getItem() instanceof BlockItem)) {
            return false;
         }

         InteractionResult result = this.init_Minecraft.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
         if (result.consumesAction()) {
            Compat263.swing(player, InteractionHand.MAIN_HAND);
         }

         boolean stands = !this.init_Minecraft.level.getBlockState(cell).getCollisionShape(this.init_Minecraft.level, cell).isEmpty();
         if (stands) {
            this.init_BlockPos = null;
         }

         return stands;
      } else {
         return false;
      }
   }

   public enum Inner_DataUtil {
      RUNNING,
      DONE,
      FELL,
      INVALID_FOOTING;
   }
}
