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

public class ap implements MacroAction {
   private static final double init_D = 0.5;
   private static final double load = 128.0;
   public ap.Inner_StreamUtil mode = ap.Inner_StreamUtil.MANUAL;
   public double deltaY = 0.0;
   public boolean useSegmented = true;
   public int segmentBlocks = 10;
   public int maxPackets = 20;
   public boolean updateLocalPosition = true;
   public boolean tryVehicleFirst = true;
   public boolean forceGrounded = true;
   private boolean init_Z = true;

   @Override
   public void execute(Minecraft mc) {
      ap.Inner_CachePool options = new ap.Inner_CachePool();
      options.init_ap_StreamUtil = this.mode;
      options.init_D = this.deltaY;
      options.init_Z = this.useSegmented;
      options.init_I = this.segmentBlocks;
      options.load_I = this.maxPackets;
      options.load_Z = this.updateLocalPosition;
      options.read = this.tryVehicleFirst;
      options.write = this.forceGrounded;
      init(mc, options);
   }

   public static ap.Inner_BufferHelper init(Minecraft mc, ap.Inner_CachePool options) {
      if (options == null) {
         options = new ap.Inner_CachePool();
      }

      if (mc != null && mc.player != null && mc.getConnection() != null) {
         LocalPlayer player = mc.player;
         double blocks = options.init_D;
         if (options.init_ap_StreamUtil != ap.Inner_StreamUtil.MANUAL) {
            ap.Inner_DataUtil target = init(player, options.init_ap_StreamUtil);
            if (!target.init_Z()) {
               if (options.update) {
                  AutismClientMessaging.sendPrefixed("§cVClip: " + target.init_Str());
               }

               return new ap.Inner_BufferHelper(false, 0, target.init_Str());
            }

            blocks = target.init_D();
         }

         int segment = Math.max(1, options.init_I);
         int maxPackets = Math.max(1, options.load_I);
         int packetsRequired = options.init_Z ? (int)Math.ceil(Math.abs(blocks) / segment) : 1;
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

               return new ap.Inner_BufferHelper(false, packetsRequired, message);
            }
         } else {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            boolean grounded = options.write || player.onGround();
            boolean horizontalCollision = player.horizontalCollision;
            if (blocks < -3.0) {
               int padding = options.init_Z ? Math.max(0, packetsRequired - 1) : 0;
               packetsRequired = init(mc, player, x, y, z, blocks, padding, options.load_Z);
            } else {
               player.resetFallDistance();

               for (int i = 0; i < packetsRequired - 1; i++) {
                  init(mc, new StatusOnly(grounded, horizontalCollision));
               }

               init(mc, new Pos(x, y + blocks, z, grounded, horizontalCollision));
               if (options.load_Z) {
                  player.setPos(x, y + blocks, z);
                  init_V(player);
               }
            }
         }

         String prefix = options.init_ap_StreamUtil == ap.Inner_StreamUtil.MANUAL
            ? "vclip " + blocks
            : "vclip " + options.init_ap_StreamUtil.name().toLowerCase(Locale.ROOT) + " -> " + String.format(Locale.ROOT, "%.2f", blocks);
         String message = prefix + " (" + packetsRequired + " packet" + (packetsRequired == 1 ? "" : "s") + ")";
         if (options.update) {
            AutismClientMessaging.sendPrefixed("§a" + message);
         }

         return new ap.Inner_BufferHelper(true, packetsRequired, message);
      } else {
         if (options.update) {
            AutismClientMessaging.sendPrefixed("§cVClip: no world / connection.");
         }

         return new ap.Inner_BufferHelper(false, 0, "No world / connection");
      }
   }

   private static ap.Inner_DataUtil init(LocalPlayer player, ap.Inner_StreamUtil mode) {
      if (mode == ap.Inner_StreamUtil.TOP) {
         return init_ap_DataUtil(player);
      } else {
         return mode == ap.Inner_StreamUtil.BOTTOM ? load(player) : new ap.Inner_DataUtil(true, 0.0, "manual");
      }
   }

   private static ap.Inner_DataUtil init_ap_DataUtil(LocalPlayer player) {
      Vec3 start = player.position();
      Vec3 candidate = F.update(F.init_F_StreamUtil_Entity(player), start, 128.0);
      return candidate != null ? new ap.Inner_DataUtil(true, candidate.y - start.y, "top") : new ap.Inner_DataUtil(false, 0.0, "no direct safe top target");
   }

   private static ap.Inner_DataUtil load(LocalPlayer player) {
      Vec3 start = player.position();

      for (double offset = 0.5; offset <= 128.0; offset += 0.5) {
         Vec3 candidate = new Vec3(start.x, start.y - offset, start.z);
         if (init(player, candidate) && load(player, candidate) && read(player, candidate)) {
            return new ap.Inner_DataUtil(true, candidate.y - start.y, "bottom");
         }
      }

      return new ap.Inner_DataUtil(false, 0.0, "no direct safe bottom target");
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
      init_V(player);
      int sent = 0;

      for (int i = 0; i < paddingPackets; i++) {
         init(mc, new StatusOnly(true, false));
         sent++;
      }

      init(mc, new StatusOnly(true, false));
      sent++;
      Vec3 from = new Vec3(x, y, z);
      Vec3 target = new Vec3(x, targetY, z);

      for (A.Inner_DataUtil step : A.init(from, target, true)) {
         init(mc, new Pos(step.init_Vec3(), step.init_Z(), false));
         sent++;
      }

      if (updateLocalPosition) {
         player.setPos(x, targetY, z);
         init_V(player);
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

   private static void init_V(LocalPlayer player) {
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
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.mode = init(tag.getStringOr("mode", ap.Inner_StreamUtil.MANUAL.name()));
      this.deltaY = tag.getDoubleOr("deltaY", 0.0);
      this.useSegmented = tag.getBooleanOr("useSegmented", true);
      this.segmentBlocks = Math.max(1, tag.getIntOr("segmentBlocks", 10));
      this.maxPackets = Math.max(1, tag.getIntOr("maxPackets", 20));
      this.updateLocalPosition = tag.getBooleanOr("updateLocalPosition", true);
      this.tryVehicleFirst = tag.getBooleanOr("tryVehicleFirst", true);
      this.forceGrounded = tag.getBooleanOr("forceGrounded", true);
      if (tag.contains("enabled")) {
         this.init_Z = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.VCLIP;
   }

   @Override
   public String getDisplayName() {
      if (this.mode == ap.Inner_StreamUtil.TOP) {
         return "VClip Top";
      } else {
         return this.mode == ap.Inner_StreamUtil.BOTTOM ? "VClip Bottom" : "VClip Y=" + String.format(Locale.ROOT, "%.2f", this.deltaY);
      }
   }

   @Override
   public String getIcon() {
      return "VC";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init_Z = e;
   }

   private static ap.Inner_StreamUtil init(String value) {
      try {
         return ap.Inner_StreamUtil.valueOf(value == null ? ap.Inner_StreamUtil.MANUAL.name() : value.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         return ap.Inner_StreamUtil.MANUAL;
      }
   }

   public record Inner_BufferHelper(boolean init_Z, int init_I, String init_Str) {
   }

   public static final class Inner_CachePool {
      public ap.Inner_StreamUtil init_ap_StreamUtil = ap.Inner_StreamUtil.MANUAL;
      public double init_D = 0.0;
      public boolean init_Z = true;
      public int init_I = 10;
      public int load_I = 20;
      public boolean load_Z = true;
      public boolean read = true;
      public boolean write = true;
      public boolean update = false;

      public static ap.Inner_CachePool init(double blocks) {
         ap.Inner_CachePool options = new ap.Inner_CachePool();
         options.init_D = blocks;
         return options;
      }

      public ap.Inner_CachePool init() {
         this.init_Z = false;
         return this;
      }
   }

   private record Inner_DataUtil(boolean init_Z, double init_D, String init_Str) {
   }

   public enum Inner_StreamUtil {
      MANUAL,
      TOP,
      BOTTOM;
   }
}
