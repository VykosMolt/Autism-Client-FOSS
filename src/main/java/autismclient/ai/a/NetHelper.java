package autismclient.ai.a;

import autismclient.compat263.Compat263;
import autismclient.util.U;
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

public final class NetHelper {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final JsonUtil init_JsonUtil;
   private final DataUtil init_DataUtil;
   private BitSetUtil init_BitSetUtil;
   private int init_I;
   private int load;
   private BlockPos init_BlockPos;

   public NetHelper(JsonUtil.Inner_CachePool lane, JsonUtil.Inner_ConfigStore style, DataUtil aim) {
      this(lane, style, aim, false);
   }

   public NetHelper(JsonUtil.Inner_CachePool lane, JsonUtil.Inner_ConfigStore style, DataUtil aim, boolean stopAtEnd) {
      this.init_JsonUtil = new JsonUtil(lane, style, stopAtEnd);
      this.init_DataUtil = aim;
   }

   public JsonUtil init_JsonUtil() {
      return this.init_JsonUtil;
   }

   public JsonUtil.Inner_CachePool init_JsonUtil_CachePool() {
      return this.init_JsonUtil.init_JsonUtil_CachePool();
   }

   public int init_I() {
      return this.init_I;
   }

   public int load() {
      return this.load;
   }

   public BlockPos init_BlockPos() {
      if (this.init_BitSetUtil == null || this.init_BitSetUtil.init_ClientLevel() != this.init_Minecraft.level) {
         this.init_BitSetUtil = new BitSetUtil(this.init_Minecraft.level, this.init_Minecraft.player);
      }

      this.init_BitSetUtil.init_V();
      int next = this.init_JsonUtil.init_I_NodeWalker_CachePo(this.init_BitSetUtil);
      JsonUtil.Inner_CachePool lane = this.init_JsonUtil.init_JsonUtil_CachePool();
      return next > lane.length() ? null : new BlockPos(lane.cellX(next), lane.floorY(), lane.cellZ(next));
   }

   public static JsonUtil.Inner_ConfigStore init_JsonUtil_ConfigSto_Z(boolean telly, JsonUtil.Inner_CachePool lane, LocalPlayer player, int blocks) {
      if (telly && player.getFoodData().getFoodLevel() > 6 && blocks >= lane.length() && !ClassCache.Inner_CachePool.init()) {
         ClientLevel level = Minecraft.getInstance().level;

         for (int i = 0; i <= lane.length(); i++) {
            BlockPos head = new BlockPos(lane.cellX(i), lane.floorY() + 3, lane.cellZ(i));
            if (level == null || !level.getBlockState(head).getCollisionShape(level, head).isEmpty()) {
               return JsonUtil.Inner_ConfigStore.load;
            }
         }

         return JsonUtil.Inner_ConfigStore.init;
      } else {
         return JsonUtil.Inner_ConfigStore.load;
      }
   }

   public static int[] init(double yaw) {
      double x = -Math.sin(Math.toRadians(yaw));
      double z = Math.cos(Math.toRadians(yaw));
      return Math.abs(x) >= Math.abs(z) ? new int[]{(int)Math.signum(x), 0} : new int[]{0, (int)Math.signum(z)};
   }

   public NetHelper.Inner_DataUtil init(int slot, IntConsumer select, Consumer<BlockPos> onPlaced) {
      LocalPlayer player = this.init_Minecraft.player;
      JsonUtil.Inner_CachePool lane = this.init_JsonUtil.init_JsonUtil_CachePool();
      this.init_I++;
      if (this.init_I == 1) {
         BlockPos footing = new BlockPos(lane.cellX(0), lane.floorY(), lane.cellZ(0));
         BlockState state = this.init_Minecraft.level.getBlockState(footing);
         if (!Block.isShapeFullBlock(state.getCollisionShape(this.init_Minecraft.level, footing))) {
            return NetHelper.Inner_DataUtil.write;
         }
      }

      if (player.onGround() && player.getY() < lane.floorY() + 1 - 0.01) {
         return NetHelper.Inner_DataUtil.write;
      }

      if (player.getY() < lane.floorY() + 0.4) {
         return NetHelper.Inner_DataUtil.read;
      }

      if (this.init_BitSetUtil == null || this.init_BitSetUtil.init_ClientLevel() != this.init_Minecraft.level) {
         this.init_BitSetUtil = new BitSetUtil(this.init_Minecraft.level, player);
      }

      this.init_BitSetUtil.init_V();
      JsonUtil.Inner_DataUtil currentRot = new JsonUtil.Inner_DataUtil(player.getYRot(), player.getXRot());
      ServerRotationTracker.Inner_CachePool wire = ServerRotationTracker.init_ch_CachePool();
      JsonUtil.Inner_DataUtil wireRot = autismclient.ai.ClassCache.init()
         ? (wire.init_Z() ? new JsonUtil.Inner_DataUtil(wire.read(), wire.write()) : null)
         : currentRot;
      JsonUtil.Inner_StreamUtil cmd = this.init_JsonUtil
         .putAt(
            NodeWalker.Inner_StreamUtil.of(player),
            currentRot,
            wireRot,
            player.getEyeHeight() < 1.5F,
            this.init_BitSetUtil,
            slot >= 0
         );
      if (cmd.done()) {
         return NetHelper.Inner_DataUtil.load;
      }

      this.init_DataUtil.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.flush);
      this.init_DataUtil.init_V_ce_DataUtil(new RotationMath.Inner_DataUtil(cmd.yaw(), cmd.pitch()));
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

      this.init_DataUtil.load_V();
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

      if (ListPool.init_Z()) {
         ListPool.init(
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

      return NetHelper.Inner_DataUtil.init;
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, BlockPos cell, int slot, IntConsumer select) {
      BlockHitResult hit = DataUtil.init_BlockHitResult();
      if (hit != null && hit.getBlockPos().relative(hit.getDirection()).equals(cell)) {
         BlockState face = this.init_Minecraft.level.getBlockState(hit.getBlockPos());
         if (U.init(face, hit.getBlockPos(), true)) {
            return false;
         }

         if (U.init_Z(face, hit.getBlockPos())) {
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
      init,
      load,
      read,
      write;
   }
}
