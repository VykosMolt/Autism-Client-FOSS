package autismclient.util;

import autismclient.mixin.accessor.AutismKeyMappingAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.modules.Module;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ManualInteractionYield {
   private static final int load = 8;
   private static final int read = 25;
   private static final int write = 2;
   static final int init_I = 8;
   private static final long init_J = 1500000000L;
   private static int update = -2147483648;
   private static int sync;
   private static boolean init_Z;
   private static int flush;
   private static int close;
   private static int open;
   private static BlockPos init_BlockPos;
   private static int poll;

   private ManualInteractionYield() {
   }

   public static boolean init() {
      load(Minecraft.getInstance());
      return init_Z;
   }

   public static boolean load() {
      load(Minecraft.getInstance());
      return init_Z && read() && flush < 25;
   }

   private static boolean read() {
      return SilentMovementCoordinator.activeRotation() != null && SilentRotationManager.isReturningToPlayer();
   }

   public static void init_V(Minecraft mc) {
      if (mc != null && mc.options != null) {
         KeyMapping attack = mc.options.keyAttack;
         KeyMapping use = mc.options.keyUse;
         boolean auraUse = !AutismLiteVariant.enabled() && CombatInteractionController.sync();
         if (auraUse) {
            open = 0;
         }

         if (load()) {
            flush++;
            close = Math.min(2, close + init(attack));
            if (!auraUse) {
               open = Math.min(2, open + init(use));
            }
         } else {
            if (!init_Z || !read()) {
               flush = 0;
            }

            if (close > 0) {
               init(attack, close);
            }

            if (open > 0) {
               init(use, open);
            }

            close = 0;
            open = 0;
         }
      }
   }

   public static HitResult init(Minecraft mc, HitResult silentPick) {
      if (mc == null || !(mc.player != null && silentPick instanceof BlockHitResult blockPick) || blockPick.getType() != Type.BLOCK) {
         return silentPick;
      } else if (!SilentMovementCoordinator.isSafeWalkActive() && SilentMovementCoordinator.activeRotation() != null) {
         BlockHitResult camera = init_BlockHitResult(mc);
         if (camera != null && camera.getBlockPos().equals(blockPick.getBlockPos())) {
            return silentPick;
         }

         LocalPlayer player = mc.player;
         return BlockHitResult.miss(player.getEyePosition(), blockPick.getDirection(), player.blockPosition());
      } else {
         return silentPick;
      }
   }

   private static void load(Minecraft mc) {
      if (mc != null) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (tick != update) {
            update = tick;
            if (mc.player != null && mc.level != null && mc.options != null && !autismclient.modules.FreecamController.isActive()) {
               boolean destroying = mc.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor && accessor.autism$isDestroying();
               if (!AutismLiteVariant.enabled() && autismclient.modules.BedNukerModule.read_Z_10()) {
                  destroying = false;
               }

               boolean attackDown = KeyMappingControl.of(mc.options.keyAttack).autism$isActuallyDown();
               boolean useDown = KeyMappingControl.of(mc.options.keyUse).autism$isActuallyDown();
               BlockHitResult camera = !attackDown && !useDown ? null : init_BlockHitResult(mc);
               if (attackDown && camera != null) {
                  if (camera.getBlockPos().equals(init_BlockPos)) {
                     poll++;
                  } else {
                     init_BlockPos = camera.getBlockPos().immutable();
                     poll = 1;
                  }
               } else {
                  init_BlockPos = null;
                  poll = 0;
               }

               boolean fighting = RecentCombatTracker.init(1500000000L);
               boolean mining = fighting ? attackDown && camera != null && init(true, poll, write()) : destroying || attackDown && camera != null;
               boolean auraUse = !AutismLiteVariant.enabled() && CombatInteractionController.sync();
               boolean direct = mining || useDown && !auraUse && camera != null && init(mc, camera);
               sync = init(sync, direct);
               init_Z = direct || sync > 0;
               if (!init_Z) {
                  flush = 0;
               }
            } else {
               sync = 0;
               init_Z = false;
               flush = 0;
               init_BlockPos = null;
               poll = 0;
            }
         }
      }
   }

   static boolean init(boolean fighting, int heldTicks, boolean autoClicker) {
      return !fighting ? true : !autoClicker && heldTicks >= 8;
   }

   private static boolean write() {
      Module module = autismclient.modules.al.init_Module_Str("auto-clicker");
      return module != null && module.isEnabled();
   }

   static int init(int grace, boolean direct) {
      return direct ? 8 : Math.max(0, grace - 1);
   }

   static BlockHitResult init_BlockHitResult(Minecraft mc) {
      LocalPlayer player = mc.player;
      if (player != null && mc.level != null) {
         Vec3 eye = player.getEyePosition();
         Vec3 look = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
         Vec3 end = eye.add(look.scale(player.blockInteractionRange()));
         BlockHitResult block = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
         if (block.getType() != Type.BLOCK) {
            return null;
         }

         double reach = eye.distanceTo(block.getLocation());
         AABB sweep = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);
         boolean entityInFront = ProjectileUtil.getEntityHitResult(
               player, eye, block.getLocation(), sweep, entity -> !entity.isSpectator() && entity.isPickable(), reach * reach
            )
            != null;
         return entityInFront ? null : block;
      } else {
         return null;
      }
   }

   private static boolean init(Minecraft mc, BlockHitResult hit) {
      LocalPlayer player = mc.player;
      if (!init(player.getMainHandItem()) && !init(player.getOffhandItem())) {
         BlockPos pos = hit.getBlockPos();
         BlockState state = mc.level.getBlockState(pos);
         return state.getMenuProvider(mc.level, pos) != null
            || state.is(BlockTags.DOORS)
            || state.is(BlockTags.TRAPDOORS)
            || state.is(BlockTags.FENCE_GATES)
            || state.is(BlockTags.BUTTONS)
            || state.getBlock() instanceof LeverBlock
            || state.getBlock() instanceof BedBlock;
      } else {
         return true;
      }
   }

   private static boolean init(ItemStack stack) {
      return stack.getItem() instanceof BlockItem || stack.getItem() instanceof BucketItem || stack.getItem() instanceof FlintAndSteelItem;
   }

   private static int init(KeyMapping key) {
      AutismKeyMappingAccessor accessor = (AutismKeyMappingAccessor)key;
      int clicks = accessor.autism$clickCount();
      accessor.autism$setClickCount(0);
      return clicks;
   }

   private static void init(KeyMapping key, int clicks) {
      AutismKeyMappingAccessor accessor = (AutismKeyMappingAccessor)key;
      accessor.autism$setClickCount(accessor.autism$clickCount() + clicks);
   }
}
