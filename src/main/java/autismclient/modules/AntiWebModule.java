package autismclient.modules;

import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.mixin.accessor.AutismEntityAccessor;
import autismclient.util.SilentMovementCoordinator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class AntiWebModule extends Module {
   public AntiWebModule() {
      super("anti-web", "AntiWeb", ae.load_ae, "Remove cobweb slowdown or change how you move in webs.");
      this.add(new ChoiceSetting("mode", "Mode", "Pass Through", "Pass Through", "Flight", "Solid").build());
      this.add(new DoubleSetting("horizontal-speed", "Horizontal Speed", 0.64, 0.1, 2.0, 0.01).visibleWhen(() -> "Flight".equals(this.choice("mode"))).build());
      this.add(new DoubleSetting("vertical-speed", "Vertical Speed", 0.64, 0.1, 2.0, 0.01).visibleWhen(() -> "Flight".equals(this.choice("mode"))).build());
   }

   private static AntiWebModule activeFor(Entity entity) {
      return entity != null && entity == MC.player && !aD.read_Z() && al.init_Module_Str("anti-web") instanceof AntiWebModule module && module.isEnabled()
         ? module
         : null;
   }

   public static boolean isActiveFor(Entity entity) {
      return activeFor(entity) != null;
   }

   public static boolean isSolidMode(Entity entity) {
      AntiWebModule module = activeFor(entity);
      return module != null && "Solid".equals(module.choice("mode"));
   }

   @Override
   public Vec3 onPlayerMove(MoverType type, Vec3 movement) {
      LocalPlayer player = MC.player;
      if (player != null && !player.isPassenger() && type == MoverType.SELF && "Flight".equals(this.choice("mode"))) {
         AABB box = player.getBoundingBox();

         for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (box.intersects(new AABB(pos)) && player.level().getBlockState(pos).is(Blocks.COBWEB)) {
               Vec2 input = player.input.getMoveVector();
               Input keys = player.input.keyPresses;
               double y = Boolean.compare(keys.jump(), keys.shift()) * this.decimal("vertical-speed");
               Vec3 flight = AutismEntityAccessor.autism$getInputVector(
                  new Vec3(input.x, 0.0, input.y), (float)this.decimal("horizontal-speed"), SilentMovementCoordinator.getPlayerYaw(player, player.getYRot())
               );
               return new Vec3(flight.x, y, flight.z);
            }
         }

         return movement;
      } else {
         return movement;
      }
   }
}
