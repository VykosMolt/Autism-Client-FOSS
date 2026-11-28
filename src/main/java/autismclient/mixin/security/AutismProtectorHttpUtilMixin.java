package autismclient.mixin.security;

import autismclient.protector.ScopedHttpUrlConnection;
import autismclient.protector.ResourcePackDownloadScope;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import net.minecraft.util.HttpUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HttpUtil.class)
public class AutismProtectorHttpUtilMixin {
   @WrapOperation(method = "downloadFile", at = @At(value = "INVOKE", target = "Ljava/net/URL;openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;"))
   private static URLConnection autism$scopedConnection(URL url, Proxy proxy, Operation<URLConnection> original) throws IOException {
      ResourcePackDownloadScope.Inner_BufferHelper snapshot = ResourcePackDownloadScope.workerScope();
      return snapshot == null ? original.call(url, proxy) : new ScopedHttpUrlConnection(snapshot, url, proxy);
   }
}
