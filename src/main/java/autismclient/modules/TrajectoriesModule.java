package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.util.AutismLiteVariant;
import autismclient.util.RotationMath;
import autismclient.util.SilentMovementCoordinator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TrajectoriesModule extends Module {
   private TrajectoriesModule.Frame lastFrame;
   private ItemStack lastStack = ItemStack.EMPTY;
   private List<TrajectoriesModule.Flight> flights = List.of();
   private int color;
   private List<TrajectoriesModule.TrajectoryPath> trajectories = List.of();

   public TrajectoriesModule() {
      super("trajectories", "Trajectories", ae.update, "Preview flight paths and precise impact points.");

      for (TrajectoriesModule.ProjectileItem item : TrajectoriesModule.ProjectileItem.values()) {
         this.add(
            new BoolSetting(item.settingKey(), item.load_Str, true)
               .description(
                  item == TrajectoriesModule.ProjectileItem.FIRE
                     ? "Preview fireballs and the blocks their explosion can destroy."
                     : (
                        item == TrajectoriesModule.ProjectileItem.PEARL
                           ? "Show the held flight and remember your thrown pearl's final landing until it arrives."
                           : "Show the trajectory when holding this item."
                     )
               )
               .build()
         );
      }

      this.add(
         new IntSetting("pearl-warning-damage", "Damage Warning", 50, 1, 100, 1)
            .unit("% HP")
            .group("Pearl Safety")
            .description("Yellow at this percentage of maximum health lost, including pearl and landing damage; red for lethal falls, hazards or void.")
            .visibleWhen(() -> this.bool("item-pearl"))
      );
   }

   public List<TrajectoriesModule.TrajectoryPath> paths() {
      return this.trajectories;
   }

   private void clearFrame() {
      this.lastFrame = null;
      this.lastStack = ItemStack.EMPTY;
      this.flights = List.of();
      this.trajectories = List.of();
   }

   @Override
   public void tick() {
      if (this.lastFrame != null && (MC == null || MC.level != this.lastFrame.level() || MC.player != this.lastFrame.player())) {
         this.clearFrame();
      }

      if (!AutismLiteVariant.enabled()) {
         TrajectoryStateCache.tickPearls(MC, this.bool("item-pearl"));
      }
   }

   @Override
   public void onGameLeft() {
      this.onDisable();
   }

   @Override
   public void onDisable() {
      this.clearFrame();
      ar.init_V_List(List.of());
      if (!AutismLiteVariant.enabled()) {
         TrajectoryStateCache.reset();
      }
   }

   public static void computePaths(float partialTick) {
      if (al.init_Module_Str("trajectories") instanceof TrajectoriesModule trajectories && trajectories.isEnabled()) {
         trajectories.trajectories = trajectories.computeTrajectories(partialTick);
         if (!AutismLiteVariant.enabled()) {
            TrajectoryStateCache.refresh(MC, trajectories.trajectories);
         }

         ar.init_V_List(trajectories.trajectories);
      } else {
         ar.init_V_List(List.of());
      }
   }

   private List<TrajectoriesModule.TrajectoryPath> computeTrajectories(float partialTick) {
      if (MC != null && MC.player != null && MC.level != null && !MC.player.isSpectator() && MC.player.isAlive()) {
         Player player = MC.player;
         InteractionHand hand = projectileHand(player);
         ItemStack stack = player.getItemInHand(hand);
         TrajectoriesModule.ProjectileItem item = projectileKind(stack);
         if (item != null && this.bool(item.settingKey())) {
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            RotationMath.Rotation rotation = SilentMovementCoordinator.getHeadRenderRotation(MC.player);
            if (rotation != null) {
               yaw = rotation.yaw();
               pitch = rotation.pitch();
            }

            if (f.load()) {
               yaw = f.init(yaw);
               pitch = f.load(pitch);
            }

            int mask = 0;

            for (TrajectoriesModule.ProjectileItem value : TrajectoriesModule.ProjectileItem.values()) {
               if (this.bool(value.settingKey())) {
                  mask |= 1 << value.ordinal();
               }
            }

            Vec3 inherited = ProjectileBallistics.inheritedVelocity(player);
            TrajectoriesModule.Frame frame = new TrajectoriesModule.Frame(
               MC.level,
               player,
               player.tickCount,
               player.position(),
               player.getEyeY(),
               inherited,
               yaw,
               pitch,
               player.getTicksUsingItem(),
               mask,
               hand,
               player.fishing != null
            );
            if (!frame.equals(this.lastFrame) || !ItemStack.matches(stack, this.lastStack)) {
               List<TrajectoriesModule.Flight> flights = new ArrayList<>(3);

               for (ProjectileBallistics.ProjectileShot shot : ProjectileBallistics.shots(player, stack, true, true)) {
                  ProjectileBallistics.Trajectory result = ProjectileBallistics.simulate(MC.level, player, shot, yaw, pitch, inherited);
                  if (result.points().size() > 1) {
                     flights.add(
                        new TrajectoriesModule.Flight(
                           result, shot.info().kind() == ProjectileBallistics.ProjectileKind.ROCKET, impactFor(result.hit(), item, stack)
                        )
                     );
                  }
               }

               this.flights = List.copyOf(flights);
               this.color = colorFor(item, stack);
               this.lastFrame = frame;
               this.lastStack = stack.copy();
            }

            Vec3 interpolated = player.tickCount == 0
               ? player.position()
               : new Vec3(
                  Math.fma(partialTick, player.getX() - player.xOld, player.xOld),
                  Math.fma(partialTick, player.getY() - player.yOld, player.yOld),
                  Math.fma(partialTick, player.getZ() - player.zOld, player.zOld)
               );
            Vec3 offset = interpolated.subtract(player.position());
            List<TrajectoriesModule.TrajectoryPath> result = new ArrayList<>(this.flights.size());

            for (TrajectoriesModule.Flight visual : this.flights) {
               ProjectileBallistics.Trajectory flight = visual.flight();
               result.add(
                  new TrajectoriesModule.TrajectoryPath(
                     flight.points(),
                     flight.hit(),
                     offset,
                     item,
                     visual.init_Z() ? -13684 : this.color,
                     visual.init_Z(),
                     result.isEmpty(),
                     visual.init_aS_BufferHelper(),
                     item == TrajectoriesModule.ProjectileItem.PEARL && flight.hit() != null ? impactPoint(flight) : null
                  )
               );
            }

            return List.copyOf(result);
         } else {
            return List.of();
         }
      } else {
         this.clearFrame();
         return List.of();
      }
   }

   static Vec3 impactPoint(ProjectileBallistics.Trajectory flight) {
      return flight.hit() != null && flight.points().size() >= 2 ? flight.points().get(flight.points().size() - 2) : null;
   }

   private static TrajectoriesModule.Impact impactFor(HitResult hit, TrajectoriesModule.ProjectileItem item, ItemStack stack) {
      if (hit == null) {
         return null;
      } else {
         PotionContents potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
         boolean area = (item == TrajectoriesModule.ProjectileItem.SPLASH || item == TrajectoriesModule.ProjectileItem.LINGERING) && (potion.hasEffects() || potion.is(Potions.WATER));
         double areaRadius = !potion.is(Potions.WATER) && item != TrajectoriesModule.ProjectileItem.SPLASH ? 3.0 : 4.0;
         if (!(hit instanceof BlockHitResult block)) {
            return area ? new TrajectoriesModule.Impact(hit.getLocation(), new Vec3(0.0, 1.0, 0.0), areaRadius, false, true) : null;
         } else {
            Direction direction = block.getDirection();
            Vec3 normal = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            boolean water = item == TrajectoriesModule.ProjectileItem.FISHING && MC.level.getFluidState(block.getBlockPos()).is(FluidTags.WATER);
            double var10000;
            switch (item) {
               case PEARL:
                  var10000 = 0.35;
                  break;
               case SNOWBALL:
               case EGG:
               case EXPERIENCE:
               default:
                  var10000 = 0.24;
                  break;
               case SPLASH:
               case LINGERING:
                  var10000 = area ? areaRadius : 0.24;
                  break;
               case FISHING:
                  var10000 = water ? 0.5 : 0.22;
                  break;
               case WIND:
                  var10000 = 0.45;
                  break;
               case FIRE:
                  var10000 = 0.4;
            }

            double radius = var10000;
            if (area || water) {
               normal = new Vec3(0.0, 1.0, 0.0);
            }

            return new TrajectoriesModule.Impact(hit.getLocation().add(normal.scale(0.012)), normal, radius, water, area);
         }
      }
   }

   private static int colorFor(TrajectoriesModule.ProjectileItem item, ItemStack stack) {
      if (item != TrajectoriesModule.ProjectileItem.SPLASH && item != TrajectoriesModule.ProjectileItem.LINGERING) {
         int var10000;
         switch (item) {
            case BOW:
            case CROSSBOW:
               var10000 = -5711361;
               break;
            case TRIDENT:
               var10000 = -7937320;
               break;
            case PEARL:
               var10000 = -8329771;
               break;
            case SNOWBALL:
               var10000 = -1903361;
               break;
            case EGG:
               var10000 = -991551;
               break;
            case SPLASH:
            case LINGERING:
            default:
               var10000 = -3032321;
               break;
            case EXPERIENCE:
               var10000 = -9081;
               break;
            case FISHING:
               var10000 = -5908241;
               break;
            case WIND:
               var10000 = -4067841;
               break;
            case FIRE:
               var10000 = -19311;
         }

         return var10000;
      } else {
         int rgb = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
         int r = ((rgb >> 16 & 0xFF) * 3 + 510) / 5;
         int g = ((rgb >> 8 & 0xFF) * 3 + 510) / 5;
         int b = ((rgb & 0xFF) * 3 + 510) / 5;
         return 0xFF000000 | r << 16 | g << 8 | b;
      }
   }

   private static InteractionHand projectileHand(Player player) {
      if (player.isUsingItem()) {
         return player.getUsedItemHand();
      }

      ItemStack main = player.getMainHandItem();
      return projectileKind(main) == null && main.getUseDuration(player) <= 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   static TrajectoriesModule.ProjectileItem projectileKind(ItemStack stack) {
      TrajectoriesModule.ProjectileItem var10000;
      switch (stack.getItem()) {
         case BowItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.BOW;
            break;
         case CrossbowItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.CROSSBOW;
            break;
         case TridentItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.TRIDENT;
            break;
         case EnderpearlItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.PEARL;
            break;
         case SnowballItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.SNOWBALL;
            break;
         case EggItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.EGG;
            break;
         case SplashPotionItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.SPLASH;
            break;
         case LingeringPotionItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.LINGERING;
            break;
         case ExperienceBottleItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.EXPERIENCE;
            break;
         case FishingRodItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.FISHING;
            break;
         case WindChargeItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.WIND;
            break;
         case FireChargeItem ignored:
            var10000 = TrajectoriesModule.ProjectileItem.FIRE;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public record Impact(Vec3 point, Vec3 normal, double init_D, boolean init_Z, boolean load_Z) {
   }

   public record TrajectoryPath(
      List<Vec3> points,
      HitResult hit,
      Vec3 init_Vec3,
      TrajectoriesModule.ProjectileItem init_aS_StreamUtil,
      int color,
      boolean init_Z,
      boolean load_Z,
      TrajectoriesModule.Impact surface,
      Vec3 load_Vec3
   ) {
   }

   private record Flight(ProjectileBallistics.Trajectory flight, boolean init_Z, TrajectoriesModule.Impact init_aS_BufferHelper) {
   }

   private record Frame(
      Object level,
      Player player,
      int init_I,
      Vec3 init_Vec3,
      double init_D,
      Vec3 load_Vec3,
      float init_F,
      float load_F,
      int load_I,
      int read,
      InteractionHand init_InteractionHand,
      boolean init_Z
   ) {
   }

   public enum ProjectileItem {
      BOW("bow", "Bow"),
      CROSSBOW("crossbow", "Crossbow"),
      TRIDENT("trident", "Trident"),
      PEARL("pearl", "Ender Pearls"),
      SNOWBALL("snowball", "Snowballs"),
      EGG("egg", "Eggs"),
      SPLASH("splash-potion", "Splash Potions"),
      LINGERING("lingering-potion", "Lingering Potions"),
      EXPERIENCE("experience", "Experience Bottles"),
      FISHING("fishing", "Fishing Rod"),
      WIND("wind-charge", "Wind Charges"),
      FIRE("fire-charge", "Fire Charges");

      final String init_Str;
      final String load_Str;

      ProjectileItem(String key, String label) {
         this.init_Str = key;
         this.load_Str = label;
      }

      public String settingKey() {
         return "item-" + this.init_Str;
      }
   }
}
