package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.util.cn;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class N extends Module {
   private final RegistryListSetting init;

   public N() {
      super("hand-no-clip", "HandNoClip", ae.read, "Ignore selected blocks when aiming a manual interaction.");
      this.add(new ChoiceSetting("ignore", "Ignore", "Specified", "Specified", "All").build());
      this.init = this.add(
         RegistryListSetting.blocks("blocks", "Blocks", "minecraft:cobweb").visibleWhen(() -> "Specified".equals(this.choice("ignore"))).build()
      );
      this.add(new BoolSetting("place", "Place", true).build());
      this.add(new BoolSetting("break", "Break and Attack", true).build());
   }

   public static HitResult init(Minecraft mc, HitResult pick, boolean attack, boolean entities) {
      if (pick != null
         && pick.getType() == Type.BLOCK
         && mc.player != null
         && mc.level != null
         && !aD.read_Z()
         && !aC.init_Z()
         && !cn.init_Z()
         && cn.init_ce_DataUtil() == null
         && al.init_Module_Str("hand-no-clip") instanceof N module
         && module.isEnabled()
         && module.bool(attack ? "break" : "place")) {
         LocalPlayer player = mc.player;
         final boolean all = "All".equals(module.choice("ignore"));
         final List<String> ignored = module.init.get();
         Vec3 eye = player.getEyePosition();
         Vec3 direction = player.getViewVector(1.0F);
         double blockRange = player.blockInteractionRange();
         BlockHitResult block = mc.level
            .clip(
               new ClipContext(eye, eye.add(direction.scale(blockRange)), Block.OUTLINE, Fluid.NONE, player) {
                  @Override
                  public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos) {
                     return !all && !ignored.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString())
                        ? super.getBlockShape(state, level, pos)
                        : Shapes.empty();
                  }
               }
            );
         if (!entities) {
            return block;
         }

         double entityRange = player.entityInteractionRange();
         if (block.getType() == Type.BLOCK) {
            entityRange = Math.min(entityRange, eye.distanceTo(block.getLocation()));
         }

         Vec3 stretch = direction.scale(entityRange);
         Vec3 end = eye.add(stretch);
         EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            player, eye, end, new AABB(eye, end).inflate(1.0), entity -> entity.isPickable() && !entity.isSpectator(), entityRange * entityRange
         );
         return hit == null ? block : hit;
      } else {
         return pick;
      }
   }
}
