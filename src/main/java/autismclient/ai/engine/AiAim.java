package autismclient.ai.engine;

import autismclient.util.AutismLiteVariant;
import autismclient.util.SilentRotationManager;
import autismclient.util.RotationStreamPlanner;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.SilentMovementCoordinator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AiAim {
   private static final double SETTLED_TOLERANCE_DEG = 1.0;
   private static final double ENDERMAN_STARE_MAX_DIST = 64.0;
   static final float ENDERMAN_YAW_NUDGE_DEG = 7.0F;
   private RotationMath.Rotation targetRotation;
   private static volatile String lastDebugState = "";
   private Vec3 targetPoint;
   private int allowedEndermanEntityId = -1;
   private AiAim.AimProfile aimProfile = AiAim.AimProfile.NORMAL;
   private int settledTicks;
   private boolean silent;
   static final double STEEP_VERTICAL_MIN_DELTA = 0.4;
   static final float MIN_STEP_DEG = 3.0F;
   static final float STEP_FRACTION = 0.5F;
   private int lastInstanceStepTick = -2147483648;
   private static int lastGlobalStepTick = -2147483648;

   public static String lastDebugState() {
      return lastDebugState;
   }

   public AiAim allowEnderman(int entityId) {
      this.allowedEndermanEntityId = entityId;
      return this;
   }

   public void setProfile(AiAim.AimProfile value) {
      if (value != null) {
         this.aimProfile = value;
      }
   }

   public AiAim.AimProfile profile() {
      return this.aimProfile;
   }

   public void aimAt(Vec3 point) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && point != null) {
         Vec3 eye = player.getEyePosition();
         RotationMath.Rotation rotation = RotationMath.lookAt(point, eye);
         if (isSteepVerticalDelta(point.subtract(eye))) {
            RotationMath.Rotation kept = new RotationMath.Rotation(this.targetRotation != null ? this.targetRotation.yaw() : player.getYRot(), rotation.pitch());
            if (keptYawHitsSameFace(player, eye, kept, point)) {
               rotation = kept;
            }
         }

         this.setTargetRotation(rotation);
         this.targetPoint = point;
      }
   }

   private static boolean keptYawHitsSameFace(LocalPlayer player, Vec3 eye, RotationMath.Rotation kept, Vec3 point) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         return true;
      }

      Vec3 wanted = point.subtract(eye);
      if (wanted.lengthSqr() < 1.0E-10) {
         return true;
      }

      BlockHitResult intended = level.clip(new ClipContext(eye, point.add(wanted.normalize().scale(0.02)), Block.OUTLINE, Fluid.NONE, player));
      if (intended.getType() != Type.BLOCK) {
         return true;
      }

      Vec3 end = eye.add(Vec3.directionFromRotation(kept.pitch(), kept.yaw()).scale(point.distanceTo(eye) + 0.02));
      BlockHitResult hit = level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
      return AiBlockInteraction.isValidTargetHit(eye, intended.getBlockPos(), hit, player.blockInteractionRange()) && hit.getDirection() == intended.getDirection();
   }

   static boolean isSteepVerticalDelta(Vec3 delta) {
      double horizontal = Math.hypot(delta.x, delta.z);
      double vertical = Math.abs(delta.y);
      return vertical > 0.4 && (horizontal < 0.4 || horizontal < vertical / 2.75);
   }

   public void setTargetRotation(RotationMath.Rotation rotation) {
      if (rotation != null) {
         this.targetPoint = null;
         if (this.targetRotation == null || RotationMath.angularDistance(this.targetRotation, rotation) > 1.0E-4) {
            this.settledTicks = 0;
         }

         this.targetRotation = rotation;
      }
   }

   public RotationMath.Rotation targetRotation() {
      return this.targetRotation;
   }

   public void setSilent(boolean value) {
      this.silent = value;
   }

   public boolean isSilent() {
      return this.silent;
   }

   public void clear() {
      this.targetRotation = null;
      this.settledTicks = 0;
      if (this.silent) {
         AiSilentAim.release();
      }
   }

   static float stepAngle(float remaining, float cap) {
      float abs = Math.abs(remaining);
      return !(abs <= 3.0F) && !(cap >= 180.0F) ? Math.copySign(Math.min(cap, Math.max(3.0F, abs * 0.5F)), remaining) : remaining;
   }

   static float stepAngleForProfile(float remaining, AiAim.AimProfile speed) {
      if (speed.linear) {
         return Math.max(-speed.cameraCap, Math.min(speed.cameraCap, remaining));
      } else if (speed == AiAim.AimProfile.FLOW) {
         float abs = Math.abs(remaining);
         return abs <= 12.0F ? remaining : Math.copySign(Math.min(speed.cameraCap, Math.max(12.0F, abs * 0.65F)), remaining);
      } else {
         return stepAngle(remaining, speed.cameraCap);
      }
   }

   public float predictedNextYaw() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return 0.0F;
      } else if (this.targetRotation != null && !this.silent) {
         return this.lastInstanceStepTick != player.tickCount && lastGlobalStepTick != player.tickCount
            ? player.getYRot() + stepAngleForProfile(Mth.wrapDegrees(this.targetRotation.yaw() - player.getYRot()), this.aimProfile)
            : player.getYRot();
      } else {
         return player.getYRot();
      }
   }

   public void tick() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && this.targetRotation != null && (this.silent || this.lastInstanceStepTick != player.tickCount && lastGlobalStepTick != player.tickCount)) {
         if (!this.silent) {
            RotationMath.Rotation target = this.avoidEndermanEye(player, this.targetRotation);
            lastDebugState = String.format(
               Locale.ROOT,
               "aim@%x speed %s goal %.0f/%.0f%s%s",
               System.identityHashCode(this),
               this.aimProfile,
               this.targetRotation.yaw(),
               this.targetRotation.pitch(),
               this.targetPoint == null ? " (rotation)" : " (point)",
               target == this.targetRotation ? "" : String.format(Locale.ROOT, " offset to %.0f/%.0f", target.yaw(), target.pitch())
            );
            float prevYaw = player.getYRot();
            float prevPitch = player.getXRot();
            float yaw = prevYaw + stepAngleForProfile(Mth.wrapDegrees(target.yaw() - prevYaw), this.aimProfile);
            float pitch = prevPitch + stepAngleForProfile(target.pitch() - prevPitch, this.aimProfile);
            RotationMath.Rotation stepped = new RotationMath.Rotation(yaw, pitch);
            if (Minecraft.getInstance().level != null) {
               stepped = this.nudgeAwayFromEndermen(Minecraft.getInstance(), stepped);
            }

            RotationMath.applyToPlayer(player, stepped, false);
            AiCameraTurn.putAt(player, prevYaw, prevPitch);
            this.lastInstanceStepTick = player.tickCount;
            lastGlobalStepTick = player.tickCount;
            this.settledTicks = this.angularError() <= 1.0 ? this.settledTicks + 1 : 0;
         } else {
            AiSilentAim.init(true);
            SilentRotationManager.requestRotation("ai-assistant", 1, this.targetRotation);
            SilentRotationManager.tick(
               "ai-assistant", player, this.aimProfile.yawCap, this.aimProfile.pitchCap, this.aimProfile.streamProfile
            );
            RotationMath.Rotation packetRotation = SilentMovementCoordinator.activeRotation();
            this.settledTicks = packetRotation != null && RotationMath.angularDistance(packetRotation, this.targetRotation) <= 1.0 ? this.settledTicks + 1 : 0;
         }
      }
   }

   private RotationMath.Rotation avoidEndermanEye(LocalPlayer player, RotationMath.Rotation rotation) {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null && this.targetRotation != null ? this.nudgeAwayFromEndermen(mc, rotation) : rotation;
   }

   private RotationMath.Rotation nudgeAwayFromEndermen(Minecraft mc, RotationMath.Rotation rotation) {
      if (!this.wouldStareAtEnderman(mc, rotation)) {
         return rotation;
      }

      for (float dip : new float[]{8.0F, 14.0F, 20.0F}) {
         RotationMath.Rotation candidate = new RotationMath.Rotation(rotation.yaw(), Math.min(90.0F, rotation.pitch() + dip));
         if (!this.wouldStareAtEnderman(mc, candidate)) {
            return candidate;
         }
      }

      for (float side : new float[]{7.0F, -7.0F, 14.0F, -14.0F}) {
         RotationMath.Rotation candidate = new RotationMath.Rotation(rotation.yaw() + side, rotation.pitch());
         if (!this.wouldStareAtEnderman(mc, candidate)) {
            return candidate;
         }
      }

      return new RotationMath.Rotation(rotation.yaw() + 7.0F, rotation.pitch());
   }

   private boolean wouldStareAtEnderman(Minecraft mc, RotationMath.Rotation rotation) {
      Vec3 view = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).normalize();
      Vec3 eye = mc.player.getEyePosition();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Enderman enderman
            && enderman.isAlive()
            && (this.targetPoint == null || !enderman.isCreepy() || !enderman.getBoundingBox().inflate(0.6).contains(this.targetPoint))
            && enderman.getId() != this.allowedEndermanEntityId
            && isWithinEndermanStareCone(view, enderman.getX() - mc.player.getX(), enderman.getEyeY() - eye.y, enderman.getZ() - mc.player.getZ())
            && mc.player.hasLineOfSight(enderman, Block.VISUAL, Fluid.NONE, enderman.getEyeY())) {
            return true;
         }
      }

      return false;
   }

   private static Entity findStaredEnderman(Minecraft mc, RotationMath.Rotation rotation) {
      Vec3 view = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).normalize();
      Vec3 eye = mc.player.getEyePosition();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Enderman enderman
            && enderman.isAlive()
            && isWithinEndermanStareCone(view, enderman.getX() - mc.player.getX(), enderman.getEyeY() - eye.y, enderman.getZ() - mc.player.getZ())
            && mc.player.hasLineOfSight(enderman, Block.VISUAL, Fluid.NONE, enderman.getEyeY())) {
            return enderman;
         }
      }

      return null;
   }

   static boolean isWithinEndermanStareCone(Vec3 view, double dx, double dy, double dz) {
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return !(distance < 1.0E-6) && !(distance > 64.0)
         ? view.normalize().dot(new Vec3(dx / distance, dy / distance, dz / distance)) > 1.0 - 0.025 / distance
         : false;
   }

   public boolean isSettled() {
      return this.targetRotation != null && this.settledTicks > 0;
   }

   public double angularError() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null && this.targetRotation != null ? RotationMath.angularDistance(RotationMath.ofPlayer(player), this.targetRotation) : 0.0;
   }

   public static BlockHitResult raycastLookBlock() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         double reach = mc.player.blockInteractionRange();
         Vec3 eye = mc.player.getEyePosition();
         Vec3 end = eye.add(effectiveLookVector().scale(reach));
         BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
         return hit != null && hit.getType() == Type.BLOCK ? hit : null;
      } else {
         return null;
      }
   }

   public static boolean isLookingAtBlock(BlockPos cell) {
      BlockHitResult hit = raycastLookBlock();
      return hit != null && hit.getBlockPos().equals(cell);
   }

   public static Direction lookedBlockFace(BlockPos cell) {
      BlockHitResult hit = raycastLookBlock();
      return hit != null && hit.getBlockPos().equals(cell) ? hit.getDirection() : null;
   }

   public static boolean isLookingAtEntity(Entity target) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && target != null) {
         double reach = mc.player.getAttackRangeWith(mc.player.getMainHandItem()).effectiveMaxRange(mc.player);
         Vec3 eye = mc.player.getEyePosition();
         Vec3 end = eye.add(effectiveLookVector().scale(reach));
         AABB box = target.getBoundingBox().inflate(target.getPickRadius());
         Optional<Vec3> contact = box.clip(eye, end);
         return contact.isPresent() && isUnobstructedHit(mc, target, eye, contact.get());
      } else {
         return false;
      }
   }

   public boolean isAimedAtEntity(Entity target) {
      if (!AutismLiteVariant.enabled() && autismclient.ai.AiClientTick.isOnTickThread()) {
         return isLookingAtEntity(target);
      } else if (!this.silent) {
         return isLookingAtEntity(target);
      } else {
         RotationMath.Rotation rotation = SilentMovementCoordinator.activeRotation();
         if (rotation == null) {
            return false;
         } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null && target != null) {
               double reach = mc.player.getAttackRangeWith(mc.player.getMainHandItem()).effectiveMaxRange(mc.player);
               Vec3 eye = mc.player.getEyePosition();
               Vec3 end = eye.add(Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).scale(reach));
               AABB box = target.getBoundingBox().inflate(target.getPickRadius());
               Optional<Vec3> contact = box.clip(eye, end);
               return contact.isPresent() && isUnobstructedHit(mc, target, eye, contact.get());
            } else {
               return false;
            }
         }
      }
   }

   public static Vec3 effectiveLookVector() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return Vec3.ZERO;
      } else if (!AutismLiteVariant.enabled() && autismclient.ai.AiClientTick.isOnTickThread()) {
         ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
         return !wire.initialized() ? Vec3.ZERO : Vec3.directionFromRotation(wire.currentPitch(), wire.currentYaw());
      } else {
         return player.getLookAngle();
      }
   }

   private static boolean isUnobstructedHit(Minecraft mc, Entity target, Vec3 eye, Vec3 contact) {
      BlockHitResult block = mc.level.clip(new ClipContext(eye, contact, Block.COLLIDER, Fluid.NONE, mc.player));
      if (block != null && block.getType() == Type.BLOCK) {
         return false;
      }

      EntityHitResult interceptor = ProjectileUtil.getEntityHitResult(
         mc.player,
         eye,
         contact,
         mc.player.getBoundingBox().expandTowards(contact.subtract(eye)).inflate(1.0),
         entity -> entity != target && !entity.isSpectator() && entity.isPickable() && !(target instanceof EnderDragonPart part && entity == part.parentMob),
         eye.distanceToSqr(contact)
      );
      return interceptor == null;
   }

   public static Vec3 aimPointForBlockFace(BlockPos cell, Direction face) {
      Minecraft mc = Minecraft.getInstance();
      Vec3 centre = Vec3.atCenterOf(cell);
      if (mc.player != null && mc.level != null && face != null) {
         Vec3 faceCentre = centre.add(new Vec3(face.getStepX(), face.getStepY(), face.getStepZ()).scale(0.5));
         Vec3 eye = mc.player.getEyePosition();
         BlockHitResult hit = mc.level.clip(new ClipContext(eye, faceCentre, Block.COLLIDER, Fluid.NONE, mc.player));
         return hit != null && hit.getType() == Type.BLOCK && !hit.getBlockPos().equals(cell) ? centre : faceCentre;
      } else {
         return centre;
      }
   }

   public static Vec3 defaultEntityAimPoint(Entity target) {
      if (target == null) {
         return Vec3.ZERO;
      }

      AABB box = target.getBoundingBox();
      double eyeY = target.getEyeHeight() > 0.0F ? target.getY() + target.getEyeHeight() * 0.85 : box.getCenter().y;
      return new Vec3(box.getCenter().x, Mth.clamp(eyeY, box.minY + 0.1, box.maxY - 0.1), box.getCenter().z);
   }

   public static Vec3 bestEntityAimPoint(Entity target) {
      Minecraft mc = Minecraft.getInstance();
      return target != null && mc.player != null && mc.level != null ? bestEntityAimPointFrom(target, mc.player.getEyePosition()) : defaultEntityAimPoint(target);
   }

   public static Vec3 bestEntityAimPointFrom(Entity target, Vec3 eye) {
      Minecraft mc = Minecraft.getInstance();
      if (target != null && mc.player != null && mc.level != null) {
         BiPredicate<Vec3, Vec3> clear = (from, to) -> {
            BlockHitResult hit = mc.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.player));
            return hit == null || hit.getType() != Type.BLOCK;
         };
         return pickVisibleHitboxPoint(
            target.getBoundingBox(),
            eye,
            mc.player.getYRot(),
            mc.player.getXRot(),
            Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)),
            clear
         );
      } else {
         return defaultEntityAimPoint(target);
      }
   }

   public static boolean canHitEntityAt(Entity target, Vec3 point) {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.level != null && target != null && point != null ? canHitEntityFrom(target, mc.player.getEyePosition(), point) : false;
   }

   public static boolean canHitEntityFrom(Entity target, Vec3 eye, Vec3 point) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && target != null && point != null) {
         double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
         Optional<Vec3> contact = target.getBoundingBox().clip(eye, eye.add(point.subtract(eye).normalize().scale(reach)));
         return contact.isEmpty() ? false : isUnobstructedHit(mc, target, eye, contact.get());
      } else {
         return false;
      }
   }

   public static Vec3 pickVisibleHitboxPoint(AABB hitbox, Vec3 eye, float yaw, float pitch, double reach) {
      return pickVisibleHitboxPoint(hitbox, eye, yaw, pitch, reach, (from, to) -> true);
   }

   public static Vec3 pickVisibleHitboxPoint(AABB hitbox, Vec3 eye, float yaw, float pitch, double reach, BiPredicate<Vec3, Vec3> clear) {
      double inset = Math.min(0.1, Math.min(hitbox.getXsize(), hitbox.getYsize()) * 0.25);
      AABB box = hitbox.deflate(inset);
      Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
      Optional<Vec3> hit = box.clip(eye, eye.add(dir.scale(reach)));
      if (hit.isPresent() && clear.test(eye, hit.get())) {
         return eye.add(dir.scale(hit.get().distanceTo(eye) + 0.05));
      }

      List<Vec3> points = new ArrayList<>(125);
      List<Double> dots = new ArrayList<>(125);

      for (int i = 0; i <= 4; i++) {
         for (int j = 0; j <= 4; j++) {
            for (int k = 0; k <= 4; k++) {
               Vec3 q = new Vec3(box.minX + box.getXsize() * i / 4.0, box.minY + box.getYsize() * j / 4.0, box.minZ + box.getZsize() * k / 4.0);
               Vec3 d = q.subtract(eye);
               double length = d.length();
               if (!(length < 1.0E-6)) {
                  points.add(q);
                  dots.add(d.dot(dir) / length);
               }
            }
         }
      }

      if (points.isEmpty()) {
         return box.getCenter();
      }

      Integer[] order = new Integer[points.size()];

      for (int i = 0; i < order.length; i++) {
         order[i] = i;
      }

      Arrays.sort(order, (a, b) -> Double.compare(dots.get(b), dots.get(a)));
      Integer[] var23 = order;
      int var25 = var23.length;

      for (int var27 = 0; var27 < var25; var27++) {
         int i = var23[var27];
         Vec3 point = points.get(i);
         Optional<Vec3> contact = hitbox.clip(eye, eye.add(point.subtract(eye).normalize().scale(reach)));
         if (contact.isPresent() && clear.test(eye, contact.get())) {
            return point;
         }
      }

      Vec3 nearest = new Vec3(
         Mth.clamp(eye.x, hitbox.minX + 1.0E-4, hitbox.maxX - 1.0E-4),
         Mth.clamp(eye.y, hitbox.minY + 1.0E-4, hitbox.maxY - 1.0E-4),
         Mth.clamp(eye.z, hitbox.minZ + 1.0E-4, hitbox.maxZ - 1.0E-4)
      );
      Optional<Vec3> nearestContact = hitbox.clip(eye, eye.add(nearest.subtract(eye).normalize().scale(reach)));
      if (nearestContact.isPresent() && clear.test(eye, nearestContact.get())) {
         return nearest;
      }

      Integer[] var28 = order;
      int var30 = var28.length;

      for (int var32 = 0; var32 < var30; var32++) {
         int i = var28[var32];
         if (clear.test(eye, points.get(i))) {
            return points.get(i);
         }
      }

      return points.get(order[0]);
   }

   public static float yawCapOf(AiAim.AimProfile speed) {
      return speed.yawCap;
   }

   public enum AimProfile {
      CALM(28.0F, 28.0F, 22.0F, RotationStreamPlanner.MotionProfile.STANDARD),
      FLOW(54.0F, 54.0F, 38.0F, RotationStreamPlanner.MotionProfile.STANDARD),
      NORMAL(45.0F, 45.0F, 180.0F, RotationStreamPlanner.MotionProfile.STANDARD),
      FAST(72.0F, 72.0F, 180.0F, RotationStreamPlanner.MotionProfile.STANDARD),
      FLICK(72.0F, 72.0F, 180.0F, RotationStreamPlanner.MotionProfile.TELLY_FLICK),
      EXACT(72.0F, 72.0F, 180.0F, RotationStreamPlanner.MotionProfile.STANDARD, true),
      TELLY(72.0F, 72.0F, 100.0F, RotationStreamPlanner.MotionProfile.TELLY_FLICK, true);

      final float yawCap;
      final float pitchCap;
      final float cameraCap;
      final RotationStreamPlanner.MotionProfile streamProfile;
      final boolean linear;

      AimProfile(float yawCap, float pitchCap, float cameraCap, RotationStreamPlanner.MotionProfile profile) {
         this(yawCap, pitchCap, cameraCap, profile, false);
      }

      AimProfile(float yawCap, float pitchCap, float cameraCap, RotationStreamPlanner.MotionProfile profile, boolean linear) {
         this.yawCap = yawCap;
         this.pitchCap = pitchCap;
         this.cameraCap = cameraCap;
         this.streamProfile = profile;
         this.linear = linear;
      }
   }
}
