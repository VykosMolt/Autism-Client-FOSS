package autismclient.util.macro;

import net.minecraft.nbt.CompoundTag;

public interface GuiWaitAware {
   boolean waitsForGuiBefore();

   void load(boolean var1);

   boolean waitsForGuiAfter();

   void read(boolean var1);

   String getGuiName();

   void setGuiName(String var1);

   default boolean update_Z() {
      return this.waitsForGuiBefore() || this.waitsForGuiAfter();
   }

   default void write(boolean v) {
      this.load(false);
      this.read(v);
   }

   default boolean g_() {
      return false;
   }

   static boolean readWaitForGuiBefore(CompoundTag tag, boolean defaultBefore) {
      if (tag == null) {
         return defaultBefore;
      } else {
         return tag.contains("waitForGuiBefore") ? tag.getBooleanOr("waitForGuiBefore", defaultBefore) : false;
      }
   }

   static boolean readWaitForGuiAfter(CompoundTag tag, boolean defaultAfter) {
      if (tag == null) {
         return defaultAfter;
      } else {
         return !tag.contains("waitForGuiAfter") && !tag.contains("waitForGuiBefore")
            ? tag.getBooleanOr("waitForGui", defaultAfter)
            : tag.getBooleanOr("waitForGuiAfter", defaultAfter);
      }
   }

   static String waitSuffix(GuiWaitAware wait) {
      if (wait == null) {
         return "";
      } else {
         boolean before = wait.waitsForGuiBefore();
         boolean after = wait.waitsForGuiAfter();
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
