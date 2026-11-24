package autismclient.mixin.via;

import autismclient.compat.a.DataUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.io.IOException;
import java.net.Socket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.viaversion.viafabricplus.protocoltranslator.util.ProtocolVersionDetector", remap = false)
public abstract class AutismViaProtocolVersionDetectorMixin {
   @WrapOperation(method = "get", at = @At(value = "NEW", target = "(Ljava/lang/String;I)Ljava/net/Socket;"), remap = false)
   private static Socket autism$proxySafeAutoDetection(String host, int port, Operation<Socket> original) throws IOException {
      Operation var3 = original;
      return DataUtil.init(host, port, (xva$0, xva$1) -> (Socket)var3.call(xva$0, xva$1));
   }
}
