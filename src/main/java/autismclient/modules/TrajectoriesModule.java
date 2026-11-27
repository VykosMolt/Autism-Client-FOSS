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
   private TrajectoriesModule.Inner_DataUtil lastFrame;
   private ItemStack lastStack = ItemStack.EMPTY;
   private List<TrajectoriesModule.Inner_ConfigStore> flights = List.of();
   private int color;
   private List<TrajectoriesModule.Inner_CachePool> load = List.of();

   public TrajectoriesModule() {
      super("trajectories", "Trajectories", ae.update, "Preview flight paths and precise impact points.");

      for (TrajectoriesModule.Inner_StreamUtil item : TrajectoriesModule.Inner_StreamUtil.values()) {
         this.add(
            new BoolSetting(item.init(), item.load_Str, true)
               .description(
                  item == TrajectoriesModule.Inner_StreamUtil.FIRE
                     ? "Preview fireballs and the blocks their explosion can destroy."
                     : (
                        item == TrajectoriesModule.Inner_StreamUtil.PEARL
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

   public List<TrajectoriesModule.Inner_CachePool> paths() {
      return this.load;
   }

   private void clearFrame() {
      this.lastFrame = null;
      this.lastStack = ItemStack.EMPTY;
      this.flights = List.of();
      this.load = List.of();
   }

   @Override
   public void tick() {
      if (this.lastFrame != null && (MC == null || MC.level != this.lastFrame.level() || MC.player != this.lastFrame.player())) {
         this.clearFrame();
      }

      if (!AutismLiteVariant.enabled()) {
         TrajectoryStateCache.init(MC, this.bool("item-pearl"));
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
         trajectories.load = trajectories.computeTrajectories(partialTick);
         if (!AutismLiteVariant.enabled()) {
            TrajectoryStateCache.init(MC, trajectories.load);
         }

         ar.init_V_List(trajectories.load);
      } else {
         ar.init_V_List(List.of());
      }
   }

   private List<TrajectoriesModule.Inner_CachePool> computeTrajectories(float partialTick) {
      if (MC != null && MC.player != null && MC.level != null && !MC.player.isSpectator() && MC.player.isAlive()) {
         Player player = MC.player;
         InteractionHand hand = projectileHand(player);
         ItemStack stack = player.getItemInHand(hand);
         TrajectoriesModule.Inner_StreamUtil item = projectileKind(stack);
         if (item != null && this.bool(item.init())) {
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

            for (TrajectoriesModule.Inner_StreamUtil value : TrajectoriesModule.Inner_StreamUtil.values()) {
               if (this.bool(value.init())) {
                  mask |= 1 << value.ordinal();
               }
            }

            Vec3 inherited = ProjectileBallistics.init(player);
            TrajectoriesModule.Inner_DataUtil frame = new TrajectoriesModule.Inner_DataUtil(
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
               List<TrajectoriesModule.Inner_ConfigStore> flights = new ArrayList<>(3);

               for (ProjectileBallistics.Inner_BufferHelper shot : ProjectileBallistics.init(player, stack, true, true)) {
                  ProjectileBallistics.Inner_CachePool result = ProjectileBallistics.init(MC.level, player, shot, yaw, pitch, inherited);
                  if (result.points().size() > 1) {
                     flights.add(
                        new TrajectoriesModule.Inner_ConfigStore(
                           result, shot.info().kind() == ProjectileBallistics.Inner_StreamUtil.ROCKET, init(result.hit(), item, stack)
                        )
                     );
                  }
               }

               this.flights = List.copyOf(flights);
               this.color = init(item, stack);
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
            List<TrajectoriesModule.Inner_CachePool> result = new ArrayList<>(this.flights.size());

            for (TrajectoriesModule.Inner_ConfigStore visual : this.flights) {
               ProjectileBallistics.Inner_CachePool flight = visual.flight();
               result.add(
                  new TrajectoriesModule.Inner_CachePool(
                     flight.points(),
                     flight.hit(),
                     offset,
                     item,
                     visual.init_Z() ? -13684 : this.color,
                     visual.init_Z(),
                     result.isEmpty(),
                     visual.init_aS_BufferHelper(),
                     item == TrajectoriesModule.Inner_StreamUtil.PEARL && flight.hit() != null ? impactPoint(flight) : null
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

   static Vec3 impactPoint(ProjectileBallistics.Inner_CachePool flight) {
      return flight.hit() != null && flight.points().size() >= 2 ? flight.points().get(flight.points().size() - 2) : null;
   }

   private static TrajectoriesModule.Inner_BufferHelper init(HitResult hit, TrajectoriesModule.Inner_StreamUtil item, ItemStack stack) {
      if (hit == null) {
         return null;
      } else {
         PotionContents potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
         boolean area = (item == TrajectoriesModule.Inner_StreamUtil.SPLASH || item == TrajectoriesModule.Inner_StreamUtil.LINGERING) && (potion.hasEffects() || potion.is(Potions.WATER));
         double areaRadius = !potion.is(Potions.WATER) && item != TrajectoriesModule.Inner_StreamUtil.SPLASH ? 3.0 : 4.0;
         if (!(hit instanceof BlockHitResult block)) {
            return area ? new TrajectoriesModule.Inner_BufferHelper(hit.getLocation(), new Vec3(0.0, 1.0, 0.0), areaRadius, false, true) : null;
         } else {
            Direction direction = block.getDirection();
            Vec3 normal = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            boolean water = item == TrajectoriesModule.Inner_StreamUtil.FISHING && MC.level.getFluidState(block.getBlockPos()).is(FluidTags.WATER);
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

            return new TrajectoriesModule.Inner_BufferHelper(hit.getLocation().add(normal.scale(0.012)), normal, radius, water, area);
         }
      }
   }

   private static int init(TrajectoriesModule.Inner_StreamUtil item, ItemStack stack) {
      if (item != TrajectoriesModule.Inner_StreamUtil.SPLASH && item != TrajectoriesModule.Inner_StreamUtil.LINGERING) {
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

   static TrajectoriesModule.Inner_StreamUtil projectileKind(ItemStack stack) {
      TrajectoriesModule.Inner_StreamUtil var10000;
      switch (stack.getItem()) {
         case BowItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.BOW;
            break;
         case CrossbowItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.CROSSBOW;
            break;
         case TridentItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.TRIDENT;
            break;
         case EnderpearlItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.PEARL;
            break;
         case SnowballItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.SNOWBALL;
            break;
         case EggItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.EGG;
            break;
         case SplashPotionItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.SPLASH;
            break;
         case LingeringPotionItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.LINGERING;
            break;
         case ExperienceBottleItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.EXPERIENCE;
            break;
         case FishingRodItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.FISHING;
            break;
         case WindChargeItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.WIND;
            break;
         case FireChargeItem ignored:
            var10000 = TrajectoriesModule.Inner_StreamUtil.FIRE;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public record Inner_BufferHelper(Vec3 point, Vec3 normal, double init_D, boolean init_Z, boolean load_Z) {
   }

   public record Inner_CachePool(
      List<Vec3> points,
      HitResult hit,
      Vec3 init_Vec3,
      TrajectoriesModule.Inner_StreamUtil init_aS_StreamUtil,
      int color,
      boolean init_Z,
      boolean load_Z,
      TrajectoriesModule.Inner_BufferHelper surface,
      Vec3 load_Vec3
   ) {
   }

   private record Inner_ConfigStore(ProjectileBallistics.Inner_CachePool flight, boolean init_Z, TrajectoriesModule.Inner_BufferHelper init_aS_BufferHelper) {
   }

   private record Inner_DataUtil(
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

   public enum Inner_StreamUtil {
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

      Inner_StreamUtil(String key, String label) {
         this.init_Str = key;
         this.load_Str = label;
      }

      public String init() {
         return "item-" + this.init_Str;
      }
   }
}
