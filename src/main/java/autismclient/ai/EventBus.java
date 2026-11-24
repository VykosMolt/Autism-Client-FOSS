package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class EventBus {
   static final double init_D = 0.4;
   static final int init_I = 24;
   static final int load = 2000;

   private EventBus() {
   }

   static boolean init(Player player) {
      if (!player.isUnderWater()) {
         return false;
      }

      if (!player.hasEffect(MobEffects.WATER_BREATHING) && !player.hasEffect(MobEffects.CONDUIT_POWER)) {
         double floor = player.getMaxAirSupply() * 0.4;
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == player && mc.level != null) {
            floor = Math.max(floor, autismclient.ai.a.AiBlockDigger.init_I_Minecraft(mc) + 40);
         }

         return player.getAirSupply() < floor;
      } else {
         return false;
      }
   }

   public static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int seconds = Math.max(5, Math.min(60, ag.init_I_JsonObject(args, "timeoutSeconds", 25)));
         if (mc.player.isUnderWater()) {
            autismclient.ai.a.BufferHelper.init_V();
         }

         return new AiOperation() {
            final long init_J = System.nanoTime() + seconds * 1000000000L;
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            BlockPos init_BlockPos;
            List<BlockPos> init_List = new ArrayList<>();
            int init_I;
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               LocalPlayer player = mc.player;
               if (!this.init_Z && player != null && mc.level != null) {
                  boolean headOut = !player.isUnderWater();
                  if (headOut && player.getAirSupply() >= player.getMaxAirSupply() * 0.9) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Breathing again (air " + player.getAirSupply() + "/" + player.getMaxAirSupply() + ")");
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.FAILED, "Could not reach air in time (air " + player.getAirSupply() + ")");
                  }

                  mc.options.keyJump.setDown(true);
                  mc.options.keyShift.setDown(false);
                  if (headOut) {
                     mc.options.keyUp.setDown(false);
                     return this.init("Treading water while the air refills (" + player.getAirSupply() + ")");
                  }

                  if (--this.init_I <= 0) {
                     this.init_List = new ArrayList<>();
                     this.init_BlockPos = EventBus.init(mc, player.blockPosition(), this.init_List);
                     this.init_I = 10;
                  }

                  BlockPos head = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
                  BlockPos roof = head.above();
                  BlockState roofState = mc.level.getBlockState(roof);
                  boolean roofSolid = !roofState.getCollisionShape(mc.level, roof).isEmpty();
                  if (roofSolid
                     && (this.init_BlockPos == null || this.init_List.size() > 5)
                     && roofState.getDestroySpeed(mc.level, roof) >= 0.0F
                     && roofState.getDestroySpeed(mc.level, roof) <= 3.0F
                     && EventBus.init_Z(mc, roof)) {
                     mc.options.keyUp.setDown(false);
                     this.init_DataUtil.init_V_Vec3(Vec3.atCenterOf(roof));
                     this.init_DataUtil.load_V();
                     int slot = af.init(roofState, 36);
                     if (slot >= 0 && player.getInventory().getSelectedSlot() != slot) {
                        af.load(slot);
                     }

                     autismclient.ai.a.AiBlockDigger.init_Z_Minecraft(mc, roof, Direction.DOWN);
                     return this.init("Breaking the roof at " + roof.toShortString() + " (air " + player.getAirSupply() + ")");
                  } else {
                     if (this.init_BlockPos == null) {
                        mc.options.keyUp.setDown(false);
                        return this.init("No air found nearby; swimming up");
                     }

                     BlockPos step = this.init_BlockPos;
                     BlockPos feetNow = player.blockPosition();

                     for (int i = 0; i < this.init_List.size(); i++) {
                        if (this.init_List.get(i).equals(feetNow) || this.init_List.get(i).equals(head)) {
                           step = i + 1 < this.init_List.size() ? this.init_List.get(i + 1) : this.init_BlockPos;
                        }
                     }

                     if (step == this.init_BlockPos && !this.init_List.isEmpty() && !this.init_List.contains(feetNow) && !this.init_List.contains(head)) {
                        step = this.init_List.get(0);
                     }

                     Vec3 target = Vec3.atCenterOf(step);
                     this.init_DataUtil.init_V_Vec3(target);
                     this.init_DataUtil.load_V();
                     double flat = Math.hypot(target.x - player.getX(), target.z - player.getZ());
                     boolean boxBlocked = !mc.level.noCollision(player, player.getBoundingBox().move(0.0, 0.6, 0.0));
                     mc.options.keyUp.setDown(flat > 0.4 || boxBlocked && flat > 0.08);
                     return this.init("Swimming to air at " + this.init_BlockPos.toShortString() + " (air " + player.getAirSupply() + ")");
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Cancelled");
               }
            }

            AiOperation.Result init(String message) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, new JsonObject());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.init();
               return R.init_AiOperation_Result(status, message, ag.init_JsonObject_ObjArr("air", mc.player == null ? 0 : mc.player.getAirSupply()));
            }

            void init() {
               mc.options.keyJump.setDown(false);
               mc.options.keyUp.setDown(false);
               this.init_DataUtil.init_V();
            }

            @Override
            public void cancel() {
               this.init_Z = true;
               this.init();
            }
         };
      } else {
         throw new IllegalArgumentException("Breathing requires a connected living player");
      }
   }

   static boolean init_Z(Minecraft mc, BlockPos roof) {
      for (int dy = 1; dy <= 4; dy++) {
         BlockPos c = roof.above(dy);
         boolean water = mc.level.getFluidState(c).is(FluidTags.WATER);
         boolean open = mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty();
         if (open && !water) {
            return true;
         }

         if (!open || !water) {
            return false;
         }
      }

      return false;
   }

   static BlockPos init_BlockPos(Minecraft mc, BlockPos feet) {
      return init(mc, feet, new ArrayList<>());
   }

   static BlockPos init(Minecraft mc, BlockPos feet, List<BlockPos> path) {
      ClientLevel level = mc.level;

      for (int dy = 1; dy <= 24; dy++) {
         BlockPos cell = feet.above(dy);
         boolean water = level.getFluidState(cell).is(FluidTags.WATER);
         boolean open = level.getBlockState(cell).getCollisionShape(level, cell).isEmpty();
         if (!open && !water) {
            break;
         }

         if (open && !water) {
            return cell;
         }
      }

      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      Map<BlockPos, BlockPos> parent = new HashMap<>();
      Set<BlockPos> seen = new HashSet<>();
      queue.add(feet);
      seen.add(feet);

      while (!queue.isEmpty() && seen.size() < 2000) {
         BlockPos at = queue.poll();

         for (Direction d : Direction.values()) {
            BlockPos next = at.relative(d);
            if (seen.add(next) && Math.abs(next.getX() - feet.getX()) <= 12 && Math.abs(next.getZ() - feet.getZ()) <= 12) {
               boolean water = level.getFluidState(next).is(FluidTags.WATER);
               boolean open = level.getBlockState(next).getCollisionShape(level, next).isEmpty();
               parent.put(next, at);
               if (open && !water && level.getFluidState(next.below()).is(FluidTags.WATER)) {
                  for (BlockPos c = next; c != null && !c.equals(feet); c = parent.get(c)) {
                     path.add(0, c);
                  }

                  return next;
               }

               if (water && (open || level.getBlockState(next).getBlock() instanceof LiquidBlock)) {
                  queue.add(next);
               }
            }
         }
      }

      return null;
   }
}
