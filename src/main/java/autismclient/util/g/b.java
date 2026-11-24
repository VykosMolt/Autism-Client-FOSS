package autismclient.util.g;

import autismclient.util.macro.A;
import java.util.List;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.phys.Vec3;

public interface b {
   default b.Inner_StreamUtil init_b_StreamUtil() {
      return null;
   }

   default b.Inner_DataUtil init_b_DataUtil_RemotePlayer(RemotePlayer bot) {
      return null;
   }

   default void init_V_b_DataUtil(b.Inner_DataUtil handle, Vec3 position, boolean onGround) {
   }

   default boolean init_Z_b_DataUtil_Z(b.Inner_DataUtil handle, boolean onGround) {
      return false;
   }

   default boolean init_Z_b_DataUtil_List(b.Inner_DataUtil handle, List<A.Inner_DataUtil> steps) {
      return false;
   }

   default String init_Str_b_DataUtil_Str(b.Inner_DataUtil handle, String dimension) {
      return "POV is unavailable";
   }

   default b.Inner_DataUtil init_b_DataUtil() {
      return null;
   }

   default b.Inner_DataUtil init_b_DataUtil_b_DataUtil(b.Inner_DataUtil handle) {
      return null;
   }

   default boolean pop_Z() {
      return false;
   }

   default void init_V_b_DataUtil(b.Inner_DataUtil handle) {
   }

   record Inner_DataUtil(String init_Str, int init_I, long init_J, long load, long read) {
   }

   record Inner_StreamUtil(Vec3 init_Vec3, String init_Str) {
   }
}
