package autismclient.util;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;

public interface KeyMappingControl {
   static KeyMappingControl of(KeyMapping mapping) {
      return (KeyMappingControl)mapping;
   }

   boolean autism$isActuallyDown();

   boolean autism$physicalForBody();

   void autism$resetPressedState();

   void autism$simulatePress(boolean var1);

   void autism$countClick();

   void autism$refocus(Window var1);
}
