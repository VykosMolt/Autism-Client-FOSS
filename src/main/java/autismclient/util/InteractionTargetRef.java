package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class InteractionTargetRef {
   private final InteractionTargetRef.Inner_DataUtil init_E_DataUtil;
   private final BlockPos init_BlockPos;
   private final String init_Str;
   private final InteractionHand init_InteractionHand;
   private final Vec3 init_Vec3;
   private final Direction init_Direction;
   private final boolean init_Z;

   private InteractionTargetRef(InteractionTargetRef.Inner_DataUtil kind, BlockPos blockPos, String entityRef, InteractionHand hand, Vec3 hitPos, Direction blockSide, boolean insideBlock) {
      this.init_E_DataUtil = kind;
      this.init_BlockPos = blockPos == null ? null : blockPos.immutable();
      this.init_Str = entityRef == null ? "" : entityRef;
      this.init_InteractionHand = hand == null ? InteractionHand.MAIN_HAND : hand;
      this.init_Vec3 = hitPos;
      this.init_Direction = blockSide;
      this.init_Z = insideBlock;
   }

   public static InteractionTargetRef init_E_BlockPos(BlockPos pos) {
      return pos == null ? null : new InteractionTargetRef(InteractionTargetRef.Inner_DataUtil.BLOCK, pos, "", InteractionHand.MAIN_HAND, null, null, false);
   }

   public static InteractionTargetRef init(BlockHitResult hitResult, InteractionHand hand) {
      return hitResult != null && hitResult.getBlockPos() != null
         ? new InteractionTargetRef(InteractionTargetRef.Inner_DataUtil.BLOCK, hitResult.getBlockPos(), "", hand, hitResult.getLocation(), hitResult.getDirection(), hitResult.isInside())
         : null;
   }

   public static InteractionTargetRef init(Entity entity, InteractionHand hand) {
      return entity == null ? null : new InteractionTargetRef(InteractionTargetRef.Inner_DataUtil.ENTITY_INTERACT, null, init_Str_Entity(entity), hand, null, null, false);
   }

   public static InteractionTargetRef init(Entity entity, InteractionHand hand, Vec3 hitPos) {
      return entity == null ? null : new InteractionTargetRef(InteractionTargetRef.Inner_DataUtil.ENTITY_INTERACT_AT, null, init_Str_Entity(entity), hand, hitPos, null, false);
   }

   public static InteractionTargetRef init_E_Str(String entityRef) {
      return entityRef != null && !entityRef.isBlank()
         ? new InteractionTargetRef(InteractionTargetRef.Inner_DataUtil.ENTITY_INTERACT, null, entityRef.trim(), InteractionHand.MAIN_HAND, null, null, false)
         : null;
   }

   public static String init_Str_Entity(Entity entity) {
      if (entity == null) {
         return "";
      }

      String uuid = entity.getStringUUID();
      String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
      String displayName = entity.getDisplayName().getString().replaceAll("§.", "").trim();
      return "~" + uuid + "~" + typeId + "~" + displayName;
   }

   public InteractionTargetRef.Inner_DataUtil init_E_DataUtil() {
      return this.init_E_DataUtil;
   }

   public BlockPos init_BlockPos() {
      return this.init_BlockPos;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public boolean init_Z() {
      return this.init_E_DataUtil == InteractionTargetRef.Inner_DataUtil.BLOCK;
   }

   public boolean init_Z_Minecraft(Minecraft mc) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null || mc.getConnection() == null) {
         return false;
      }

      if (this.init_E_DataUtil != InteractionTargetRef.Inner_DataUtil.BLOCK) {
         Entity entity = this.load_Entity(mc);
         if (entity == null) {
            return false;
         } else if (this.init_E_DataUtil == InteractionTargetRef.Inner_DataUtil.ENTITY_INTERACT_AT && this.init_Vec3 != null) {
            mc.getConnection().send(new ServerboundInteractPacket(entity.getId(), this.init_InteractionHand, this.init_Vec3, mc.player.isShiftKeyDown()));
            return true;
         } else {
            mc.getConnection().send(new ServerboundInteractPacket(entity.getId(), this.init_InteractionHand, Vec3.ZERO, mc.player.isShiftKeyDown()));
            return true;
         }
      } else {
         if (this.init_BlockPos == null) {
            return false;
         }

         if (mc.player != null && mc.gameMode != null) {
            BlockHitResult hitResult = this.init_BlockHitResult_Minecraft(mc);
            if (hitResult == null) {
               return false;
            }

            mc.gameMode.useItemOn(mc.player, this.init_InteractionHand, hitResult);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean load_Z(Minecraft mc) {
      if (mc == null || mc.player == null) {
         return false;
      } else if (this.init_E_DataUtil != InteractionTargetRef.Inner_DataUtil.BLOCK) {
         Entity entity = this.load_Entity(mc);
         return init(mc, entity);
      } else {
         return this.init_BlockPos != null && init_Z(mc, this.init_BlockPos) && this.init_BlockHitResult_Minecraft(mc) != null;
      }
   }

   public Entity init_Entity_Minecraft(Minecraft mc) {
      return this.init_E_DataUtil == InteractionTargetRef.Inner_DataUtil.BLOCK ? null : this.load_Entity(mc);
   }

   private BlockHitResult init_BlockHitResult_Minecraft(Minecraft mc) {
      return this.init_Direction != null && this.init_Vec3 != null
         ? new BlockHitResult(this.init_Vec3, this.init_Direction, this.init_BlockPos, this.init_Z)
         : init_BlockHitResult(mc, this.init_BlockPos);
   }

   public static BlockHitResult init_BlockHitResult(Minecraft mc, BlockPos blockPos) {
      if (blockPos == null) {
         return null;
      } else {
         Vec3 center = Vec3.atCenterOf(blockPos);
         if (mc != null && mc.player != null) {
            Vec3 eyePos = mc.player.getEyePosition();
            Vec3 delta = center.subtract(eyePos);
            Direction side = Direction.getApproximateNearest(delta.x, delta.y, delta.z).getOpposite();
            return init(blockPos, side);
         } else {
            return new BlockHitResult(center, Direction.UP, blockPos, false);
         }
      }
   }

   public static BlockHitResult init(BlockPos blockPos, Direction face) {
      if (blockPos != null && face != null) {
         Vec3 center = Vec3.atCenterOf(blockPos);
         Vec3 faceCenter = center.add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
         return new BlockHitResult(faceCenter, face, blockPos, false);
      } else {
         return null;
      }
   }

   public static boolean init_Z(Minecraft mc, BlockPos blockPos) {
      if (mc != null && mc.player != null && blockPos != null) {
         double reach = Math.max(4.5, mc.player.blockInteractionRange());
         return mc.player.distanceToSqr(Vec3.atCenterOf(blockPos)) <= reach * reach;
      } else {
         return false;
      }
   }

   public static boolean init(Minecraft mc, Entity entity) {
      if (mc != null && mc.player != null && entity != null && entity != mc.player) {
         double reach = Math.max(4.5, mc.player.blockInteractionRange());
         return entity.distanceToSqr(mc.player) <= reach * reach;
      } else {
         return false;
      }
   }

   private Entity load_Entity(Minecraft mc) {
      if (mc != null && mc.level != null) {
         String uuid = "";
         String typeId = this.init_Str;
         if (this.init_Str.startsWith("~")) {
            String[] parts = this.init_Str.split("~", 4);
            uuid = parts.length >= 2 ? parts[1] : "";
            typeId = parts.length >= 3 ? parts[2] : "";
         }

         Entity nearestTypeMatch = null;
         double nearestDistance = 1.7976931348623157E308;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != null) {
               if (!uuid.isEmpty() && uuid.equals(entity.getStringUUID())) {
                  return entity;
               }

               if (!typeId.isBlank()) {
                  String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                  if (typeId.equalsIgnoreCase(entityTypeId)) {
                     double distance = mc.player == null ? 0.0 : entity.distanceToSqr(mc.player);
                     if (!(distance >= nearestDistance)) {
                        nearestDistance = distance;
                        nearestTypeMatch = entity;
                     }
                  }
               }
            }
         }

         return nearestTypeMatch;
      } else {
         return null;
      }
   }

   public enum Inner_DataUtil {
      BLOCK,
      ENTITY_INTERACT,
      ENTITY_INTERACT_AT;
   }
}
