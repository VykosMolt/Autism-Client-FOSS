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
   private final Minecraft mc = Minecraft.getInstance();
   private final AiBridger bridger;
   private final AiAim aim;
   private AiLiveWorld liveWorld;
   private int init_I;
   private int load;
   private BlockPos init_BlockPos;

   public AiBridgeDriver(AiBridger.Inner_CachePool lane, AiBridger.Inner_ConfigStore style, AiAim aim) {
      this(lane, style, aim, false);
   }

   public AiBridgeDriver(AiBridger.Inner_CachePool lane, AiBridger.Inner_ConfigStore style, AiAim aim, boolean stopAtEnd) {
      this.bridger = new AiBridger(lane, style, stopAtEnd);
      this.aim = aim;
   }

   public AiBridger getBridger() {
      return this.bridger;
   }

   public AiBridger.Inner_CachePool getLane() {
      return this.bridger.getLane();
   }

   public int getTicks() {
      return this.init_I;
   }

   public int load() {
      return this.load;
   }

   public BlockPos nextSupport() {
      if (this.liveWorld == null || this.liveWorld.getLevel() != this.mc.level) {
         this.liveWorld = new AiLiveWorld(this.mc.level, this.mc.player);
      }

      this.liveWorld.clearCaches();
      int next = this.bridger.init_I_NodeWalker_CachePo(this.liveWorld);
      AiBridger.Inner_CachePool lane = this.bridger.getLane();
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

   public AiBridgeDriver.BridgeStatus init(int slot, IntConsumer select, Consumer<BlockPos> onPlaced) {
      LocalPlayer player = this.mc.player;
      AiBridger.Inner_CachePool lane = this.bridger.getLane();
      this.init_I++;
      if (this.init_I == 1) {
         BlockPos footing = new BlockPos(lane.cellX(0), lane.floorY(), lane.cellZ(0));
         BlockState state = this.mc.level.getBlockState(footing);
         if (!Block.isShapeFullBlock(state.getCollisionShape(this.mc.level, footing))) {
            return AiBridgeDriver.BridgeStatus.INVALID_FOOTING;
         }
      }

      if (player.onGround() && player.getY() < lane.floorY() + 1 - 0.01) {
         return AiBridgeDriver.BridgeStatus.INVALID_FOOTING;
      }

      if (player.getY() < lane.floorY() + 0.4) {
         return AiBridgeDriver.BridgeStatus.FELL;
      }

      if (this.liveWorld == null || this.liveWorld.getLevel() != this.mc.level) {
         this.liveWorld = new AiLiveWorld(this.mc.level, player);
      }

      this.liveWorld.clearCaches();
      AiBridger.Inner_DataUtil currentRot = new AiBridger.Inner_DataUtil(player.getYRot(), player.getXRot());
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      AiBridger.Inner_DataUtil wireRot = autismclient.ai.AiClientTick.init()
         ? (wire.initialized() ? new AiBridger.Inner_DataUtil(wire.currentYaw(), wire.currentPitch()) : null)
         : currentRot;
      AiBridger.Inner_StreamUtil cmd = this.bridger
         .putAt(
            AiPhysics.MotionState.of(player),
            currentRot,
            wireRot,
            player.getEyeHeight() < 1.5F,
            this.liveWorld,
            slot >= 0
         );
      if (cmd.done()) {
         return AiBridgeDriver.BridgeStatus.DONE;
      }

      this.aim.setProfile(AiAim.AimProfile.TELLY);
      this.aim.setTargetRotation(new RotationMath.Rotation(cmd.yaw(), cmd.pitch()));
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

      this.aim.tick();
      this.mc.options.keyUp.setDown(cmd.forward() > 0);
      this.mc.options.keyDown.setDown(cmd.forward() < 0);
      this.mc.options.keyLeft.setDown(cmd.left() > 0);
      this.mc.options.keyRight.setDown(cmd.left() < 0);
      this.mc.options.keyJump.setDown(cmd.jump());
      this.mc.options.keyShift.setDown(cmd.sneak() || this.init_BlockPos != null || failedPlace && player.onGround());
      if (cmd.sprint()) {
         player.setSprinting(true);
         if (!this.mc.options.toggleSprint().get()) {
            this.mc.options.keySprint.setDown(true);
         }
      } else {
         player.setSprinting(false);
         this.mc.options.keySprint.setDown(false);
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
               + this.bridger.init_I_NodeWalker_CachePo(this.liveWorld)
         );
      }

      return AiBridgeDriver.BridgeStatus.RUNNING;
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, BlockPos cell, int slot, IntConsumer select) {
      BlockHitResult hit = AiAim.raycastLookBlock();
      if (hit != null && hit.getBlockPos().relative(hit.getDirection()).equals(cell)) {
         BlockState face = this.mc.level.getBlockState(hit.getBlockPos());
         if (BlockPlacementPlanner.wouldInteractWithoutSneak(face, hit.getBlockPos(), true)) {
            return false;
         }

         if (BlockPlacementPlanner.requiresSneakToPlace(face, hit.getBlockPos())) {
            this.init_BlockPos = cell;
            this.mc.options.keyShift.setDown(true);
            if (!player.isShiftKeyDown() || !player.getLastSentInput().shift()) {
               return false;
            }
         }

         select.accept(slot);
         if (!(player.getInventory().getItem(slot).getItem() instanceof BlockItem)) {
            return false;
         }

         InteractionResult result = this.mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
         if (result.consumesAction()) {
            Compat263.swing(player, InteractionHand.MAIN_HAND);
         }

         boolean stands = !this.mc.level.getBlockState(cell).getCollisionShape(this.mc.level, cell).isEmpty();
         if (stands) {
            this.init_BlockPos = null;
         }

         return stands;
      } else {
         return false;
      }
   }

   public enum BridgeStatus {
      RUNNING,
      DONE,
      FELL,
      INVALID_FOOTING;
   }
}
