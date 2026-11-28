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
   private final InteractionTargetRef.TargetKind kind;
   private final BlockPos blockPos;
   private final String entityRef;
   private final InteractionHand hand;
   private final Vec3 hitPos;
   private final Direction blockSide;
   private final boolean insideBlock;

   private InteractionTargetRef(InteractionTargetRef.TargetKind kind, BlockPos blockPos, String entityRef, InteractionHand hand, Vec3 hitPos, Direction blockSide, boolean insideBlock) {
      this.kind = kind;
      this.blockPos = blockPos == null ? null : blockPos.immutable();
      this.entityRef = entityRef == null ? "" : entityRef;
      this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
      this.hitPos = hitPos;
      this.blockSide = blockSide;
      this.insideBlock = insideBlock;
   }

   public static InteractionTargetRef ofBlock(BlockPos pos) {
      return pos == null ? null : new InteractionTargetRef(InteractionTargetRef.TargetKind.BLOCK, pos, "", InteractionHand.MAIN_HAND, null, null, false);
   }

   public static InteractionTargetRef ofBlock(BlockHitResult hitResult, InteractionHand hand) {
      return hitResult != null && hitResult.getBlockPos() != null
         ? new InteractionTargetRef(InteractionTargetRef.TargetKind.BLOCK, hitResult.getBlockPos(), "", hand, hitResult.getLocation(), hitResult.getDirection(), hitResult.isInside())
         : null;
   }

   public static InteractionTargetRef ofEntity(Entity entity, InteractionHand hand) {
      return entity == null ? null : new InteractionTargetRef(InteractionTargetRef.TargetKind.ENTITY_INTERACT, null, refFor(entity), hand, null, null, false);
   }

   public static InteractionTargetRef ofEntity(Entity entity, InteractionHand hand, Vec3 hitPos) {
      return entity == null ? null : new InteractionTargetRef(InteractionTargetRef.TargetKind.ENTITY_INTERACT_AT, null, refFor(entity), hand, hitPos, null, false);
   }

   public static InteractionTargetRef ofEntityRef(String entityRef) {
      return entityRef != null && !entityRef.isBlank()
         ? new InteractionTargetRef(InteractionTargetRef.TargetKind.ENTITY_INTERACT, null, entityRef.trim(), InteractionHand.MAIN_HAND, null, null, false)
         : null;
   }

   public static String refFor(Entity entity) {
      if (entity == null) {
         return "";
      }

      String uuid = entity.getStringUUID();
      String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
      String displayName = entity.getDisplayName().getString().replaceAll("§.", "").trim();
      return "~" + uuid + "~" + typeId + "~" + displayName;
   }

   public InteractionTargetRef.TargetKind kind() {
      return this.kind;
   }

   public BlockPos blockPos() {
      return this.blockPos;
   }

   public String entityRef() {
      return this.entityRef;
   }

   public boolean isBlock() {
      return this.kind == InteractionTargetRef.TargetKind.BLOCK;
   }

   public boolean interact(Minecraft mc) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null || mc.getConnection() == null) {
         return false;
      }

      if (this.kind != InteractionTargetRef.TargetKind.BLOCK) {
         Entity entity = this.lookupEntity(mc);
         if (entity == null) {
            return false;
         } else if (this.kind == InteractionTargetRef.TargetKind.ENTITY_INTERACT_AT && this.hitPos != null) {
            mc.getConnection().send(new ServerboundInteractPacket(entity.getId(), this.hand, this.hitPos, mc.player.isShiftKeyDown()));
            return true;
         } else {
            mc.getConnection().send(new ServerboundInteractPacket(entity.getId(), this.hand, Vec3.ZERO, mc.player.isShiftKeyDown()));
            return true;
         }
      } else {
         if (this.blockPos == null) {
            return false;
         }

         if (mc.player != null && mc.gameMode != null) {
            BlockHitResult hitResult = this.blockHit(mc);
            if (hitResult == null) {
               return false;
            }

            mc.gameMode.useItemOn(mc.player, this.hand, hitResult);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean isPresent(Minecraft mc) {
      if (mc == null || mc.player == null) {
         return false;
      } else if (this.kind != InteractionTargetRef.TargetKind.BLOCK) {
         Entity entity = this.lookupEntity(mc);
         return init(mc, entity);
      } else {
         return this.blockPos != null && isInReach(mc, this.blockPos) && this.blockHit(mc) != null;
      }
   }

   public Entity resolveEntity(Minecraft mc) {
      return this.kind == InteractionTargetRef.TargetKind.BLOCK ? null : this.lookupEntity(mc);
   }

   private BlockHitResult blockHit(Minecraft mc) {
      return this.blockSide != null && this.hitPos != null
         ? new BlockHitResult(this.hitPos, this.blockSide, this.blockPos, this.insideBlock)
         : syntheticBlockHit(mc, this.blockPos);
   }

   public static BlockHitResult syntheticBlockHit(Minecraft mc, BlockPos blockPos) {
      if (blockPos == null) {
         return null;
      } else {
         Vec3 center = Vec3.atCenterOf(blockPos);
         if (mc != null && mc.player != null) {
            Vec3 eyePos = mc.player.getEyePosition();
            Vec3 delta = center.subtract(eyePos);
            Direction side = Direction.getApproximateNearest(delta.x, delta.y, delta.z).getOpposite();
            return hitResult(blockPos, side);
         } else {
            return new BlockHitResult(center, Direction.UP, blockPos, false);
         }
      }
   }

   public static BlockHitResult hitResult(BlockPos blockPos, Direction face) {
      if (blockPos != null && face != null) {
         Vec3 center = Vec3.atCenterOf(blockPos);
         Vec3 faceCenter = center.add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
         return new BlockHitResult(faceCenter, face, blockPos, false);
      } else {
         return null;
      }
   }

   public static boolean isInReach(Minecraft mc, BlockPos blockPos) {
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

   private Entity lookupEntity(Minecraft mc) {
      if (mc != null && mc.level != null) {
         String uuid = "";
         String typeId = this.entityRef;
         if (this.entityRef.startsWith("~")) {
            String[] parts = this.entityRef.split("~", 4);
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

   public enum TargetKind {
      BLOCK,
      ENTITY_INTERACT,
      ENTITY_INTERACT_AT;
   }
}
