package autismclient.mixin.security;

import autismclient.g.BitSetUtil;
import autismclient.g.MethodUtil;
import java.util.LinkedHashSet;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl")
public class AutismProtectorKeyMappingRegistryImplMixin {
   @Inject(method = "registerKeyMapping", at = @At("RETURN"))
   private static void autism$trackModKeyMapping(KeyMapping keyMapping, CallbackInfoReturnable<KeyMapping> cir) {
      LinkedHashSet<String> mods = BitSetUtil.init();
      if (!mods.isEmpty()) {
         MethodUtil.read(keyMapping.getName(), mods.getLast());
      }
   }
}
