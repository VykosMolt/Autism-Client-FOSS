package autismclient.gui.b;

import autismclient.util.g.ClassCache;
import autismclient.util.g.PacketUtil;
import autismclient.util.g.RenderState;

public final class ConfigStore {
   public static final int init = -6641998;
   public static final int load = -11013497;
   public static final int read = -1526710;

   private ConfigStore() {
   }

   public static String init(PacketUtil manager, ClassCache.Inner_FieldCache snapshot) {
      if (manager != null && snapshot != null) {
         String value = manager.update_Str_Str(snapshot.init_Str());
         return value == null ? "" : value;
      } else {
         return "";
      }
   }

   public static boolean init_Z(ClassCache.Inner_FieldCache snapshot) {
      if (snapshot != null && snapshot.init_Z()) {
         ClassCache.Inner_UiHelper progress = snapshot.init_ClassCache_UiHelpe();
         return progress != null && progress.init_Z() && progress.init_Str() != null && !progress.init_Str().isBlank();
      } else {
         return false;
      }
   }

   public static String init_Str(ClassCache.Inner_FieldCache snapshot) {
      return !init_Z(snapshot) ? "" : snapshot.init_ClassCache_UiHelpe().init_Str();
   }

   public static String init(String assignedName) {
      return "Assigned: " + (assignedName != null && !assignedName.isBlank() ? assignedName : "none");
   }

   public static String load(String playingName) {
      return playingName != null && !playingName.isBlank() ? "Playing: " + playingName : "";
   }

   public static boolean init_Z(ClassCache.Inner_FieldCache snapshot, long now) {
      return snapshot != null && snapshot.init_ClassCache_RenderS() != null && snapshot.init_ClassCache_RenderS().init_Z(now);
   }

   public static String init_Str(ClassCache.Inner_FieldCache snapshot, long now) {
      return !init_Z(snapshot, now) ? "" : "Starts in " + RenderState.init_Str_J(snapshot.init_ClassCache_RenderS().init_J(now));
   }

   public static String load(PacketUtil manager, ClassCache.Inner_FieldCache snapshot) {
      String assigned = init(manager, snapshot);
      StringBuilder text = new StringBuilder("Assigned: ").append(assigned.isBlank() ? "none" : assigned);
      String playing = init_Str(snapshot);
      if (!playing.isBlank()) {
         text.append(" | Playing: ").append(playing);
      }

      String queued = init_Str(snapshot, System.currentTimeMillis());
      if (!queued.isBlank()) {
         text.append(" | ").append(queued);
      }

      return text.toString();
   }
}
