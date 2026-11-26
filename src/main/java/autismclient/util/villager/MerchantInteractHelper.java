package autismclient.util.villager;

import autismclient.util.RotationMath;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class MerchantInteractHelper {
   private MerchantInteractHelper() {
   }

   public static boolean init(Minecraft mc, Entity entity, double range) {
      if (mc.player != null && mc.level != null && entity != null && entity.isAlive() && !entity.isRemoved()) {
         Vec3 eye = mc.player.getEyePosition();
         Vec3 point = entity.getBoundingBox().getCenter();
         double reach = Math.min(range, mc.player.entityInteractionRange());
         return eye.distanceToSqr(point) <= reach * reach
            && mc.level.clip(new ClipContext(eye, point, Block.COLLIDER, Fluid.NONE, mc.player)).getType() == Type.MISS;
      } else {
         return false;
      }
   }

   public static Entity init(Minecraft mc, boolean aimed, double range) {
      if (mc.player != null && mc.level != null) {
         if (aimed) {
            return mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Merchant && init(mc, hit.getEntity(), range)
               ? hit.getEntity()
               : null;
         }

         Entity best = null;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Merchant && init(mc, entity, range) && (best == null || mc.player.distanceToSqr(entity) < mc.player.distanceToSqr(best))) {
               best = entity;
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static boolean init(Minecraft mc, Vec3 point) {
      if (mc.player != null && mc.getConnection() != null) {
         RotationMath.Rotation current = RotationMath.init(mc.player);
         RotationMath.Rotation goal = RotationMath.init(point, mc.player.getEyePosition());
         RotationMath.Rotation next = RotationMath.init_ce_DataUtil(RotationMath.init(current, goal, 30.0F, 30.0F), current);
         RotationMath.init_V_LocalPlayer(mc.player, next, false);
         if (RotationMath.init_F(next, goal) > 3.0F) {
            return false;
         }

         mc.getConnection().send(new Rot(mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), mc.player.horizontalCollision));
         return true;
      } else {
         return false;
      }
   }

   public static boolean load(Minecraft mc, Entity entity, double range) {
      if (init(mc, entity, range)
         && mc.gameMode != null
         && mc.gui.screen() == null
         && mc.player.containerMenu == mc.player.inventoryMenu
         && init(mc, entity.getBoundingBox().getCenter())) {
         mc.gameMode.interact(mc.player, entity, new EntityHitResult(entity, entity.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND);
         return true;
      } else {
         return false;
      }
   }
}
