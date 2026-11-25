package autismclient.modules;

import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.ValueRange;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class aH extends Module {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final double load_D = 0.55;
   private static final double read = 0.05;
   static final double init_D = 0.15;
   private static final int init_I = 3;
   private final Random init_Random = new Random();
   private long init_J;
   private boolean init_Z;
   private int load_I;

   static double init_D_D_2(double configured) {
      return Math.max(0.15, configured);
   }

   public aH() {
      super("safe-walk", "SafeWalk", ae.load_ae, "Sneaks at ledges.");
      this.add(new IntSetting("look-down", "Look Down", 25, 0, 90, 1).unit("deg").description("Minimum downward look"));
      this.add(new DoubleSetting("edge-distance", "Edge Distance", 0.3, 0.15, 1.5, 0.05).unit("blocks").description("Ledge probe distance"));
      this.add(new RangeSetting("hold-time", "Hold Time", new ValueRange(100, 200), 0.0, 500.0, 5.0).unit("ms").description("Random sneak hold time"));
   }

   @Override
   public void onDisable() {
      this.init_Z = false;
      this.init_J = 0L;
      this.load_I = 0;
   }

   public static Input init(ClientInput source, Input original) {
      if (original != null && init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.player.input == source) {
         if (al.init_Module_Str("safe-walk") instanceof aH safeWalk) {
            if (!safeWalk.isEnabled()) {
               safeWalk.init_Z = false;
               return original;
            } else if (!safeWalk.init_Z_25()) {
               return original;
            } else {
               return original.shift()
                  ? original
                  : new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), true, original.sprint());
            }
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   private boolean init_Z_25() {
      if (read()) {
         this.init_Z = false;
         return false;
      }

      boolean atEdge = this.load();
      long now = System.currentTimeMillis();
      this.load_I = atEdge ? 0 : this.load_I + 1;
      if (this.init_Z && now < this.init_J) {
         return true;
      }

      if (atEdge) {
         this.init_Z = true;
         this.init_J = now + this.init_J_6();
         return true;
      }

      if (init(this.init_Z, this.load_I, 3)) {
         return true;
      }

      this.init_Z = false;
      return false;
   }

   static boolean init(boolean holding, int clearTicks, int releaseTicks) {
      return holding && clearTicks < releaseTicks;
   }

   private long init_J_6() {
      ValueRange band = ValueRange.parse(this.value("hold-time"), new ValueRange(100, 200));
      return Math.round(band.random(this.init_Random));
   }

   private boolean load() {
      LocalPlayer player = init_Minecraft.player;
      if (player == null || init_Minecraft.level == null) {
         return false;
      } else if (player.onGround() && !player.isPassenger() && !player.getAbilities().flying) {
         return player.getXRot() < this.integer("look-down") ? false : this.init_Z_D_2(init_D_D_2(this.decimal("edge-distance")));
      } else {
         return false;
      }
   }

   private boolean init_Z_D_2(double distance) {
      LocalPlayer player = init_Minecraft.player;
      Vec3 velocity = player.getDeltaMovement();
      Vec3 direction = velocity.horizontalDistanceSqr() > 1.0E-6
         ? new Vec3(velocity.x, 0.0, velocity.z).normalize()
         : Vec3.directionFromRotation(0.0F, player.getYRot());
      Vec3 probe = player.position().add(direction.scale(distance));
      BlockPos underProbe = BlockPos.containing(probe).below();
      if (!init_Minecraft.level.isOutsideBuildHeight(underProbe)
         && ScaffoldModule.init_Z_BlockState_3(init_Minecraft.level.getBlockState(underProbe), init_Minecraft.level, underProbe, CollisionContext.of(player))) {
         return false;
      }

      double half = Math.max(0.05, player.getBbWidth() / 2.0 - 0.05);
      AABB below = new AABB(probe.x - half, probe.y - 0.55, probe.z - half, probe.x + half, probe.y, probe.z + half);
      return init_Minecraft.level.noCollision(player, below);
   }

   private static boolean read() {
      Module scaffold = al.init_Module_Str("scaffold");
      return scaffold != null && scaffold.isEnabled();
   }
}
