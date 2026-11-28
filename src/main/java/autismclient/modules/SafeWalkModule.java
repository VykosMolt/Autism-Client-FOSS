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

public final class SafeWalkModule extends Module {
   private static final Minecraft CLIENT = Minecraft.getInstance();
   private static final double load_D = 0.55;
   private static final double read = 0.05;
   static final double init_D = 0.15;
   private static final int init_I = 3;
   private final Random random = new Random();
   private long holdUntil;
   private boolean holding;
   private int clearTicks;

   static double clampEdgeDistance(double configured) {
      return Math.max(0.15, configured);
   }

   public SafeWalkModule() {
      super("safe-walk", "SafeWalk", ae.load_ae, "Sneaks at ledges.");
      this.add(new IntSetting("look-down", "Look Down", 25, 0, 90, 1).unit("deg").description("Minimum downward look"));
      this.add(new DoubleSetting("edge-distance", "Edge Distance", 0.3, 0.15, 1.5, 0.05).unit("blocks").description("Ledge probe distance"));
      this.add(new RangeSetting("hold-time", "Hold Time", new ValueRange(100, 200), 0.0, 500.0, 5.0).unit("ms").description("Random sneak hold time"));
   }

   @Override
   public void onDisable() {
      this.holding = false;
      this.holdUntil = 0L;
      this.clearTicks = 0;
   }

   public static Input adjustInput(ClientInput source, Input original) {
      if (original != null && CLIENT != null && CLIENT.player != null && CLIENT.player.input == source) {
         if (al.init_Module_Str("safe-walk") instanceof SafeWalkModule safeWalk) {
            if (!safeWalk.isEnabled()) {
               safeWalk.holding = false;
               return original;
            } else if (!safeWalk.shouldHold()) {
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

   private boolean shouldHold() {
      if (isScaffoldEnabled()) {
         this.holding = false;
         return false;
      }

      boolean atEdge = this.isNearEdge();
      long now = System.currentTimeMillis();
      this.clearTicks = atEdge ? 0 : this.clearTicks + 1;
      if (this.holding && now < this.holdUntil) {
         return true;
      }

      if (atEdge) {
         this.holding = true;
         this.holdUntil = now + this.rollHoldMillis();
         return true;
      }

      if (keepHolding(this.holding, this.clearTicks, 3)) {
         return true;
      }

      this.holding = false;
      return false;
   }

   static boolean keepHolding(boolean holding, int clearTicks, int releaseTicks) {
      return holding && clearTicks < releaseTicks;
   }

   private long rollHoldMillis() {
      ValueRange band = ValueRange.parse(this.value("hold-time"), new ValueRange(100, 200));
      return Math.round(band.random(this.random));
   }

   private boolean isNearEdge() {
      LocalPlayer player = CLIENT.player;
      if (player == null || CLIENT.level == null) {
         return false;
      } else if (player.onGround() && !player.isPassenger() && !player.getAbilities().flying) {
         return player.getXRot() < this.integer("look-down") ? false : this.edgeAhead(clampEdgeDistance(this.decimal("edge-distance")));
      } else {
         return false;
      }
   }

   private boolean edgeAhead(double distance) {
      LocalPlayer player = CLIENT.player;
      Vec3 velocity = player.getDeltaMovement();
      Vec3 direction = velocity.horizontalDistanceSqr() > 1.0E-6
         ? new Vec3(velocity.x, 0.0, velocity.z).normalize()
         : Vec3.directionFromRotation(0.0F, player.getYRot());
      Vec3 probe = player.position().add(direction.scale(distance));
      BlockPos underProbe = BlockPos.containing(probe).below();
      if (!CLIENT.level.isOutsideBuildHeight(underProbe)
         && ScaffoldModule.init_Z_BlockState_3(CLIENT.level.getBlockState(underProbe), CLIENT.level, underProbe, CollisionContext.of(player))) {
         return false;
      }

      double half = Math.max(0.05, player.getBbWidth() / 2.0 - 0.05);
      AABB below = new AABB(probe.x - half, probe.y - 0.55, probe.z - half, probe.x + half, probe.y, probe.z + half);
      return CLIENT.level.noCollision(player, below);
   }

   private static boolean isScaffoldEnabled() {
      Module scaffold = al.init_Module_Str("scaffold");
      return scaffold != null && scaffold.isEnabled();
   }
}
