package autismclient.ai.a;

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
   private static final double load_D = 1.0;
   private static final double read_D = 64.0;
   static final float init_F = 7.0F;
   private RotationMath.Inner_DataUtil init_ce_DataUtil;
   private static volatile String init_Str = "";
   private Vec3 init_Vec3;
   private int init_I = -1;
   private AiAim.Inner_DataUtil init_DataUtil_DataUtil = AiAim.Inner_DataUtil.read_DataUtil_DataUtil;
   private int load_I;
   private boolean init_Z;
   static final double init_D = 0.4;
   static final float load_F = 3.0F;
   static final float read_F = 0.5F;
   private int read_I = -2147483648;
   private static int write = -2147483648;

   public static String init_Str() {
      return init_Str;
   }

   public AiAim init_DataUtil_I(int entityId) {
      this.init_I = entityId;
      return this;
   }

   public void init_V_DataUtil_DataUtil(AiAim.Inner_DataUtil value) {
      if (value != null) {
         this.init_DataUtil_DataUtil = value;
      }
   }

   public AiAim.Inner_DataUtil init_DataUtil_DataUtil() {
      return this.init_DataUtil_DataUtil;
   }

   public void init_V_Vec3(Vec3 point) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && point != null) {
         Vec3 eye = player.getEyePosition();
         RotationMath.Inner_DataUtil rotation = RotationMath.init(point, eye);
         if (init_Z_Vec3(point.subtract(eye))) {
            RotationMath.Inner_DataUtil kept = new RotationMath.Inner_DataUtil(this.init_ce_DataUtil != null ? this.init_ce_DataUtil.init() : player.getYRot(), rotation.load());
            if (init(player, eye, kept, point)) {
               rotation = kept;
            }
         }

         this.init_V_ce_DataUtil(rotation);
         this.init_Vec3 = point;
      }
   }

   private static boolean init(LocalPlayer player, Vec3 eye, RotationMath.Inner_DataUtil kept, Vec3 point) {
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

      Vec3 end = eye.add(Vec3.directionFromRotation(kept.load(), kept.init()).scale(point.distanceTo(eye) + 0.02));
      BlockHitResult hit = level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
      return AiBlockInteraction.init(eye, intended.getBlockPos(), hit, player.blockInteractionRange()) && hit.getDirection() == intended.getDirection();
   }

   static boolean init_Z_Vec3(Vec3 delta) {
      double horizontal = Math.hypot(delta.x, delta.z);
      double vertical = Math.abs(delta.y);
      return vertical > 0.4 && (horizontal < 0.4 || horizontal < vertical / 2.75);
   }

   public void init_V_ce_DataUtil(RotationMath.Inner_DataUtil rotation) {
      if (rotation != null) {
         this.init_Vec3 = null;
         if (this.init_ce_DataUtil == null || RotationMath.init_F(this.init_ce_DataUtil, rotation) > 1.0E-4) {
            this.load_I = 0;
         }

         this.init_ce_DataUtil = rotation;
      }
   }

   public RotationMath.Inner_DataUtil init_ce_DataUtil() {
      return this.init_ce_DataUtil;
   }

   public void init_V_Z(boolean value) {
      this.init_Z = value;
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public void init_V() {
      this.init_ce_DataUtil = null;
      this.load_I = 0;
      if (this.init_Z) {
         AiSilentAim.init_V();
      }
   }

   static float init_F_F_F(float remaining, float cap) {
      float abs = Math.abs(remaining);
      return !(abs <= 3.0F) && !(cap >= 180.0F) ? Math.copySign(Math.min(cap, Math.max(3.0F, abs * 0.5F)), remaining) : remaining;
   }

   static float init_F_F_DataUtil_DataUtil(float remaining, AiAim.Inner_DataUtil speed) {
      if (speed.init_Z) {
         return Math.max(-speed.read_F, Math.min(speed.read_F, remaining));
      } else if (speed == AiAim.Inner_DataUtil.load_DataUtil_DataUtil) {
         float abs = Math.abs(remaining);
         return abs <= 12.0F ? remaining : Math.copySign(Math.min(speed.read_F, Math.max(12.0F, abs * 0.65F)), remaining);
      } else {
         return init_F_F_F(remaining, speed.read_F);
      }
   }

   public float init_F() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return 0.0F;
      } else if (this.init_ce_DataUtil != null && !this.init_Z) {
         return this.read_I != player.tickCount && write != player.tickCount
            ? player.getYRot() + init_F_F_DataUtil_DataUtil(Mth.wrapDegrees(this.init_ce_DataUtil.init() - player.getYRot()), this.init_DataUtil_DataUtil)
            : player.getYRot();
      } else {
         return player.getYRot();
      }
   }

   public void load_V() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && this.init_ce_DataUtil != null && (this.init_Z || this.read_I != player.tickCount && write != player.tickCount)) {
         if (!this.init_Z) {
            RotationMath.Inner_DataUtil target = this.init_ce_DataUtil_LocalPlayer_ce_DataUtil(player, this.init_ce_DataUtil);
            init_Str = String.format(
               Locale.ROOT,
               "aim@%x speed %s goal %.0f/%.0f%s%s",
               System.identityHashCode(this),
               this.init_DataUtil_DataUtil,
               this.init_ce_DataUtil.init(),
               this.init_ce_DataUtil.load(),
               this.init_Vec3 == null ? " (rotation)" : " (point)",
               target == this.init_ce_DataUtil ? "" : String.format(Locale.ROOT, " offset to %.0f/%.0f", target.init(), target.load())
            );
            float prevYaw = player.getYRot();
            float prevPitch = player.getXRot();
            float yaw = prevYaw + init_F_F_DataUtil_DataUtil(Mth.wrapDegrees(target.init() - prevYaw), this.init_DataUtil_DataUtil);
            float pitch = prevPitch + init_F_F_DataUtil_DataUtil(target.load() - prevPitch, this.init_DataUtil_DataUtil);
            RotationMath.Inner_DataUtil stepped = new RotationMath.Inner_DataUtil(yaw, pitch);
            if (Minecraft.getInstance().level != null) {
               stepped = this.init_ce_DataUtil_Minecraft_ce_DataUtil(Minecraft.getInstance(), stepped);
            }

            RotationMath.init_V_LocalPlayer(player, stepped, false);
            AiCameraTurn.putAt(player, prevYaw, prevPitch);
            this.read_I = player.tickCount;
            write = player.tickCount;
            this.load_I = this.init_D() <= 1.0 ? this.load_I + 1 : 0;
         } else {
            AiSilentAim.init(true);
            SilentRotationManager.init_V_Str("ai-assistant", 1, this.init_ce_DataUtil);
            SilentRotationManager.init(
               "ai-assistant", player, this.init_DataUtil_DataUtil.init_F, this.init_DataUtil_DataUtil.load_F, this.init_DataUtil_DataUtil.init_ap_StreamUtil
            );
            RotationMath.Inner_DataUtil packetRotation = SilentMovementCoordinator.init_ce_DataUtil();
            this.load_I = packetRotation != null && RotationMath.init_F(packetRotation, this.init_ce_DataUtil) <= 1.0 ? this.load_I + 1 : 0;
         }
      }
   }

   private RotationMath.Inner_DataUtil init_ce_DataUtil_LocalPlayer_ce_DataUtil(LocalPlayer player, RotationMath.Inner_DataUtil rotation) {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null && this.init_ce_DataUtil != null ? this.init_ce_DataUtil_Minecraft_ce_DataUtil(mc, rotation) : rotation;
   }

   private RotationMath.Inner_DataUtil init_ce_DataUtil_Minecraft_ce_DataUtil(Minecraft mc, RotationMath.Inner_DataUtil rotation) {
      if (!this.init_Z_Minecraft_ce_DataUtil(mc, rotation)) {
         return rotation;
      }

      for (float dip : new float[]{8.0F, 14.0F, 20.0F}) {
         RotationMath.Inner_DataUtil candidate = new RotationMath.Inner_DataUtil(rotation.init(), Math.min(90.0F, rotation.load() + dip));
         if (!this.init_Z_Minecraft_ce_DataUtil(mc, candidate)) {
            return candidate;
         }
      }

      for (float side : new float[]{7.0F, -7.0F, 14.0F, -14.0F}) {
         RotationMath.Inner_DataUtil candidate = new RotationMath.Inner_DataUtil(rotation.init() + side, rotation.load());
         if (!this.init_Z_Minecraft_ce_DataUtil(mc, candidate)) {
            return candidate;
         }
      }

      return new RotationMath.Inner_DataUtil(rotation.init() + 7.0F, rotation.load());
   }

   private boolean init_Z_Minecraft_ce_DataUtil(Minecraft mc, RotationMath.Inner_DataUtil rotation) {
      Vec3 view = Vec3.directionFromRotation(rotation.load(), rotation.init()).normalize();
      Vec3 eye = mc.player.getEyePosition();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Enderman enderman
            && enderman.isAlive()
            && (this.init_Vec3 == null || !enderman.isCreepy() || !enderman.getBoundingBox().inflate(0.6).contains(this.init_Vec3))
            && enderman.getId() != this.init_I
            && init(view, enderman.getX() - mc.player.getX(), enderman.getEyeY() - eye.y, enderman.getZ() - mc.player.getZ())
            && mc.player.hasLineOfSight(enderman, Block.VISUAL, Fluid.NONE, enderman.getEyeY())) {
            return true;
         }
      }

      return false;
   }

   private static Entity init_Entity_Minecraft_ce_DataUtil(Minecraft mc, RotationMath.Inner_DataUtil rotation) {
      Vec3 view = Vec3.directionFromRotation(rotation.load(), rotation.init()).normalize();
      Vec3 eye = mc.player.getEyePosition();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Enderman enderman
            && enderman.isAlive()
            && init(view, enderman.getX() - mc.player.getX(), enderman.getEyeY() - eye.y, enderman.getZ() - mc.player.getZ())
            && mc.player.hasLineOfSight(enderman, Block.VISUAL, Fluid.NONE, enderman.getEyeY())) {
            return enderman;
         }
      }

      return null;
   }

   static boolean init(Vec3 view, double dx, double dy, double dz) {
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return !(distance < 1.0E-6) && !(distance > 64.0)
         ? view.normalize().dot(new Vec3(dx / distance, dy / distance, dz / distance)) > 1.0 - 0.025 / distance
         : false;
   }

   public boolean load_Z() {
      return this.init_ce_DataUtil != null && this.load_I > 0;
   }

   public double init_D() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null && this.init_ce_DataUtil != null ? RotationMath.init_F(RotationMath.init(player), this.init_ce_DataUtil) : 0.0;
   }

   public static BlockHitResult init_BlockHitResult() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         double reach = mc.player.blockInteractionRange();
         Vec3 eye = mc.player.getEyePosition();
         Vec3 end = eye.add(init_Vec3().scale(reach));
         BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
         return hit != null && hit.getType() == Type.BLOCK ? hit : null;
      } else {
         return null;
      }
   }

   public static boolean init_Z_BlockPos(BlockPos cell) {
      BlockHitResult hit = init_BlockHitResult();
      return hit != null && hit.getBlockPos().equals(cell);
   }

   public static Direction init_Direction_BlockPos(BlockPos cell) {
      BlockHitResult hit = init_BlockHitResult();
      return hit != null && hit.getBlockPos().equals(cell) ? hit.getDirection() : null;
   }

   public static boolean init_Z_Entity(Entity target) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && target != null) {
         double reach = mc.player.getAttackRangeWith(mc.player.getMainHandItem()).effectiveMaxRange(mc.player);
         Vec3 eye = mc.player.getEyePosition();
         Vec3 end = eye.add(init_Vec3().scale(reach));
         AABB box = target.getBoundingBox().inflate(target.getPickRadius());
         Optional<Vec3> contact = box.clip(eye, end);
         return contact.isPresent() && init(mc, target, eye, contact.get());
      } else {
         return false;
      }
   }

   public boolean load_Z_Entity(Entity target) {
      if (!AutismLiteVariant.enabled() && autismclient.ai.AiClientTick.init()) {
         return init_Z_Entity(target);
      } else if (!this.init_Z) {
         return init_Z_Entity(target);
      } else {
         RotationMath.Inner_DataUtil rotation = SilentMovementCoordinator.init_ce_DataUtil();
         if (rotation == null) {
            return false;
         } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null && target != null) {
               double reach = mc.player.getAttackRangeWith(mc.player.getMainHandItem()).effectiveMaxRange(mc.player);
               Vec3 eye = mc.player.getEyePosition();
               Vec3 end = eye.add(Vec3.directionFromRotation(rotation.load(), rotation.init()).scale(reach));
               AABB box = target.getBoundingBox().inflate(target.getPickRadius());
               Optional<Vec3> contact = box.clip(eye, end);
               return contact.isPresent() && init(mc, target, eye, contact.get());
            } else {
               return false;
            }
         }
      }
   }

   public static Vec3 init_Vec3() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return Vec3.ZERO;
      } else if (!AutismLiteVariant.enabled() && autismclient.ai.AiClientTick.init()) {
         ServerRotationTracker.Inner_CachePool wire = ServerRotationTracker.init_ch_CachePool();
         return !wire.init_Z() ? Vec3.ZERO : Vec3.directionFromRotation(wire.write(), wire.read());
      } else {
         return player.getLookAngle();
      }
   }

   private static boolean init(Minecraft mc, Entity target, Vec3 eye, Vec3 contact) {
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

   public static Vec3 init_Vec3_BlockPos_Direction(BlockPos cell, Direction face) {
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

   public static Vec3 init_Vec3_Entity(Entity target) {
      if (target == null) {
         return Vec3.ZERO;
      }

      AABB box = target.getBoundingBox();
      double eyeY = target.getEyeHeight() > 0.0F ? target.getY() + target.getEyeHeight() * 0.85 : box.getCenter().y;
      return new Vec3(box.getCenter().x, Mth.clamp(eyeY, box.minY + 0.1, box.maxY - 0.1), box.getCenter().z);
   }

   public static Vec3 load_Vec3_Entity(Entity target) {
      Minecraft mc = Minecraft.getInstance();
      return target != null && mc.player != null && mc.level != null ? init_Vec3_Entity_Vec3(target, mc.player.getEyePosition()) : init_Vec3_Entity(target);
   }

   public static Vec3 init_Vec3_Entity_Vec3(Entity target, Vec3 eye) {
      Minecraft mc = Minecraft.getInstance();
      if (target != null && mc.player != null && mc.level != null) {
         BiPredicate<Vec3, Vec3> clear = (from, to) -> {
            BlockHitResult hit = mc.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.player));
            return hit == null || hit.getType() != Type.BLOCK;
         };
         return init(
            target.getBoundingBox(),
            eye,
            mc.player.getYRot(),
            mc.player.getXRot(),
            Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)),
            clear
         );
      } else {
         return init_Vec3_Entity(target);
      }
   }

   public static boolean init_Z_Entity_Vec3(Entity target, Vec3 point) {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.level != null && target != null && point != null ? init(target, mc.player.getEyePosition(), point) : false;
   }

   public static boolean init(Entity target, Vec3 eye, Vec3 point) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && target != null && point != null) {
         double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
         Optional<Vec3> contact = target.getBoundingBox().clip(eye, eye.add(point.subtract(eye).normalize().scale(reach)));
         return contact.isEmpty() ? false : init(mc, target, eye, contact.get());
      } else {
         return false;
      }
   }

   public static Vec3 init(AABB hitbox, Vec3 eye, float yaw, float pitch, double reach) {
      return init(hitbox, eye, yaw, pitch, reach, (from, to) -> true);
   }

   public static Vec3 init(AABB hitbox, Vec3 eye, float yaw, float pitch, double reach, BiPredicate<Vec3, Vec3> clear) {
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

   public static float init_F_DataUtil_DataUtil(AiAim.Inner_DataUtil speed) {
      return speed.init_F;
   }

   public enum Inner_DataUtil {
      init_DataUtil_DataUtil(28.0F, 28.0F, 22.0F, RotationStreamPlanner.Inner_StreamUtil.STANDARD),
      load_DataUtil_DataUtil(54.0F, 54.0F, 38.0F, RotationStreamPlanner.Inner_StreamUtil.STANDARD),
      read_DataUtil_DataUtil(45.0F, 45.0F, 180.0F, RotationStreamPlanner.Inner_StreamUtil.STANDARD),
      write(72.0F, 72.0F, 180.0F, RotationStreamPlanner.Inner_StreamUtil.STANDARD),
      update(72.0F, 72.0F, 180.0F, RotationStreamPlanner.Inner_StreamUtil.TELLY_FLICK),
      sync(72.0F, 72.0F, 180.0F, RotationStreamPlanner.Inner_StreamUtil.STANDARD, true),
      flush(72.0F, 72.0F, 100.0F, RotationStreamPlanner.Inner_StreamUtil.TELLY_FLICK, true);

      final float init_F;
      final float load_F;
      final float read_F;
      final RotationStreamPlanner.Inner_StreamUtil init_ap_StreamUtil;
      final boolean init_Z;

      Inner_DataUtil(float yawCap, float pitchCap, float cameraCap, RotationStreamPlanner.Inner_StreamUtil profile) {
         this(yawCap, pitchCap, cameraCap, profile, false);
      }

      Inner_DataUtil(float yawCap, float pitchCap, float cameraCap, RotationStreamPlanner.Inner_StreamUtil profile, boolean linear) {
         this.init_F = yawCap;
         this.load_F = pitchCap;
         this.read_F = cameraCap;
         this.init_ap_StreamUtil = profile;
         this.init_Z = linear;
      }
   }
}
