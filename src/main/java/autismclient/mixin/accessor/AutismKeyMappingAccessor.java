package autismclient.mixin.accessor;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface AutismKeyMappingAccessor {
   @Accessor("clickCount")
   int autism$clickCount();

   @Accessor("clickCount")
   void autism$setClickCount(int var1);
}
