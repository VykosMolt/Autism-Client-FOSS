package autismclient.util;

import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class cd {
   private static volatile Player init_Player;
   private static volatile UUID init_UUID;
   private static volatile String init_Str = "";
   private static volatile ClientLevel init_ClientLevel;
   private static CameraType init_CameraType = CameraType.FIRST_PERSON;
   private static boolean init_Z;

   private cd() {
   }

   public static boolean init_Z() {
      return !AutismLiteVariant.enabled() && !autismclient.e.TableIndex.init() ? false : init_UUID != null;
   }

   public static String init_Str() {
      return init_Str;
   }

   public static cd.Inner_DataUtil init_cd_DataUtil(Player player) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && mc.level != null && mc.getConnection() != null) {
         if (player == null || player.isRemoved() || player.level() != mc.level) {
            return new cd.Inner_DataUtil(false, "That player is not loaded.");
         }

         if (player == mc.player) {
            return new cd.Inner_DataUtil(false, "You cannot remote-view yourself.");
         }

         if (autismclient.modules.aC.init_Z()) {
            return new cd.Inner_DataUtil(false, "Disable Freecam first.");
         }

         if (init_Z() && player.getUUID().equals(init_UUID)) {
            init(false);
            return new cd.Inner_DataUtil(true, "Remote view stopped.");
         }

         if (init_Z()) {
            init(false);
         }

         init_CameraType = mc.options.getCameraType();
         init_Z = !init_CameraType.isFirstPerson();
         init_Player = player;
         init_UUID = player.getUUID();
         init_Str = init_Str(player);
         init_ClientLevel = mc.level;
         mc.options.setCameraType(CameraType.FIRST_PERSON);
         mc.setCameraEntity(player);
         return new cd.Inner_DataUtil(true, "Viewing " + init_Str + ". Use " + autismclient.commands.DataUtil.init_Str() + "rv stop to leave.");
      } else {
         return new cd.Inner_DataUtil(false, "Join a world first.");
      }
   }

   public static void init(boolean notify) {
      Minecraft mc = Minecraft.getInstance();
      Player oldTarget = init_Player;
      String oldName = init_Str;
      boolean restorePerspective = init_Z;
      CameraType restoreType = init_CameraType;
      load();
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

   public static void init_V() {
      if (init_Z()) {
         Minecraft mc = Minecraft.getInstance();
         Player current = init_Player;
         if (mc == null
            || mc.player == null
            || mc.level == null
            || mc.getConnection() == null
            || mc.level != init_ClientLevel
            || current == null
            || current.isRemoved()
            || !current.getUUID().equals(init_UUID)
            || mc.getCameraEntity() != current) {
            init(true);
         }
      }
   }

   public static Player init_Player() {
      Minecraft mc = Minecraft.getInstance();
      Player current = init_Player;
      return init_Z() && current != null && !current.isRemoved() && mc != null && mc.level == init_ClientLevel && mc.getCameraEntity() == current
         ? current
         : null;
   }

   public static Player init_Player(Player multiPilot) {
      return multiPilot != null ? multiPilot : init_Player();
   }

   public static Entity init(Minecraft mc) {
      Player current = init_Player();
      if (current != null && mc != null && mc.player != null && mc.getCameraEntity() == current) {
         mc.setCameraEntity(mc.player);
         return current;
      } else {
         return null;
      }
   }

   public static void init(Minecraft mc, Entity restore) {
      if (mc != null && restore != null && init_Z() && restore == init_Player && !restore.isRemoved() && mc.getCameraEntity() == mc.player) {
         mc.setCameraEntity(restore);
      }
   }

   private static String init_Str(Player player) {
      String name = player.getGameProfile() == null ? null : player.getGameProfile().name();
      if (name == null || name.isBlank()) {
         name = player.getName().getString();
      }

      return name != null && !name.isBlank() ? name : "player";
   }

   private static void load() {
      init_Player = null;
      init_UUID = null;
      init_Str = "";
      init_ClientLevel = null;
      init_CameraType = CameraType.FIRST_PERSON;
      init_Z = false;
   }

   public record Inner_DataUtil(boolean init_Z, String init_Str) {
   }
}
