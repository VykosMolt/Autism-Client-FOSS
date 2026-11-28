package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.g.PkgCls_a;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VClipAction implements MacroAction {
   private static final double init_D = 0.5;
   private static final double load = 128.0;
   public VClipAction.ClipMode mode = VClipAction.ClipMode.MANUAL;
   public double deltaY = 0.0;
   public boolean useSegmented = true;
   public int segmentBlocks = 10;
   public int maxPackets = 20;
   public boolean updateLocalPosition = true;
   public boolean tryVehicleFirst = true;
   public boolean forceGrounded = true;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
      VClipAction.ClipOptions options = new VClipAction.ClipOptions();
      options.mode = this.mode;
      options.deltaY = this.deltaY;
      options.segmented = this.useSegmented;
      options.segmentBlocks = this.segmentBlocks;
      options.maxPackets = this.maxPackets;
      options.updateLocalPosition = this.updateLocalPosition;
      options.read = this.tryVehicleFirst;
      options.write = this.forceGrounded;
      init(mc, options);
   }

   public static VClipAction.ClipResult init(Minecraft mc, VClipAction.ClipOptions options) {
      if (options == null) {
         options = new VClipAction.ClipOptions();
      }

      if (mc != null && mc.player != null && mc.getConnection() != null) {
         LocalPlayer player = mc.player;
         double blocks = options.deltaY;
         if (options.mode != VClipAction.ClipMode.MANUAL) {
            VClipAction.ClipTarget target = init(player, options.mode);
            if (!target.found()) {
               if (options.update) {
                  AutismClientMessaging.sendPrefixed("§cVClip: " + target.message());
               }

               return new VClipAction.ClipResult(false, 0, target.message());
            }

            blocks = target.blocks();
         }

         int segment = Math.max(1, options.segmentBlocks);
         int maxPackets = Math.max(1, options.maxPackets);
         int packetsRequired = options.segmented ? (int)Math.ceil(Math.abs(blocks) / segment) : 1;
         if (packetsRequired > maxPackets) {
            packetsRequired = 1;
         }

         if (packetsRequired <= 0) {
            packetsRequired = 1;
         }

         Entity vehicle = options.read ? player.getVehicle() : null;
         if (vehicle != null) {
            try {
               for (int i = 0; i < packetsRequired - 1; i++) {
                  init(mc, ServerboundMoveVehiclePacket.fromEntity(vehicle));
               }

               vehicle.setPos(vehicle.getX(), vehicle.getY() + blocks, vehicle.getZ());
               init(mc, ServerboundMoveVehiclePacket.fromEntity(vehicle));
            } catch (Throwable t) {
               String message = "Vehicle vclip failed: " + t.getMessage();
               if (options.update) {
                  AutismClientMessaging.sendPrefixed("§c" + message);
               }

               return new VClipAction.ClipResult(false, packetsRequired, message);
            }
         } else {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            boolean grounded = options.write || player.onGround();
            boolean horizontalCollision = player.horizontalCollision;
            if (blocks < -3.0) {
               int padding = options.segmented ? Math.max(0, packetsRequired - 1) : 0;
               packetsRequired = init(mc, player, x, y, z, blocks, padding, options.updateLocalPosition);
            } else {
               player.resetFallDistance();

               for (int i = 0; i < packetsRequired - 1; i++) {
                  init(mc, new StatusOnly(grounded, horizontalCollision));
               }

               init(mc, new Pos(x, y + blocks, z, grounded, horizontalCollision));
               if (options.updateLocalPosition) {
                  player.setPos(x, y + blocks, z);
                  resetFall(player);
               }
            }
         }

         String prefix = options.mode == VClipAction.ClipMode.MANUAL
            ? "vclip " + blocks
            : "vclip " + options.mode.name().toLowerCase(Locale.ROOT) + " -> " + String.format(Locale.ROOT, "%.2f", blocks);
         String message = prefix + " (" + packetsRequired + " packet" + (packetsRequired == 1 ? "" : "s") + ")";
         if (options.update) {
            AutismClientMessaging.sendPrefixed("§a" + message);
         }

         return new VClipAction.ClipResult(true, packetsRequired, message);
      } else {
         if (options.update) {
            AutismClientMessaging.sendPrefixed("§cVClip: no world / connection.");
         }

         return new VClipAction.ClipResult(false, 0, "No world / connection");
      }
   }

   private static VClipAction.ClipTarget init(LocalPlayer player, VClipAction.ClipMode mode) {
      if (mode == VClipAction.ClipMode.TOP) {
         return findTarget(player);
      } else {
         return mode == VClipAction.ClipMode.BOTTOM ? load(player) : new VClipAction.ClipTarget(true, 0.0, "manual");
      }
   }

   private static VClipAction.ClipTarget findTarget(LocalPlayer player) {
      Vec3 start = player.position();
      Vec3 candidate = TpRoutePlanner.update(TpRoutePlanner.entityView(player), start, 128.0);
      return candidate != null ? new VClipAction.ClipTarget(true, candidate.y - start.y, "top") : new VClipAction.ClipTarget(false, 0.0, "no direct safe top target");
   }

   private static VClipAction.ClipTarget load(LocalPlayer player) {
      Vec3 start = player.position();

      for (double offset = 0.5; offset <= 128.0; offset += 0.5) {
         Vec3 candidate = new Vec3(start.x, start.y - offset, start.z);
         if (init(player, candidate) && load(player, candidate) && read(player, candidate)) {
            return new VClipAction.ClipTarget(true, candidate.y - start.y, "bottom");
         }
      }

      return new VClipAction.ClipTarget(false, 0.0, "no direct safe bottom target");
   }

   private static boolean init(LocalPlayer player, Vec3 pos) {
      return player != null && player.level() != null && player.level().isLoaded(BlockPos.containing(pos));
   }

   private static boolean load(LocalPlayer player, Vec3 pos) {
      Vec3 delta = pos.subtract(player.position());
      return player.level().noCollision(player, player.getBoundingBox().move(delta));
   }

   private static boolean read(LocalPlayer player, Vec3 pos) {
      Vec3 delta = pos.subtract(player.position());
      AABB moved = player.getBoundingBox().move(delta);
      return !player.level().noCollision(player, moved.move(0.0, -0.0625, 0.0));
   }

   private static int init(Minecraft mc, LocalPlayer player, double x, double y, double z, double blocks, int paddingPackets, boolean updateLocalPosition) {
      double targetY = y + blocks;
      resetFall(player);
      int sent = 0;

      for (int i = 0; i < paddingPackets; i++) {
         init(mc, new StatusOnly(true, false));
         sent++;
      }

      init(mc, new StatusOnly(true, false));
      sent++;
      Vec3 from = new Vec3(x, y, z);
      Vec3 target = new Vec3(x, targetY, z);

      for (ClipCollisionHelper.ClipStep step : ClipCollisionHelper.init(from, target, true)) {
         init(mc, new Pos(step.position(), step.onGround(), false));
         sent++;
      }

      if (updateLocalPosition) {
         player.setPos(x, targetY, z);
         resetFall(player);
      }

      return sent;
   }

   private static void init(Minecraft mc, Packet<?> packet) {
      if (AutismLiteVariant.enabled()) {
         mc.getConnection().send(packet);
      } else {
         PkgCls_a.init_V_Runnable(() -> mc.getConnection().send(packet));
      }
   }

   private static void resetFall(LocalPlayer player) {
      player.resetFallDistance();
      Vec3 velocity = player.getDeltaMovement();
      if (velocity.y < 0.0) {
         player.setDeltaMovement(velocity.x, 0.0, velocity.z);
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.mode.name());
      tag.putDouble("deltaY", this.deltaY);
      tag.putBoolean("useSegmented", this.useSegmented);
      tag.putInt("segmentBlocks", Math.max(1, this.segmentBlocks));
      tag.putInt("maxPackets", Math.max(1, this.maxPackets));
      tag.putBoolean("updateLocalPosition", this.updateLocalPosition);
      tag.putBoolean("tryVehicleFirst", this.tryVehicleFirst);
      tag.putBoolean("forceGrounded", this.forceGrounded);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.mode = init(tag.getStringOr("mode", VClipAction.ClipMode.MANUAL.name()));
      this.deltaY = tag.getDoubleOr("deltaY", 0.0);
      this.useSegmented = tag.getBooleanOr("useSegmented", true);
      this.segmentBlocks = Math.max(1, tag.getIntOr("segmentBlocks", 10));
      this.maxPackets = Math.max(1, tag.getIntOr("maxPackets", 20));
      this.updateLocalPosition = tag.getBooleanOr("updateLocalPosition", true);
      this.tryVehicleFirst = tag.getBooleanOr("tryVehicleFirst", true);
      this.forceGrounded = tag.getBooleanOr("forceGrounded", true);
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.VCLIP;
   }

   @Override
   public String getDisplayName() {
      if (this.mode == VClipAction.ClipMode.TOP) {
         return "VClip Top";
      } else {
         return this.mode == VClipAction.ClipMode.BOTTOM ? "VClip Bottom" : "VClip Y=" + String.format(Locale.ROOT, "%.2f", this.deltaY);
      }
   }

   @Override
   public String getIcon() {
      return "VC";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   private static VClipAction.ClipMode init(String value) {
      try {
         return VClipAction.ClipMode.valueOf(value == null ? VClipAction.ClipMode.MANUAL.name() : value.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         return VClipAction.ClipMode.MANUAL;
      }
   }

   public record ClipResult(boolean success, int init_I, String message) {
   }

   public static final class ClipOptions {
      public VClipAction.ClipMode mode = VClipAction.ClipMode.MANUAL;
      public double deltaY = 0.0;
      public boolean segmented = true;
      public int segmentBlocks = 10;
      public int maxPackets = 20;
      public boolean updateLocalPosition = true;
      public boolean read = true;
      public boolean write = true;
      public boolean update = false;

      public static VClipAction.ClipOptions init(double blocks) {
         VClipAction.ClipOptions options = new VClipAction.ClipOptions();
         options.deltaY = blocks;
         return options;
      }

      public VClipAction.ClipOptions init() {
         this.segmented = false;
         return this;
      }
   }

   private record ClipTarget(boolean found, double blocks, String message) {
   }

   public enum ClipMode {
      MANUAL,
      TOP,
      BOTTOM;
   }
}
