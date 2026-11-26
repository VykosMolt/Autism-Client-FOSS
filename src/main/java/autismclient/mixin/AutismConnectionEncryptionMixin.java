package autismclient.mixin;

import autismclient.util.PacketCaptureState;
import autismclient.util.PacketWireCaptureStore;
import autismclient.util.PacketWireCaptureHandler;
import io.netty.channel.Channel;
import javax.crypto.Cipher;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class AutismConnectionEncryptionMixin {
   @Shadow
   private Channel channel;

   @Inject(method = "setEncryptionKey", at = @At("TAIL"))
   private void autism$observeEncryptionBoundary(Cipher decryptCipher, Cipher encryptCipher, CallbackInfo ci) {
      if (this.channel != null) {
         if (PacketCaptureState.init_Z()) {
            PacketWireCaptureStore.init_V(this.channel);
         }

         try {
            if (this.channel.pipeline().get("autism_ciphertext_in") == null && this.channel.pipeline().get("decrypt") != null) {
               this.channel.pipeline().addBefore("decrypt", "autism_ciphertext_in", new PacketWireCaptureHandler("S2C"));
            }

            if (this.channel.pipeline().get("autism_ciphertext_out") == null && this.channel.pipeline().get("encrypt") != null) {
               this.channel.pipeline().addBefore("encrypt", "autism_ciphertext_out", new PacketWireCaptureHandler("C2S"));
            }
         } catch (Throwable var5) {
         }
      }
   }
}
