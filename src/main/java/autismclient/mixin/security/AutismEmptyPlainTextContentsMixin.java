package autismclient.mixin.security;

import autismclient.protector.PacketOriginDuck;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.network.chat.contents.PlainTextContents$1")
public class AutismEmptyPlainTextContentsMixin implements PacketOriginDuck {
   @Override
   public void autism$setFromPacket() {
      // Never taint the JVM-wide PlainTextContents.EMPTY singleton.
   }

   @Override
   public boolean autism$isFromPacket() {
      return false;
   }
}
