package autismclient.util;

import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class RemoteViewController {
   private static volatile Player target;
   private static volatile UUID targetUuid;
   private static volatile String targetName = "";
   private static volatile ClientLevel level;
   private static CameraType previousCamera = CameraType.FIRST_PERSON;
   private static boolean wasThirdPerson;

   private RemoteViewController() {
   }

   public static boolean isActive() {
      return !AutismLiteVariant.enabled() && !autismclient.e.TableIndex.init() ? false : targetUuid != null;
   }

   public static String getOldName() {
      return targetName;
   }

   public static RemoteViewController.ViewResult startViewing(Player player) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && mc.level != null && mc.getConnection() != null) {
         if (player == null || player.isRemoved() || player.level() != mc.level) {
            return new RemoteViewController.ViewResult(false, "That player is not loaded.");
         }

         if (player == mc.player) {
            return new RemoteViewController.ViewResult(false, "You cannot remote-view yourself.");
         }

         if (autismclient.modules.FreecamController.isActive()) {
            return new RemoteViewController.ViewResult(false, "Disable Freecam first.");
         }

         if (isActive() && player.getUUID().equals(targetUuid)) {
            stop(false);
            return new RemoteViewController.ViewResult(true, "Remote view stopped.");
         }

         if (isActive()) {
            stop(false);
         }

         previousCamera = mc.options.getCameraType();
         wasThirdPerson = !previousCamera.isFirstPerson();
         target = player;
         targetUuid = player.getUUID();
         targetName = nameOf(player);
         level = mc.level;
         mc.options.setCameraType(CameraType.FIRST_PERSON);
         mc.setCameraEntity(player);
         return new RemoteViewController.ViewResult(true, "Viewing " + targetName + ". Use " + autismclient.commands.CommandManager.getPrefix() + "rv stop to leave.");
      } else {
         return new RemoteViewController.ViewResult(false, "Join a world first.");
      }
   }

   public static void stop(boolean notify) {
      Minecraft mc = Minecraft.getInstance();
      Player oldTarget = target;
      String oldName = targetName;
      boolean restorePerspective = wasThirdPerson;
      CameraType restoreType = previousCamera;
      clearState();
      if (mc != null) {
         Entity camera = mc.getCameraEntity();
         if (mc.player != null && (camera == oldTarget || camera == null || camera.isRemoved())) {
            mc.setCameraEntity(mc.player);
         }

         if (restorePerspective && mc.options.getCameraType().isFirstPerson()) {
            mc.options.setCameraType(restoreType);
         }
      }

      if (notify && oldTarget != null) {
         bi.init("Remote view ended" + (oldName.isBlank() ? "" : ": " + oldName), -14249);
      }
   }

   public static void tick() {
      if (isActive()) {
         Minecraft mc = Minecraft.getInstance();
         Player current = target;
         if (mc == null
            || mc.player == null
            || mc.level == null
            || mc.getConnection() == null
            || mc.level != level
            || current == null
            || current.isRemoved()
            || !current.getUUID().equals(targetUuid)
            || mc.getCameraEntity() != current) {
            stop(true);
         }
      }
   }

   public static Player viewedPlayer() {
      Minecraft mc = Minecraft.getInstance();
      Player current = target;
      return isActive() && current != null && !current.isRemoved() && mc != null && mc.level == level && mc.getCameraEntity() == current
         ? current
         : null;
   }

   public static Player init_Player(Player multiPilot) {
      return multiPilot != null ? multiPilot : viewedPlayer();
   }

   public static Entity swapCamera(Minecraft mc) {
      Player current = viewedPlayer();
      if (current != null && mc != null && mc.player != null && mc.getCameraEntity() == current) {
         mc.setCameraEntity(mc.player);
         return current;
      } else {
         return null;
      }
   }

   public static void restoreCamera(Minecraft mc, Entity restore) {
      if (mc != null && restore != null && isActive() && restore == target && !restore.isRemoved() && mc.getCameraEntity() == mc.player) {
         mc.setCameraEntity(restore);
      }
   }

   private static String nameOf(Player player) {
      String name = player.getGameProfile() == null ? null : player.getGameProfile().name();
      if (name == null || name.isBlank()) {
         name = player.getName().getString();
      }

      return name != null && !name.isBlank() ? name : "player";
   }

   private static void clearState() {
      target = null;
      targetUuid = null;
      targetName = "";
      level = null;
      previousCamera = CameraType.FIRST_PERSON;
      wasThirdPerson = false;
   }

   public record ViewResult(boolean success, String message) {
   }
}
