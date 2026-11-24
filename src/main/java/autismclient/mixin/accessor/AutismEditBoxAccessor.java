package autismclient.mixin.accessor;

import java.util.List;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBox.class)
public interface AutismEditBoxAccessor {
   @Accessor("value")
   String autism$value();

   @Accessor("maxLength")
   int autism$maxLength();

   @Accessor("isEditable")
   boolean autism$isEditable();

   @Accessor("displayPos")
   int autism$displayPos();

   @Accessor("cursorPos")
   int autism$cursorPos();

   @Accessor("highlightPos")
   int autism$highlightPos();

   @Accessor("suggestion")
   @Nullable String autism$suggestion();

   @Accessor("formatters")
   List<?> autism$formatters();

   @Accessor("hint")
   @Nullable Component autism$hint();

   @Accessor("focusedTime")
   long autism$focusedTime();

   @Accessor("textX")
   int autism$textX();

   @Accessor("textY")
   int autism$textY();
}
