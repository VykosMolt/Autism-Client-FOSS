package autismclient.ai.a;

import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.cx;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

final class ArrayUtil {
   private final Minecraft init_Minecraft;
   private final autismclient.gui.d.PacketUtil init_PacketUtil;

   ArrayUtil(Minecraft mc, ArrayUtil.Inner_DataUtil loss, Consumer<Boolean> approve, Runnable cancel) {
      this.init_Minecraft = mc;
      List<autismclient.gui.d.PacketUtil.Inner_DataUtil> actions = new ArrayList<>();
      actions.add(new autismclient.gui.d.PacketUtil.Inner_DataUtil("Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::init_V));
      actions.add(
         new autismclient.gui.d.PacketUtil.Inner_DataUtil(
            "Go", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, () -> this.init_V_Consumer_Z(approve, false)
         )
      );
      Module noFall = al.init_Module_Str("no-fall");
      if (noFall != null && !noFall.isEnabled()) {
         actions.add(
            new autismclient.gui.d.PacketUtil.Inner_DataUtil(
               "NoFall & Go", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> this.init_V_Consumer_Z(approve, true)
            )
         );
      }

      String risk = String.format(Locale.ROOT, "About %.1f hearts of damage Â· %.1f hearts available", loss.damage() / 2.0F, loss.health() / 2.0F);
      this.init_PacketUtil = new autismclient.gui.d.PacketUtil(
         "Parkour fall warning",
         List.of(
            loss.lethal() ? "This route could kill you in vanilla." : "This route can cause fall damage.",
            risk,
            "Go assumes the server has fall damage disabled."
         ),
         actions,
         cancel
      );
   }

   private void init_V_Consumer_Z(Consumer<Boolean> approve, boolean enable) {
      this.init_PacketUtil.init_V_Runnable(() -> approve.accept(enable));
   }

   boolean init_Z() {
      return cx.init_Z(this.init_Minecraft, this.init_PacketUtil);
   }

   void init_V() {
      this.init_PacketUtil.onClose();
   }

   static ArrayUtil.Inner_DataUtil init_ArrayUtil_DataUtil_Minecraft_List(Minecraft mc, List<ClassCache.UiHelper> route) {
      LocalPlayer player = mc.player;
      double health = player.getHealth() + player.getAbsorptionAmount();
      if (!player.getAbilities().invulnerable && !player.hasEffect(MobEffects.SLOW_FALLING)) {
         AutismLivingEntityAccessor access = (AutismLivingEntityAccessor)player;
         ClientLevel level = mc.level;
         BitSetUtil world = new BitSetUtil(level, player);
         float damage = 0.0F;

         for (ClassCache.UiHelper move : route) {
            BlockState floor = mc.level.getBlockState(move.to().below());
            BlockState feet = mc.level.getBlockState(move.to());
            if (feet.getFluidState().isEmpty() && !feet.is(Blocks.COBWEB) && !floor.is(Blocks.SLIME_BLOCK)) {
               float multiplier = !floor.is(Blocks.HAY_BLOCK) && !floor.is(Blocks.HONEY_BLOCK) ? (floor.getBlock() instanceof BedBlock ? 0.5F : 1.0F) : 0.2F;
               double landingY = move.jump() != null ? move.jump().landY() : init_D_ClientLevel_BlockPos(level, move.to());
               double sourceY = init_D_ClientLevel_BlockPos(level, move.from());
               double peak = sourceY;
               if (move.jump() != null) {
                  PoolEntry maneuver = move.jump().maneuver();
                  if (maneuver == null) {
                     peak += 1.253;
                  } else {
                     NodeWalker.Inner_StreamUtil state = maneuver.start();

                     for (NodeWalker.Inner_DataUtil input : maneuver.inputs()) {
                        state = NodeWalker.init(state, input, world);
                        peak = Math.max(peak, state.y());
                     }
                  }
               }

               if (!(peak <= landingY)) {
                  int raw = access.autism$calculateFallDamage(peak - landingY, multiplier);
                  if (raw > 0) {
                     float amount = access.autism$getDamageAfterMagicAbsorb(player.damageSources().fall(), raw);
                     damage += Math.max(0.0F, amount);
                  }
               }
            }
         }

         return new ArrayUtil.Inner_DataUtil(damage, (float)health);
      } else {
         return new ArrayUtil.Inner_DataUtil(0.0F, (float)health);
      }
   }

   private static double init_D_ClientLevel_BlockPos(ClientLevel level, BlockPos stance) {
      BlockPos support = stance.below();
      VoxelShape shape = level.getBlockState(support).getCollisionShape(level, support);
      return shape.isEmpty() ? stance.getY() : support.getY() + shape.max(Axis.Y);
   }

   record Inner_DataUtil(float damage, float health) {
      boolean lethal() {
         return this.damage >= this.health;
      }
   }
}
