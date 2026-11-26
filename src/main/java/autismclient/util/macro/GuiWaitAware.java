package autismclient.util.macro;

import net.minecraft.nbt.CompoundTag;

public interface GuiWaitAware {
   boolean read_Z();

   void load(boolean var1);

   boolean write_Z();

   void read(boolean var1);

   String init_Str_7();

   void init_V_Str(String var1);

   default boolean update_Z() {
      return this.read_Z() || this.write_Z();
   }

   default void write(boolean v) {
      this.load(false);
      this.read(v);
   }

   default boolean g_() {
      return false;
   }

   static boolean init_Z_CompoundTag_Z(CompoundTag tag, boolean defaultBefore) {
      if (tag == null) {
         return defaultBefore;
      } else {
         return tag.contains("waitForGuiBefore") ? tag.getBooleanOr("waitForGuiBefore", defaultBefore) : false;
      }
   }

   static boolean load_Z_CompoundTag_Z(CompoundTag tag, boolean defaultAfter) {
      if (tag == null) {
         return defaultAfter;
      } else {
         return !tag.contains("waitForGuiAfter") && !tag.contains("waitForGuiBefore")
            ? tag.getBooleanOr("waitForGui", defaultAfter)
            : tag.getBooleanOr("waitForGuiAfter", defaultAfter);
      }
   }

   static String init_Str_aP(GuiWaitAware wait) {
      if (wait == null) {
         return "";
      } else {
         boolean before = wait.read_Z();
         boolean after = wait.write_Z();
         if (before && after) {
            return " [before+after]";
         } else if (before) {
            return " [before]";
         } else {
            return after ? " [after]" : "";
         }
      }
   }
}
