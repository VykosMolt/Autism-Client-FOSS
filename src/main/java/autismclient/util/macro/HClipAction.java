package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.g.PkgCls_a;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class HClipAction implements MacroAction {
   private static final int init_I = 128;
   private static final double init_D = 128.0;
   private static final double load_D = 0.5;
   private static final double read = 0.5;
   private static final boolean DEBUG = Boolean.getBoolean("autism.hclip.debug");
   private static HClipAction.PlanCacheKey cachedPlanKey;
   private static HClipAction.ClipPlan cachedPlan;
   private static long cachedPlanTick = -9223372036854775808L;
   public HClipAction.ClipMode mode = HClipAction.ClipMode.MANUAL;
   public double blocks = 0.0;
   public boolean useSegmented = true;
   public int segmentBlocks = 10;
   public int maxPackets = 20;
   public boolean updateLocalPosition = true;
   public boolean tryVehicleFirst = true;
   public boolean forceGrounded = false;
   public int searchRadius = 32;
   public int verticalRange = 8;
   public int maxRoutePackets = 80;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
      HClipAction.ClipOptions options = new HClipAction.ClipOptions();
      options.mode = this.mode;
      options.blocks = this.blocks;
      options.useSegmented = this.useSegmented;
      options.segmentBlocks = this.segmentBlocks;
      options.maxPackets = this.maxPackets;
      options.updateLocalPosition = this.updateLocalPosition;
      options.tryVehicleFirst = this.tryVehicleFirst;
      options.forceGrounded = this.forceGrounded;
      options.searchRadius = this.searchRadius;
      options.verticalRange = this.verticalRange;
      options.maxRoutePackets = this.maxRoutePackets;
      init(mc, options);
   }

   public static HClipAction.ClipResult init(Minecraft mc, HClipAction.ClipOptions options) {
      if (options == null) {
         options = new HClipAction.ClipOptions();
      }

      if (mc != null && mc.player != null && mc.getConnection() != null) {
         LocalPlayer player = mc.player;
         double blocks = options.blocks;
         if (options.mode != HClipAction.ClipMode.MANUAL) {
            HClipAction.ClipTarget target = init(player, options);
            if (!target.init_Z()) {
               if (options.chatFeedback) {
                  AutismClientMessaging.sendPrefixed("§cHClip: " + target.init_Str());
               }

               return new HClipAction.ClipResult(false, 0, target.init_Str());
            }

            blocks = target.init_D();
         }

         int segment = Math.max(1, options.segmentBlocks);
         int maxPaddingPackets = Math.max(1, options.maxPackets);
         int paddingPackets = options.useSegmented ? Math.max(0, (int)Math.ceil(Math.abs(blocks) / segment) - 1) : 0;
         if (paddingPackets + 1 > maxPaddingPackets) {
            paddingPackets = 0;
         }

         double yawRad = Math.toRadians(player.getYRot());
         double deltaX = -Math.sin(yawRad) * blocks;
         double deltaZ = Math.cos(yawRad) * blocks;
         HClipAction.ClipPlan plan = planClipCached(player, deltaX, deltaZ, blocks, options);
         if (!plan.init_Z()) {
            if (options.chatFeedback) {
               AutismClientMessaging.sendPrefixed("§cHClip: " + plan.message());
            }

            return new HClipAction.ClipResult(false, 0, plan.message());
         } else {
            Entity vehicle = options.tryVehicleFirst ? player.getVehicle() : null;
            int packetCount;
            if (vehicle != null) {
               if (plan.init_Z_D(player.getY())) {
                  String message = "planned route requires vertical clipping; vehicle hclip refused";
                  if (options.chatFeedback) {
                     AutismClientMessaging.sendPrefixed("§cHClip: " + message);
                  }

                  return new HClipAction.ClipResult(false, plan.waypoints().size(), message);
               }

               try {
                  for (int i = 0; i < paddingPackets; i++) {
                     init(mc, ServerboundMoveVehiclePacket.fromEntity(vehicle));
                  }

                  for (Vec3 waypoint : plan.waypoints()) {
                     vehicle.setPos(waypoint.x, waypoint.y, waypoint.z);
                     init(mc, ServerboundMoveVehiclePacket.fromEntity(vehicle));
                  }
               } catch (Throwable t) {
                  String message = "Vehicle hclip failed: " + t.getMessage();
                  if (options.chatFeedback) {
                     AutismClientMessaging.sendPrefixed("§c" + message);
                  }

                  return new HClipAction.ClipResult(false, paddingPackets + plan.waypoints().size(), message);
               }

               packetCount = paddingPackets + plan.waypoints().size();
            } else {
               boolean grounded = options.forceGrounded;
               int sent = 0;
               Vec3 current = player.position();
               init(player);

               for (Vec3 waypoint : plan.waypoints()) {
                  if (options.useSegmented) {
                     double legDist = current.distanceTo(waypoint);
                     int legPadding = Math.max(0, (int)Math.ceil(legDist / segment) - 1);
                     if (legPadding > maxPaddingPackets - 1) {
                        legPadding = Math.max(0, maxPaddingPackets - 1);
                     }

                     for (int i = 0; i < legPadding; i++) {
                        init(mc, new StatusOnly(grounded, false));
                     }

                     sent += legPadding;
                  }

                  sent += init_I_Minecraft(mc, current, waypoint, grounded);
                  current = waypoint;
               }

               Vec3 finalPos = plan.waypoints().getLast();
               if (options.updateLocalPosition) {
                  player.setPos(finalPos.x, finalPos.y, finalPos.z);
                  init(player);
               }

               packetCount = sent;
            }

            String prefix = options.mode == HClipAction.ClipMode.MANUAL
               ? "hclip " + blocks
               : "hclip " + options.mode.name().toLowerCase(Locale.ROOT) + " -> " + String.format(Locale.ROOT, "%.2f", blocks);
            String message = prefix + " (" + packetCount + " packet" + (packetCount == 1 ? "" : "s") + ")";
            if (DEBUG && plan.init_Z()) {
               message = message + ", " + plan.message();
            }

            if (options.chatFeedback) {
               AutismClientMessaging.sendPrefixed("§a" + message);
            }

            return new HClipAction.ClipResult(true, packetCount, message);
         }
      } else {
         if (options.chatFeedback) {
            AutismClientMessaging.sendPrefixed("§cHClip: no world / connection.");
         }

         return new HClipAction.ClipResult(false, 0, "No world / connection");
      }
   }

   private static int init_I_Minecraft(Minecraft mc, Vec3 from, Vec3 to, boolean grounded) {
      List<ClipCollisionHelper.ClipStep> steps = ClipCollisionHelper.steps(from, to, grounded);

      for (ClipCollisionHelper.ClipStep step : steps) {
         init(mc, new Pos(step.position(), step.onGround(), false));
      }

      return steps.size();
   }

   private static void init(Minecraft mc, Packet<?> packet) {
      if (AutismLiteVariant.enabled()) {
         mc.getConnection().send(packet);
      } else {
         PkgCls_a.init_V_Runnable(() -> mc.getConnection().send(packet));
      }
   }

   private static void init(LocalPlayer player) {
      player.resetFallDistance();
      Vec3 velocity = player.getDeltaMovement();
      if (velocity.y < 0.0) {
         player.setDeltaMovement(velocity.x, 0.0, velocity.z);
      }
   }

   private static HClipAction.ClipPlan planClipCached(
      LocalPlayer player, double deltaX, double deltaZ, double blocks, HClipAction.ClipOptions options
   ) {
      Vec3 start = player.position();
      Vec3 requestedTarget = start.add(deltaX, 0.0, deltaZ);
      int radius = Math.max(1, options.searchRadius);
      int verticalRange = Math.max(0, options.verticalRange);
      int escapeRange = Math.max(verticalRange, 128);
      int routeCap = Math.max(1, Math.min(200, options.maxRoutePackets));
      HClipAction.PlanCacheKey cacheKey = HClipAction.PlanCacheKey.init(player, start, requestedTarget, radius, verticalRange, routeCap);
      long tick = player.level() == null ? -9223372036854775808L : player.level().getGameTime();
      if (cachedPlanKey != null && cachedPlanKey.equals(cacheKey) && cachedPlan != null && tick - cachedPlanTick <= 2L) {
         return cachedPlan;
      }

      TpRoutePlanner.RoutePlan route = TpRoutePlanner.planRoute(
         TpRoutePlanner.entityView(player), start, requestedTarget, radius, escapeRange, routeCap, false
      );
      return !route.isComplete()
         ? HClipAction.ClipPlan.failedPlan(route.detail())
         : init(cacheKey, tick, HClipAction.ClipPlan.init(start.y, route.waypoints(), route.detail()));
   }

   private static HClipAction.ClipPlan init(HClipAction.PlanCacheKey key, long tick, HClipAction.ClipPlan plan) {
      if (plan.init_Z()) {
         cachedPlanKey = key;
         cachedPlan = plan;
         cachedPlanTick = tick;
      }

      return plan;
   }

   private static HClipAction.ClipPlan init_TokenStore_StreamU_LocalPlayer_2(LocalPlayer player, Vec3 start, Vec3 target, int escapeRange, int routeCap) {
      for (int dy : init(escapeRange)) {
         if (dy != 0) {
            Vec3 escStart = new Vec3(start.x, start.y + dy, start.z);
            Vec3 escEnd = new Vec3(target.x, start.y + dy, target.z);
            if (init(player, escStart) && load(player, escStart) && init(player, escEnd) && load(player, escEnd) && init(player, escStart, escEnd)) {
               List<Vec3> waypoints = new ArrayList<>(3);
               waypoints.add(escStart);
               waypoints.add(escEnd);
               Vec3 landing = findLandingSpot(player, target.x, target.z, start.y);
               if (landing != null && !samePosition(landing, escEnd)) {
                  waypoints.add(landing);
               }

               List<Vec3> route = init(start, waypoints);
               if (!route.isEmpty() && route.size() <= routeCap) {
                  return HClipAction.ClipPlan.init(start.y, route, "layered dy=" + dy);
               }
            }
         }
      }

      return HClipAction.ClipPlan.failedPlan("no clear layer");
   }

   private static HClipAction.ClipPlan init(LocalPlayer player, Vec3 start, Vec3 target, int escapeRange, int searchRadius, int routeCap) {
      List<Vec3> route = new ArrayList<>();
      Vec3 cur = start;
      int guard = 0;

      label63:
      while (horizontalDistance(cur, target) > 0.5 && guard++ < routeCap) {
         Vec3 adv = init_Vec3_LocalPlayer_2(player, cur, target, searchRadius);
         if (horizontalDistance(adv, cur) > 0.5) {
            route.add(adv);
            cur = adv;
         } else {
            boolean escaped = false;
            Iterator var11 = init(escapeRange).iterator();

            while (true) {
               if (var11.hasNext()) {
                  int dy = (Integer)var11.next();
                  if (dy == 0) {
                     continue;
                  }

                  Vec3 layer = new Vec3(cur.x, cur.y + dy, cur.z);
                  if (!init(player, layer) || !load(player, layer)) {
                     continue;
                  }

                  Vec3 adv2 = init_Vec3_LocalPlayer_2(player, layer, target, searchRadius);
                  if (!(horizontalDistance(adv2, layer) > 1.0)) {
                     continue;
                  }

                  route.add(layer);
                  route.add(adv2);
                  cur = adv2;
                  escaped = true;
               }

               if (!escaped) {
                  break label63;
               }
               break;
            }
         }
      }

      if (route.isEmpty()) {
         return HClipAction.ClipPlan.failedPlan("segmented made no progress");
      }

      Vec3 landing = findLandingSpot(player, cur.x, cur.z, start.y);
      if (landing != null && !samePosition(landing, cur)) {
         route.add(landing);
      }

      List<Vec3> cleaned = init(start, route);
      if (cleaned.isEmpty()) {
         return HClipAction.ClipPlan.failedPlan("segmented empty route");
      } else {
         return cleaned.size() > routeCap
            ? HClipAction.ClipPlan.failedPlan("segmented route needs " + cleaned.size() + " packets, cap is " + routeCap)
            : HClipAction.ClipPlan.init(start.y, cleaned, "segmented");
      }
   }

   private static Vec3 findLandingSpot(LocalPlayer player, double x, double z, double preferredY) {
      for (double off = 0.0; off <= 128.0; off += 0.5) {
         int[] signs = off == 0.0 ? new int[]{1} : new int[]{1, -1};

         for (int sign : signs) {
            Vec3 candidate = new Vec3(x, preferredY + sign * off, z);
            if (init(player, candidate) && load(player, candidate)) {
               return candidate;
            }
         }
      }

      return null;
   }

   private static Vec3 init_Vec3_LocalPlayer_2(LocalPlayer player, Vec3 from, Vec3 target, int searchRadius) {
      double dx = target.x - from.x;
      double dz = target.z - from.z;
      double dist = Math.hypot(dx, dz);
      if (dist < 1.0E-5) {
         return from;
      }

      double maxDist = Math.min(dist, searchRadius);
      double ux = dx / dist;
      double uz = dz / dist;
      Vec3 best = from;

      for (double d = 0.5; d <= maxDist + 1.0E-9; d += 0.5) {
         Vec3 candidate = new Vec3(from.x + ux * d, from.y, from.z + uz * d);
         if (!init(player, candidate) || !load(player, candidate)) {
            break;
         }

         best = candidate;
      }

      return best;
   }

   private static HClipAction.ClipTarget init(LocalPlayer player, HClipAction.ClipOptions options) {
      int direction = options.mode == HClipAction.ClipMode.BACK ? -1 : 1;
      int radius = Math.max(1, Math.min(128, options.searchRadius));
      double yawRad = Math.toRadians(player.getYRot());
      double dirX = -Math.sin(yawRad) * direction;
      double dirZ = Math.cos(yawRad) * direction;
      Vec3 start = player.position();
      boolean seenBlocked = false;
      int firstBlocked = 0;
      int lastBlocked = 0;

      for (int distance = 1; distance <= radius; distance++) {
         Vec3 candidate = new Vec3(start.x + dirX * distance, start.y, start.z + dirZ * distance);
         boolean clear = init(player, candidate) && load(player, candidate);
         if (!clear) {
            if (!seenBlocked) {
               firstBlocked = distance;
            }

            lastBlocked = distance;
            seenBlocked = true;
         } else if (seenBlocked) {
            return new HClipAction.ClipTarget(true, direction * distance, "past obstruction");
         }
      }

      if (seenBlocked) {
         int requested = Math.min(radius, Math.max(firstBlocked + 1, lastBlocked + 1));
         return new HClipAction.ClipTarget(true, direction * requested, "route around obstruction");
      } else {
         return new HClipAction.ClipTarget(
            false, 0.0, options.mode == HClipAction.ClipMode.BACK ? "no blocking run behind you" : "no blocking run in front of you"
         );
      }
   }

   private static List<Integer> init(int verticalRange) {
      List<Integer> offsets = new ArrayList<>(verticalRange * 2 + 1);
      offsets.add(0);

      for (int i = 1; i <= verticalRange; i++) {
         offsets.add(i);
         offsets.add(-i);
      }

      return offsets;
   }

   private static List<Vec3> init(Vec3 start, List<Vec3> route) {
      List<Vec3> cleaned = new ArrayList<>(route.size());
      Vec3 previous = start;

      for (Vec3 waypoint : route) {
         if (!samePosition(previous, waypoint)) {
            cleaned.add(waypoint);
            previous = waypoint;
         }
      }

      return cleaned;
   }

   private static boolean samePosition(Vec3 a, Vec3 b) {
      return Math.abs(a.x - b.x) < 1.0E-5 && Math.abs(a.y - b.y) < 1.0E-5 && Math.abs(a.z - b.z) < 1.0E-5;
   }

   private static double horizontalDistance(Vec3 a, Vec3 b) {
      return Math.hypot(a.x - b.x, a.z - b.z);
   }

   private static boolean init(LocalPlayer player, Vec3 pos) {
      return player != null && player.level() != null && player.level().isLoaded(BlockPos.containing(pos));
   }

   private static boolean init(LocalPlayer player, Vec3 from, Vec3 to) {
      if (Math.abs(from.x - to.x) < 1.0E-5 && Math.abs(from.z - to.z) < 1.0E-5) {
         return true;
      }

      if (Math.abs(from.y - to.y) > 1.0E-5) {
         return false;
      }

      double distance = from.distanceTo(to);
      int steps = Math.max(1, (int)Math.ceil(distance / 0.25));

      for (int i = 1; i <= steps; i++) {
         double t = (double)i / steps;
         Vec3 pos = from.lerp(to, t);
         if (!init(player, pos) || !load(player, pos)) {
            return false;
         }
      }

      return true;
   }

   private static boolean load(LocalPlayer player, Vec3 pos) {
      Vec3 delta = pos.subtract(player.position());
      return player.level().noCollision(player, player.getBoundingBox().move(delta));
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.mode.name());
      tag.putDouble("blocks", this.blocks);
      tag.putBoolean("useSegmented", this.useSegmented);
      tag.putInt("segmentBlocks", Math.max(1, this.segmentBlocks));
      tag.putInt("maxPackets", Math.max(1, this.maxPackets));
      tag.putBoolean("updateLocalPosition", this.updateLocalPosition);
      tag.putBoolean("tryVehicleFirst", this.tryVehicleFirst);
      tag.putBoolean("forceGrounded", this.forceGrounded);
      tag.putInt("searchRadius", Math.max(1, this.searchRadius));
      tag.putInt("verticalRange", Math.max(0, this.verticalRange));
      tag.putInt("maxRoutePackets", Math.max(1, Math.min(200, this.maxRoutePackets)));
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.mode = init(tag.getStringOr("mode", HClipAction.ClipMode.MANUAL.name()));
      this.blocks = tag.getDoubleOr("blocks", 0.0);
      this.useSegmented = tag.getBooleanOr("useSegmented", true);
      this.segmentBlocks = Math.max(1, tag.getIntOr("segmentBlocks", 10));
      this.maxPackets = Math.max(1, tag.getIntOr("maxPackets", 20));
      this.updateLocalPosition = tag.getBooleanOr("updateLocalPosition", true);
      this.tryVehicleFirst = tag.getBooleanOr("tryVehicleFirst", true);
      this.forceGrounded = tag.getBooleanOr("forceGrounded", false);
      this.searchRadius = Math.max(1, tag.getIntOr("searchRadius", 32));
      this.verticalRange = Math.max(0, tag.getIntOr("verticalRange", 8));
      this.maxRoutePackets = Math.max(1, Math.min(200, tag.contains("maxRoutePackets") ? tag.getIntOr("maxRoutePackets", 80) : tag.getIntOr("maxPackets", 80)));
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.HCLIP;
   }

   @Override
   public String getDisplayName() {
      if (this.mode == HClipAction.ClipMode.FORWARD) {
         return "HClip Forward";
      } else {
         return this.mode == HClipAction.ClipMode.BACK ? "HClip Back" : "HClip " + String.format(Locale.ROOT, "%.2f", this.blocks);
      }
   }

   @Override
   public String getIcon() {
      return "HC";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   private static HClipAction.ClipMode init(String value) {
      try {
         return HClipAction.ClipMode.valueOf(value == null ? HClipAction.ClipMode.MANUAL.name() : value.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         return HClipAction.ClipMode.MANUAL;
      }
   }

   public static final class ClipOptions {
      public HClipAction.ClipMode mode = HClipAction.ClipMode.MANUAL;
      public double blocks = 0.0;
      public boolean useSegmented = true;
      public int segmentBlocks = 10;
      public int maxPackets = 20;
      public boolean updateLocalPosition = true;
      public boolean tryVehicleFirst = true;
      public boolean forceGrounded = false;
      public int searchRadius = 32;
      public int verticalRange = 8;
      public int maxRoutePackets = 80;
      public boolean chatFeedback = false;

      public static HClipAction.ClipOptions init(double blocks) {
         HClipAction.ClipOptions options = new HClipAction.ClipOptions();
         options.blocks = blocks;
         return options;
      }

      public HClipAction.ClipOptions init() {
         this.useSegmented = false;
         return this;
      }
   }

   public enum ClipMode {
      MANUAL,
      FORWARD,
      BACK;
   }

   public record ClipResult(boolean success, int waypointCount, String message) {
   }

   private record ClipTarget(boolean init_Z, double init_D, String init_Str) {
   }

   private record ClipPlan(boolean init_Z, double originY, List<Vec3> waypoints, String message) {
      static HClipAction.ClipPlan init(double originY, List<Vec3> waypoints) {
         return init(originY, waypoints, "ok");
      }

      static HClipAction.ClipPlan init(double originY, List<Vec3> waypoints, String message) {
         return new HClipAction.ClipPlan(true, originY, List.copyOf(waypoints), message);
      }

      static HClipAction.ClipPlan failedPlan(String message) {
         return new HClipAction.ClipPlan(false, 0.0, List.of(), message);
      }

      boolean init_Z_D(double fallbackOriginY) {
         if (this.waypoints.isEmpty()) {
            return false;
         }

         double y = this.originY == 0.0 ? fallbackOriginY : this.originY;

         for (Vec3 waypoint : this.waypoints) {
            if (Math.abs(waypoint.y - y) > 1.0E-5) {
               return true;
            }
         }

         return false;
      }
   }

   private record PlanCacheKey(int init_I, long init_J, long load_J, long read_J, long write_J, long update, long sync, int load_I, int read_I, int write_I) {
      static HClipAction.PlanCacheKey init(LocalPlayer player, Vec3 start, Vec3 target, int radius, int verticalRange, int routeCap) {
         int levelId = player.level() == null ? 0 : System.identityHashCode(player.level());
         return new HClipAction.PlanCacheKey(
            levelId, init(start.x), init(start.y), init(start.z), init(target.x), init(target.y), init(target.z), radius, verticalRange, routeCap
         );
      }

      private static long init(double value) {
         return Math.round(value * 64.0);
      }
   }
}
